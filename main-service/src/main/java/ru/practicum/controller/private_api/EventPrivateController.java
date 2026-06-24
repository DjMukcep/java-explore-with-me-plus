package ru.practicum.controller.private_api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.entity.event.EventService;

/**
 * конечные точки:
 * /users/{userId}/events
 * /users/{userId}/events/{eventId}
 * /users/{userId}/events/{eventId}/requests
 */
@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class EventPrivateController {

    private final EventService eventService;

    @PostMapping("/{userId}/events")
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto create(@PathVariable Long userId, @RequestBody @Valid NewEventDto dto) {
        return eventService.create(userId, dto);
    }

    @GetMapping("/{userId}/events/{eventId}")
    public EventFullDto getById(@PathVariable Long userId, @PathVariable Long eventId) {
        return eventService.getById(userId, eventId);
    }
}

