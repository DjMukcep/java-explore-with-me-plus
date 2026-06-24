package ru.practicum.controller.public_api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventPublicParamDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.entity.event.EventService;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventPublicController {

    private final EventService eventService;

    @GetMapping
    public List<EventShortDto> getEvents(@ModelAttribute @Valid EventPublicParamDto params,
                                         HttpServletRequest request) {
        return eventService.getPublishedEvents(params, request);
    }

    @GetMapping("/{id}")
    public EventFullDto getEventById(@PathVariable Long id,
                                     HttpServletRequest request) {
        return eventService.getPublishedEventById(id, request);
    }
}