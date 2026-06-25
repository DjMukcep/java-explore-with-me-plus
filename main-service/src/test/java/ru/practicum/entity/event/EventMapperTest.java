package ru.practicum.entity.event;

import org.junit.jupiter.api.Test;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.user.User;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventMapperTest {

    @Test
    void toEventFullDto_shouldMapAllFields() {
        Category category = Category.builder().id(1L).name("test").build();
        User initiator = User.builder().id(10L).name("initiator").build();

        Event event = Event.builder()
                .id(1L)
                .annotation("annotation")
                .description("description")
                .eventDate(LocalDateTime.of(2026, 6, 25, 12, 0))
                .createdOn(LocalDateTime.of(2026, 6, 24, 12, 0))
                .publishedOn(LocalDateTime.of(2026, 6, 24, 13, 0))
                .paid(true)
                .participantLimit(10)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .title("title")
                .category(category)
                .initiator(initiator)
                .build();

        EventFullDto dto = EventMapper.toEventFullDto(event, 5L, 100L);

        assertEquals(1L, dto.getId());
        assertEquals("annotation", dto.getAnnotation());
        assertEquals("description", dto.getDescription());
        assertEquals(5L, dto.getConfirmedRequests());
        assertEquals(100L, dto.getViews());
        assertTrue(dto.getPaid());
        assertEquals("PUBLISHED", dto.getState());
    }

    @Test
    void toEventShortDto_shouldMapOnlyShortFields() {
        Category category = Category.builder().id(1L).name("test").build();
        User initiator = User.builder().id(10L).name("initiator").build();

        Event event = Event.builder()
                .id(1L)
                .annotation("annotation")
                .eventDate(LocalDateTime.of(2026, 6, 25, 12, 0))
                .paid(false)
                .title("title")
                .category(category)
                .initiator(initiator)
                .build();

        EventShortDto dto = EventMapper.toEventShortDto(event, 3L, 50L);

        assertEquals(1L, dto.getId());
        assertEquals("annotation", dto.getAnnotation());
        assertEquals(3L, dto.getConfirmedRequests());
        assertEquals(50L, dto.getViews());
        assertFalse(dto.getPaid());
        assertEquals("title", dto.getTitle());
    }

    @Test
    void updateEventFromAdminRequest_shouldUpdateOnlyNonNullFields() {
        Event event = Event.builder()
                .id(1L)
                .annotation("old")
                .title("old")
                .paid(false)
                .participantLimit(5)
                .build();

        Category category = Category.builder().id(2L).name("cat").build();

        UpdateEventAdminRequest request = UpdateEventAdminRequest.builder()
                .annotation("new annotation")
                .paid(true)
                .build();

        EventMapper.updateEventFromAdminRequest(request, event, category);

        assertEquals("new annotation", event.getAnnotation());
        assertEquals("old", event.getTitle());
        assertTrue(event.getPaid());
        assertEquals(2L, event.getCategory().getId());
    }
}