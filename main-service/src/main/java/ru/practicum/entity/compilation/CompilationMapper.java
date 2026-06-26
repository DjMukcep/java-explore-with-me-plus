package ru.practicum.entity.compilation;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventMapper;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class CompilationMapper {

    public static CompilationDto toDto(Compilation entity, Map<Long, Long> eventHits) {
        CompilationDto dto = new CompilationDto();

        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setPinned(entity.getPinned());

        Set<EventShortDto> events = entity.getEvents().stream()
                .map(event -> EventMapper.mapToShortDto(
                        event, event.getInitiator(), eventHits.getOrDefault(event.getId(), 0L))
                )
                .collect(Collectors.toSet());

        dto.setEvents(events);
        return dto;
    }

    public static Compilation toEntity(NewCompilationDto dto, Set<Event> events) {
        Compilation entity = new Compilation();

        if (dto.getPinned() != null) {
            entity.setPinned(dto.getPinned());
        }

        if (dto.getEvents() != null) {
            entity.setEvents(events);
        }

        entity.setTitle(dto.getTitle());

        return entity;
    }
}
