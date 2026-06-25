package ru.practicum.entity.event;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatClient;
import ru.practicum.StatsRequest;
import ru.practicum.ViewStats;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.entity.category.CategoryService;
import ru.practicum.entity.event.QEvent;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserService;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final StatClient statClient;
    private final EventRepository eventRepository;
    private final CategoryService categoryService;
    private final UserService userService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EventFullDto create(Long userId, NewEventDto dto) {

        User user = userService.findById(userId);
        CategoryDto category = categoryService.findById(dto.getCategory());

        Event event = EventMapper.mapToEntity(dto, category, user);

        event = eventRepository.save(event);

        log.info("event created {}", event);

        return EventMapper.mapToFullDto(event, user, 0);
    }

    @Override
    public EventFullDto getById(Long userId, Long eventId) {
        User user = userService.findById(userId);

        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new NotFoundException(String.format("Event with id '%d' not found", eventId))
        );

        List<String> uris = List.of("/events/" + eventId);

        StatsRequest request = new StatsRequest(
                event.getCreatedOn().format(FORMATTER),
                LocalDateTime.now().format(FORMATTER),
                uris,
                false
        );

        long hits = statClient.getViewStats(request).stream()
                .map(ViewStats::getHits)
                .mapToLong(Long::longValue)
                .sum();

        return EventMapper.mapToFullDto(event, user, hits);
    }

    @Override
    public List<Event> getByIds(Collection<Long> ids) {
        return eventRepository.findAllById(ids);
    }
}
