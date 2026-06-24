package ru.practicum.entity.event;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;

import ru.practicum.entity.category.CategoryMapper;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserMapper;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@UtilityClass
public class EventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event mapToEntity(NewEventDto eventDto, CategoryDto categoryDto, User user) {
        Event entity = new Event();

        entity.setTitle(eventDto.getTitle());
        entity.setAnnotation(eventDto.getAnnotation());
        entity.setCategory(CategoryMapper.toEntity(categoryDto));

        LocalDateTime time;
        try {
            time = LocalDateTime.parse(eventDto.getEventDate(), FORMATTER);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid time format: " + eventDto.getEventDate());
        }

        if (time.isBefore(LocalDateTime.now())) {
            throw new ValidationException("eventDate must contain future date, but was: " + time.format(FORMATTER));
        }

        entity.setEventDate(LocalDateTime.parse(eventDto.getEventDate(), FORMATTER));
        entity.setDescription(eventDto.getDescription());
        entity.setParticipantLimit(eventDto.getParticipantLimit());
        entity.setCreatedOn(LocalDateTime.now());
        entity.setInitiator(user);
        entity.setLocation(eventDto.getLocation());
        entity.setPaid(eventDto.getPaid());
        entity.setState(EventState.PENDING);
        entity.setRequestModeration(eventDto.getRequestModeration());

        return entity;
    }

    public static EventFullDto mapToFullDto(Event entity, User user, long views) {
        EventFullDto dto = new EventFullDto();

        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setAnnotation(entity.getAnnotation());
        dto.setDescription(entity.getDescription());
        dto.setCategory(CategoryMapper.toDto(entity.getCategory()));
        dto.setEventDate(entity.getEventDate().format(FORMATTER));
        if (entity.getPublishedOn() != null) {
            dto.setPublishedOn(entity.getPublishedOn().format(FORMATTER));
        }
        dto.setCreatedOn(entity.getCreatedOn().format(FORMATTER));
        dto.setParticipantLimit(entity.getParticipantLimit());
        dto.setPaid(entity.getPaid());
        dto.setInitiator(UserMapper.toShortDto(user));
        dto.setLocation(entity.getLocation());
        dto.setRequestModeration(entity.getRequestModeration());
        dto.setState(entity.getState().name());
        dto.setViews(views);

        return dto;
    }
}
