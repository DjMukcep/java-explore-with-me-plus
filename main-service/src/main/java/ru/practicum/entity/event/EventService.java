package ru.practicum.entity.event;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.*;

import java.util.List;

public interface EventService {
    // Admin
    List<EventFullDto> getEventsByAdmin(EventAdminParamDto params);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest request);

    // Public
    List<EventShortDto> getPublishedEvents(EventPublicParamDto params, HttpServletRequest request);

    EventFullDto getPublishedEventById(Long id, HttpServletRequest request);

    // Private
    EventFullDto create(Long userId, NewEventDto dto);

    EventFullDto getById(Long userId, Long eventId);
}