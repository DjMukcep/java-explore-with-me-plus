package ru.practicum.entity.event;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.UserShortDto;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.NewEventDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.user.User;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
        entity.setLocation(toEntityLocation(eventDto.getLocation()));
        entity.setPaid(eventDto.getPaid());
        entity.setState(EventState.PENDING);
        entity.setRequestModeration(eventDto.getRequestModeration());

        return entity;
    }

    public static EventFullDto toEventFullDto(Event event, Long confirmedRequests, Long views) {
        EventFullDto dto = new EventFullDto();

        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()));
        dto.setConfirmedRequests(confirmedRequests);
        dto.setCreatedOn(event.getCreatedOn().format(FORMATTER));
        dto.setDescription(event.getDescription());
        dto.setEventDate(event.getEventDate().format(FORMATTER));
        dto.setInitiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()));
        dto.setLocation(toDtoLocation(event.getLocation()));
        dto.setPaid(event.getPaid());
        dto.setParticipantLimit(event.getParticipantLimit());
        if (event.getPublishedOn() != null) {
            dto.setPublishedOn(event.getPublishedOn().format(FORMATTER));
        }
        dto.setRequestModeration(event.getRequestModeration());
        dto.setState(event.getState().name());
        dto.setTitle(event.getTitle());
        dto.setViews(views);

        return dto;
    }

    public static EventShortDto toEventShortDto(Event event, Long confirmedRequests, Long views) {
        EventShortDto dto = new EventShortDto();

        dto.setId(event.getId());
        dto.setAnnotation(event.getAnnotation());
        dto.setCategory(new CategoryDto(event.getCategory().getId(), event.getCategory().getName()));
        dto.setConfirmedRequests(confirmedRequests);
        dto.setEventDate(event.getEventDate().format(FORMATTER));
        dto.setInitiator(new UserShortDto(event.getInitiator().getId(), event.getInitiator().getName()));
        dto.setPaid(event.getPaid());
        dto.setTitle(event.getTitle());
        dto.setViews(views);

        return dto;
    }

    public static EventShortDto mapToShortDto(Event entity, User user, long views) {
        EventShortDto dto = new EventShortDto();

        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setAnnotation(entity.getAnnotation());
        dto.setCategory(CategoryMapper.toDto(entity.getCategory()));
        dto.setEventDate(entity.getEventDate().format(FORMATTER));
        dto.setPaid(entity.getPaid());
        dto.setInitiator(UserMapper.toShortDto(user));
        dto.setRequestModeration(entity.getRequestModeration());
        dto.setState(entity.getState().name());
        dto.setViews(views);

        return dto;
    }
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
            event.setLocation(toEntityLocation(request.getLocation()));
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

    // ---------- Конвертация Location ----------

    private static ru.practicum.entity.Location toEntityLocation(ru.practicum.dto.Location dto) {
        if (dto == null) return null;
        return ru.practicum.entity.Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }

    private static ru.practicum.dto.Location toDtoLocation(ru.practicum.entity.Location entity) {
        if (entity == null) return null;
        return ru.practicum.dto.Location.builder()
                .lat(entity.getLat())
                .lon(entity.getLon())
                .build();
    }
}
