package ru.practicum.entity.event;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHit;
import ru.practicum.StatClient;
import ru.practicum.StatsRequest;
import ru.practicum.ViewStats;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.category.CategoryMapper;
import ru.practicum.entity.category.CategoryService;
import ru.practicum.entity.request.Request;
import ru.practicum.entity.request.RequestMapper;
import ru.practicum.entity.request.RequestService;
import ru.practicum.entity.request.RequestStatus;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final StatClient statClient;
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final RequestService requestService;
    private final UserService userService;

    private static final String APP_NAME = "ewm-main-service";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ---------- Private ----------

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        User user = userService.findById(userId);
        CategoryDto categoryDto = categoryService.findById(dto.getCategory());
        Category category = CategoryMapper.toEntity(categoryDto);

        Event event = EventMapper.mapToEntity(dto, category, user);
        event = eventRepository.save(event);

        log.info("event created {}", event);

        return EventMapper.toEventFullDto(event, 0L, 0L);
    }

    @Override
    public EventFullDto getById(Long userId, Long eventId) {
        userService.findById(userId);

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException(String.format("Event with id '%d' not found", eventId))
        );

        Map<Long, Long> viewsMap = getViewsMap(Collections.singletonList(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);

        return EventMapper.toEventFullDto(event, 0L, views);
    }

    @Override
    public List<EventShortDto> getUserEvents(Long userId, Pageable pageable) {
        userService.checkUserExist(userId);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        List<Long> eventIds = events.stream().map(Event::getId).toList();
        List<EventRequestsCountDto> requests = requestService.countByEventIdsAndStatus(eventIds, RequestStatus.CONFIRMED);
        Map<Long, Long> eventRequestsCount = requests.stream()
                .collect(Collectors.toMap(EventRequestsCountDto::eventId, EventRequestsCountDto::count));
        Map<Long, Long> viewsMap = getViewsMap(events);

        return EventMapper.toEventShortDto(events, eventRequestsCount, viewsMap);
    }

    @Override
    @Transactional
    public EventFullDto updateEventByCreatorId(EventParamDto eventParamDto, UpdateEventUserRequest request) {
        userService.checkUserExist(eventParamDto.getUserId());
        Event event = getEvent(eventParamDto.getEventId());
        checkUserIsEventInitiator(eventParamDto.getUserId(), event);
        checkEventUpdateConditions(event, request);

        updateEventFromUserRequest(request, event);

        List<EventRequestsCountDto> requestsDto =
                requestService.countByEventIdsAndStatus(List.of(event.getId()), RequestStatus.CONFIRMED);
        Long requestsCount = requestsDto.isEmpty() ? 0L : requestsDto.getFirst().count();
        Long views = getViewsMap(Collections.singletonList(event)).getOrDefault(event.getId(), 0L);

        return EventMapper.toEventFullDto(event, requestsCount, views);
    }

    @Override
    public List<ParticipationRequestDto> getEventRequestsByCreatorId(Long userId, Long eventId) {
        userService.checkUserExist(userId);
        Event event = getEvent(eventId);
        checkUserIsEventInitiator(userId, event);

        return requestService.getParticipationRequestsByEventId(eventId);
    }

    /**
     * если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется
     * нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)
     * статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)
     * если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить
     */
    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestsStatus(EventParamDto eventParamDto,
                                                                    EventRequestStatusUpdateRequest request) {
        Long eventId = eventParamDto.getEventId();
        Long userId = eventParamDto.getUserId();
        Event event = getEvent(eventId);
        userService.checkUserExist(userId);
        checkUserIsEventInitiator(userId, event);
        List<Request> requests = requestService.findByIds(request.getRequestIds());

        long confirmedRequests = requestService.countByEventIdAndStatus(event.getId(), RequestStatus.CONFIRMED);
        long remainedRequests = event.getParticipantLimit() - confirmedRequests;

        RequestStatus targetStatus = request.getStatus();
        
        if (targetStatus == RequestStatus.CONFIRMED) {
            
            if (event.getParticipantLimit() == 0 || !event.getRequestModeration()) {
                throw new ConflictException("Confirmation is not required or allowed for this event configuration.");
            }
            
            if (remainedRequests <= 0) {
                throw new ConflictException("The participant limit has been reached");
            }
        }

        List<Request> confirmed = new ArrayList<>();
        List<Request> rejected = new ArrayList<>();

        for (Request req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException(String.format("Request must have status PENDING. " +
                        "Request with id=" + req.getId() + " has status " + req.getStatus()));
            }

            if (targetStatus == RequestStatus.REJECTED) {
                req.setStatus(RequestStatus.REJECTED);
                rejected.add(req);
                continue;
            }

            if (remainedRequests == 0L) {
                req.setStatus(RequestStatus.REJECTED);
                rejected.add(req);
                continue;
            }
            req.setStatus(RequestStatus.CONFIRMED);
            confirmed.add(req);
            remainedRequests--;
        }

        List<ParticipationRequestDto> confirmedDto = RequestMapper.toRequestDto(confirmed);
        List<ParticipationRequestDto> rejectedDto = RequestMapper.toRequestDto(rejected);

        return new EventRequestStatusUpdateResult(confirmedDto, rejectedDto);
    }

    // ---------- Admin ----------

    @Override
    public List<EventFullDto> getEventsByAdmin(EventAdminParamDto params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Predicate predicate = buildAdminPredicate(params);

        List<Event> events = eventRepository.findAll(predicate, pageable).getContent();

        if (events.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, Long> viewsMap = getViewsMap(events);

        return events.stream()
                .map(event -> {
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return EventMapper.toEventFullDto(event, 0L, views);
                })
                .toList();
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Category category = null;
        if (request.getCategory() != null) {
            CategoryDto categoryDto = categoryService.findById(request.getCategory());
            category = CategoryMapper.toEntity(categoryDto);
        }
        EventMapper.updateEventFromAdminRequest(request, event, category);

        applyStateAction(event, request.getStateAction());

        Map<Long, Long> viewsMap = getViewsMap(Collections.singletonList(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);

        return EventMapper.toEventFullDto(event, 0L, views);
    }

    private void applyStateAction(Event event, String stateAction) {
        if (stateAction == null) return;

        LocalDateTime now = LocalDateTime.now();

        switch (stateAction) {
            case "PUBLISH_EVENT":
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                if (event.getEventDate().isBefore(now.plusHours(1))) {
                    throw new ConflictException("Cannot publish the event because event date is less than 1 hour from now.");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(now);
                break;
            case "REJECT_EVENT":
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Cannot reject the event because it's already published.");
                }
                event.setState(EventState.CANCELED);
                break;
            default:
                throw new ConflictException("Invalid state action: " + stateAction);
        }
    }

    // ---------- Public ----------

    @Override
    public List<EventShortDto> getPublishedEvents(EventPublicParamDto params, HttpServletRequest request) {
        validateDateRange(params.getRangeStart(), params.getRangeEnd());

        LocalDateTime rangeStart = params.getRangeStart() != null ? params.getRangeStart() : LocalDateTime.now();

        Pageable pageable = buildPageable(params.getSort(), params.getFrom(), params.getSize());
        Predicate predicate = buildPublicPredicate(params, rangeStart);

        List<Event> events = ((Page<Event>) eventRepository.findAll(predicate, pageable)).getContent();

        if (events.isEmpty()) {
            sendHit(request);
            return Collections.emptyList();
        }

        Map<Long, Long> viewsMap = getViewsMap(events);

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return EventMapper.toEventShortDto(event, 0L, views);
                })
                .toList();

        if (params.isOnlyAvailable()) {
            result = filterAvailable(result);
        }

        if ("VIEWS".equals(params.getSort())) {
            result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        sendHit(request);
        return result;
    }

    @Override
    public EventFullDto getPublishedEventById(Long id, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(id, EventState.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Event with id=" + id + " was not found"));

        sendHit(request);

        Map<Long, Long> viewsMap = getViewsMap(Collections.singletonList(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);

        return EventMapper.toEventFullDto(event, 0L, views);
    }

    // ---------- QueryDSL predicates ----------

    private Predicate buildAdminPredicate(EventAdminParamDto params) {
        QEvent event = QEvent.event;
        BooleanBuilder builder = new BooleanBuilder();

        if (params.getUsers() != null && !params.getUsers().isEmpty()) {
            builder.and(event.initiator.id.in(params.getUsers()));
        }
        if (params.getStates() != null && !params.getStates().isEmpty()) {
            builder.and(event.state.in(params.getStates().stream()
                    .map(stateStr -> EventState.valueOf(stateStr.toUpperCase()))
                    .toList()));
        }
        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            builder.and(event.category.id.in(params.getCategories()));
        }
        if (params.getRangeStart() != null) {
            builder.and(event.eventDate.goe(params.getRangeStart()));
        }
        if (params.getRangeEnd() != null) {
            builder.and(event.eventDate.loe(params.getRangeEnd()));
        }
        return builder.getValue();
    }

    private Predicate buildPublicPredicate(EventPublicParamDto params, LocalDateTime rangeStart) {
        QEvent event = QEvent.event;
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(event.state.eq(EventState.PUBLISHED));

        if (params.getText() != null && !params.getText().isBlank()) {
            String text = params.getText();
            builder.and(new BooleanBuilder()
                    .or(event.annotation.containsIgnoreCase(text))
                    .or(event.description.containsIgnoreCase(text)));
        }
        if (params.getCategories() != null && !params.getCategories().isEmpty()) {
            builder.and(event.category.id.in(params.getCategories()));
        }
        if (params.getPaid() != null) {
            builder.and(event.paid.eq(params.getPaid()));
        }
        if (rangeStart != null) {
            builder.and(event.eventDate.goe(rangeStart));
        }
        if (params.getRangeEnd() != null) {
            builder.and(event.eventDate.loe(params.getRangeEnd()));
        }
        return builder.getValue();
    }

    // ---------- Private helpers ----------

    private void validateDateRange(LocalDateTime start, LocalDateTime end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new ValidationException("Range end must be after range start");
        }
    }

    private Pageable buildPageable(String sort, int from, int size) {
        if ("EVENT_DATE".equals(sort)) {
            return PageRequest.of(from / size, size, Sort.by("eventDate").ascending());
        }
        return PageRequest.of(from / size, size);
    }

    private List<EventShortDto> filterAvailable(List<EventShortDto> events) {
        return events;
    }

    private void sendHit(HttpServletRequest request) {
        statClient.hit(EndpointHit.builder()
                .app(APP_NAME)
                .uri(request.getRequestURI())
                .ip(request.getRemoteAddr())
                .timestamp(LocalDateTime.now().format(FORMATTER))
                .build());
    }

    private Map<Long, Long> getViewsMap(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(1));

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .toList();

        List<ViewStats> stats = statClient.getViewStats(new StatsRequest(
                start.format(FORMATTER),
                LocalDateTime.now().plusYears(1).format(FORMATTER),
                uris, false));

        return stats.stream()
                .collect(Collectors.toMap(
                        vs -> Long.parseLong(vs.getUri().substring("/events/".length())),
                        ViewStats::getHits));
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException("Event with id " + eventId + " not found")
        );
    }

    private void updateEventFromUserRequest(UpdateEventUserRequest request, Event event) {
        ofNullable(request.getAnnotation()).ifPresent(event::setAnnotation);
        ofNullable(request.getDescription()).ifPresent(event::setDescription);
        ofNullable(request.getEventDate()).ifPresent(event::setEventDate);
        ofNullable(request.getLocation()).ifPresent(event::setLocation);
        ofNullable(request.getPaid()).ifPresent(event::setPaid);
        ofNullable(request.getParticipantLimit()).ifPresent(event::setParticipantLimit);
        ofNullable(request.getRequestModeration()).ifPresent(event::setRequestModeration);
        ofNullable(request.getTitle()).ifPresent(event::setTitle);

        if (request.getCategory() != null) {
            CategoryDto categoryDto = categoryService.findById(request.getCategory());
            Category category = CategoryMapper.toEntity(categoryDto);
            event.setCategory(category);
        }

        if (request.getStateAction() == StateAction.CANCEL_REVIEW) {
            event.setState(EventState.CANCELED);
        }

        if (request.getStateAction() == StateAction.SEND_TO_REVIEW) {
            event.setState(EventState.PENDING);
        }
    }

    private void checkUserIsEventInitiator(Long userId, Event event) {
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConflictException(
                    String.format("User with id %d didn't initiate event with id %d", userId, event.getId())
            );
        }
    }

    private void checkEventUpdateConditions(Event event, UpdateEventUserRequest request) {
        if (event.getState().equals(EventState.PUBLISHED)) {
            throw new ConflictException("Only pending or canceled events can be changed");
        }
        if (request.getEventDate() != null && !request.getEventDate().isAfter(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Event date must be at least two hours in the future.");

        }
    }
}