package ru.practicum.entity.event;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;

import java.util.Collection;
import java.util.List;

public interface EventService {

    EventFullDto create(Long userId, NewEventDto dto);

    EventFullDto getById(Long userId, Long eventId);

    List<Event> getByIds(Collection<Long> ids);
}
