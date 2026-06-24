package ru.practicum.entity.event;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto dto);

    EventFullDto getById(Long userId, Long eventId);
}
