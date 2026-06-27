package ru.practicum.entity.event;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.user.User;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@UtilityClass
public class EventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event mapToEntity(NewEventDto eventDto, Category category, User user) {
        Event entity = new Event();

        entity.setTitle(eventDto.getTitle());
        entity.setAnnotation(eventDto.getAnnotation());
        entity.setCategory(category);

        LocalDateTime time;
        try {
            time = LocalDateTime.parse(eventDto.getEventDate(), FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid time format: " + eventDto.getEventDate());
        }

        if (time.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConditionsNotMetException("eventDate must contain future date, but was: " + time.format(FORMATTER));
        }

        entity.setEventDate(time);
        entity.setDescription(eventDto.getDescription());
        entity.setParticipantLimit(eventDto.getParticipantLimit());
        entity.setCreatedOn(LocalDateTime.now());
        entity.setInitiator(user);
        entity.setLocation(new Location(eventDto.getLocation().getLat(),eventDto.getLocation().getLon()));
        entity.setPaid(eventDto.getPaid());
        entity.setState(EventState.PENDING);
        entity.setRequestModeration(eventDto.getRequestModeration());

        return entity;
    }

    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                .confirmedRequests(confirmedRequests)
                .createdOn(event.getCreatedOn().format(FORMATTER))
                .description(Optional.ofNullable(event.getDescription()).isPresent() ? event.getDescription() : null)
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()))
                .location(new Location(event.getLocation().getLat(), event.getLocation().getLon()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(Optional.ofNullable(
                        event.getPublishedOn()).isPresent() ? event.getPublishedOn().format(FORMATTER) : null)
                .state(event.getState().name())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views) {
        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                .confirmedRequests(confirmedRequests)
                .eventDate(event.getEventDate().format(FORMATTER))
                .initiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(views)
                .build();
    }

    public static List<EventShortDto> toEventShortDto(List<Event> events,
                                                      Map<Long,Long> confirmedRequests,
                                                      Map<Long,Long> views) {
        return events.stream()
                .map(event ->
                   EventShortDto.builder()
                            .id(event.getId())
                            .annotation(event.getAnnotation())
                            .category(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()))
                            .confirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L))
                            .eventDate(event.getEventDate().format(FORMATTER))
                            .initiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()))
                            .paid(event.getPaid())
                            .title(event.getTitle())
                            .views(views.getOrDefault(event.getId(), 0L))
                            .build()
                )
                .toList();
    }

    public static void updateEventFromAdminRequest(UpdateEventAdminRequest request, Event event, Category category) {
        if (request.getAnnotation() != null) {
            event.setAnnotation(request.getAnnotation());
        }
        if (category != null) {
            event.setCategory(category);
        }
        if (request.getDescription() != null) {
            event.setDescription(request.getDescription());
        }
        if (request.getEventDate() != null) {
            event.setEventDate(request.getEventDate());
        }
        if (request.getLocation() != null) {
            event.setLocation(new Location(request.getLocation().getLat(), request.getLocation().getLon()));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null) {
            event.setTitle(request.getTitle());
        }
    }
}