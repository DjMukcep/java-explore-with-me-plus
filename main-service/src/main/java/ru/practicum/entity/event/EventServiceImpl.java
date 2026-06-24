package ru.practicum.entity.event;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatClient;
import ru.practicum.StatsRequest;
import ru.practicum.ViewStats;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.category.CategoryRepository;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserRepository;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final StatClient statClient;
    private final EventRepository eventRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public EventFullDto create(Long userId, NewEventDto dto) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format("User with id '%d' not found", userId))
        );

        Category category = categoryRepository.findById(dto.getCategory()).orElseThrow(
                () -> new NotFoundException(String.format("Category with id '%d' not found", dto.getCategory()))
        );

        Event event = EventMapper.mapToEntity(dto, category, user);

        event = eventRepository.save(event);

        return EventMapper.mapToFullDto(event, user, 0);
    }

    @Override
    public EventFullDto getById(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundException(String.format("User with id '%d' not found", userId))
        );

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
}
