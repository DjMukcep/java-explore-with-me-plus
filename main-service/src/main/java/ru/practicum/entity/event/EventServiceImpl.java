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
import ru.practicum.dto.event.*;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.category.CategoryService;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final StatClient statClient;
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    private static final String APP_NAME = "ewm-main-service";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ---------- Private ----------

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        User user = userService.findById(userId);
        Category category = categoryService.findEntityById(dto.getCategory());

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

    // ---------- Admin ----------

    @Override
    public List<EventFullDto> getEventsByAdmin(EventAdminParamDto params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());
        Predicate predicate = buildAdminPredicate(params);

        Iterable<Event> eventsIterable = eventRepository.findAll(predicate, pageable);
        List<Event> events = new ArrayList<>();
        eventsIterable.forEach(events::add);

        Map<Long, Long> viewsMap = getViewsMap(events);

        return events.stream()
                .map(event -> {
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return EventMapper.toEventFullDto(event, 0L, views);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        applyStateAction(event, request.getStateAction());

        Category category = null;
        if (request.getCategory() != null) {
            category = categoryService.findEntityById(request.getCategory());
        }
        EventMapper.updateEventFromAdminRequest(request, event, category);
        eventRepository.save(event);

        Map<Long, Long> viewsMap = getViewsMap(Collections.singletonList(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);

        return EventMapper.toEventFullDto(event, 0L, views);
    }

    private void applyStateAction(Event event, String stateAction) {
        if (stateAction == null) return;

        switch (stateAction) {
            case "PUBLISH_EVENT":
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Cannot publish the event because it's not in the right state: " + event.getState());
                }
                if (event.getEventDate().isBefore(LocalDateTime.now().plusHours(1))) {
                    throw new ConflictException("Cannot publish the event because event date is less than 1 hour from now.");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
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

        Page<Event> eventsPage = eventRepository.findAll(predicate, pageable);
        List<Event> events = eventsPage.getContent();
        Map<Long, Long> viewsMap = getViewsMap(events);

        List<EventShortDto> result = events.stream()
                .map(event -> {
                    Long views = viewsMap.getOrDefault(event.getId(), 0L);
                    return EventMapper.toEventShortDto(event, 0L, views);
                })
                .collect(Collectors.toList());

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

        Map<Long, Long> viewsMap = getViewsMap(Collections.singletonList(event));
        Long views = viewsMap.getOrDefault(event.getId(), 0L);

        sendHit(request);
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
                    .map(EventState::valueOf).toList()));
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
            String text = params.getText().toLowerCase();
            builder.and(event.annotation.lower().like("%" + text + "%")
                    .or(event.description.lower().like("%" + text + "%")));
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
        if (events.isEmpty()) return Collections.emptyMap();

        LocalDateTime start = events.stream()
                .map(Event::getCreatedOn)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now().minusYears(1));

        List<String> uris = events.stream()
                .map(event -> "/events/" + event.getId())
                .collect(Collectors.toList());

        List<ViewStats> stats = statClient.getViewStats(new StatsRequest(
                start.format(FORMATTER),
                LocalDateTime.now().plusYears(1).format(FORMATTER),
                uris, false));

        return stats.stream()
                .collect(Collectors.toMap(
                        vs -> Long.parseLong(vs.getUri().substring("/events/".length())),
                        ViewStats::getHits));
    }
}