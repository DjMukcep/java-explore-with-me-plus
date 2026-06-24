package ru.practicum.entity.event;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
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
import ru.practicum.entity.category.CategoryRepository;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final StatClient statClient;
    private static final String APP_NAME = "ewm-main-service";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // ---------- Admin ----------

    @Override
    public List<EventFullDto> getEventsByAdmin(EventAdminParamDto params) {
        Pageable pageable = PageRequest.of(params.getFrom() / params.getSize(), params.getSize());

        List<EventState> states = null;
        if (params.getStates() != null && !params.getStates().isEmpty()) {
            states = params.getStates().stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
        }

        List<Event> events = eventRepository.findEventsByAdmin(
                params.getUsers(), states, params.getCategories(),
                params.getRangeStart(), params.getRangeEnd(), pageable);

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
            category = categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Category with id=" + request.getCategory() + " was not found"));
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

        Page<Event> eventsPage = eventRepository.findPublishedEvents(
                params.getText(), params.getCategories(), params.getPaid(),
                rangeStart, params.getRangeEnd(), pageable);

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
        // Заглушка до реализации RequestRepository
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
                .filter(createdOn -> createdOn != null)
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