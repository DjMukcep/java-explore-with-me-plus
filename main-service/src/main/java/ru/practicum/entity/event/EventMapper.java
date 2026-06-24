package ru.practicum.entity.event;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.NewEventDto;

import ru.practicum.entity.category.Category;
import ru.practicum.entity.category.CategoryMapper;
import ru.practicum.entity.user.User;
import ru.practicum.entity.user.UserMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@UtilityClass
public class EventMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Event mapToEntity(NewEventDto eventDto, Category categoryEntity, User user) {
        Event entity = new Event();

        entity.setTitle(eventDto.getTitle());
        entity.setAnnotation(eventDto.getAnnotation());
        entity.setCategory(categoryEntity);
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
        dto.setCategory(CategoryMapper.mapToDto(entity.getCategory()));
        dto.setEventDate(entity.getEventDate().format(FORMATTER));
        dto.setPublishedOn(entity.getPublishedOn().format(FORMATTER));
        dto.setCreatedOn(entity.getCreatedOn().format(FORMATTER));
        dto.setParticipantLimit(entity.getParticipantLimit());
        dto.setPaid(entity.getPaid());
        dto.setInitiator(UserMapper.mapToShortDto(user));
        dto.setLocation(entity.getLocation());
        dto.setRequestModeration(entity.getRequestModeration());
        dto.setState(entity.getState().toString());
        dto.setViews(views);

        return dto;
    }
}
