package ru.practicum.entity.compilation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.entity.Location;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventMapper;
import ru.practicum.entity.event.EventState;
import ru.practicum.entity.user.User;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

class CompilationMapperTest {

    private static final long EVENT_ID_1 = 1L;
    private static final long EVENT_ID_2 = 2L;
    private static final long COMPILATION_ID = 100L;
    private static final long CATEGORY_ID = 201L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private User initiator;
    private Set<Event> eventsSet;
    private Map<Long, Long> eventHits;
    private Event event1;
    private Event event2;
    private Compilation compilation;
    private EventShortDto shortDto1;
    private EventShortDto shortDto2;

    @BeforeEach
    void setUp() {
        initiator = User.builder()
                .name("test name")
                .mail("test@mail.com")
                .build();
        UserShortDto initiatorDto = UserShortDto.builder()
                .id(initiator.getId())
                .name(initiator.getName())
                .build();

        Category category = Category.builder()
                .id(CATEGORY_ID)
                .name("test category")
                .build();
        CategoryDto categoryDto = CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
        Location location = Location.builder()
                .lat(123.123f)
                .lon(31.321f)
                .build();

        eventHits = new HashMap<>();
        eventHits.put(EVENT_ID_1, 10L);
        eventHits.put(EVENT_ID_2, 5L);

        event1 = Event.builder()
                .id(EVENT_ID_1)
                .initiator(initiator)
                .category(category)
                .annotation("Annotation 1")
                .eventDate(LocalDateTime.now())
                .location(location)
                .paid(false)
                .participantLimit(10)
                .publishedOn(LocalDateTime.now())
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .title("Event 1")
                .description("Description 1")
                .createdOn(LocalDateTime.now())
                .build();

        event2 = Event.builder()
                .id(EVENT_ID_2)
                .initiator(initiator)
                .category(category)
                .annotation("Annotation 2")
                .eventDate(LocalDateTime.now().plusHours(1))
                .location(location)
                .paid(true)
                .participantLimit(20)
                .publishedOn(LocalDateTime.now().plusHours(1))
                .requestModeration(false)
                .state(EventState.PENDING)
                .title("Event 2")
                .description("Description 2")
                .createdOn(LocalDateTime.now().plusHours(1))
                .build();

        eventsSet = new HashSet<>(Arrays.asList(event1, event2));

        shortDto1 = EventShortDto.builder()
                .id(event1.getId())
                .title(event1.getTitle())
                .annotation(event1.getAnnotation())
                .category(categoryDto)
                .eventDate(event1.getEventDate().format(FORMATTER))
                .initiator(initiatorDto)
                .paid(event1.getPaid())
                .requestModeration(event1.getRequestModeration())
                .state(event1.getState().name())
                .views(eventHits.get(EVENT_ID_1))
                .build();
        shortDto2 = EventShortDto.builder()
                .id(event2.getId())
                .title(event2.getTitle())
                .annotation(event2.getAnnotation())
                .category(categoryDto)
                .eventDate(event2.getEventDate().format(FORMATTER))
                .initiator(initiatorDto)
                .paid(event2.getPaid())
                .requestModeration(event2.getRequestModeration())
                .state(event2.getState().name())
                .views(eventHits.get(EVENT_ID_2))
                .build();

        compilation = new Compilation();
        compilation.setId(COMPILATION_ID);
        compilation.setTitle("Test Compilation");
        compilation.setPinned(true);
        compilation.setEvents(eventsSet);
    }

    @Test
    void toDto_shouldMapEntityToDtoWithCorrectHits() {
        try (MockedStatic<EventMapper> mocked = mockStatic(EventMapper.class)) {
            mocked.when(() -> EventMapper.mapToShortDto(eq(event1), same(initiator), eq(10L)))
                    .thenReturn(shortDto1);
            mocked.when(() -> EventMapper.mapToShortDto(eq(event2), same(initiator), eq(5L)))
                            .thenReturn(shortDto2);

            CompilationDto result = CompilationMapper.toDto(compilation, eventHits);

            assertThat(result.getId(), equalTo(COMPILATION_ID));
            assertThat(result.getTitle(), equalTo("Test Compilation"));
            assertThat(result.getPinned(), equalTo(true));
            assertThat(result.getEvents(), hasSize(2));
            assertThat(result.getEvents(), hasItems(shortDto1, shortDto2));

            mocked.verify(() -> EventMapper.mapToShortDto(eq(event1), same(initiator), eq(10L)));
            mocked.verify(() -> EventMapper.mapToShortDto(eq(event2), same(initiator), eq(5L)));
        }
    }

    @Test
    void toEntity_shouldMapDtoToEntityWithPinnedAndEvents() {
        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("New Compilation");
        dto.setPinned(true);
        dto.setEvents(Set.of(EVENT_ID_1, EVENT_ID_2));

        Compilation result = CompilationMapper.toEntity(dto, eventsSet);

        assertThat(result.getId(), nullValue());
        assertThat(result.getTitle(), is("New Compilation"));
        assertThat(result.getPinned(), is(true));
        assertThat(result.getEvents(), sameInstance(eventsSet));
    }

}