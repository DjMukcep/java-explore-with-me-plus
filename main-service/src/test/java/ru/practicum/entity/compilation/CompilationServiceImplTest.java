package ru.practicum.entity.compilation;

import com.querydsl.core.types.dsl.BooleanExpression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.practicum.StatClient;
import ru.practicum.ViewStats;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationsParamDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.entity.Location;
import ru.practicum.entity.category.Category;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventService;
import ru.practicum.entity.event.EventState;
import ru.practicum.entity.user.User;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompilationServiceImplTest {

    @Mock
    private CompilationRepository compilationRepository;
    @Mock
    private StatClient statClient;
    @Mock
    private EventService eventService;

    @InjectMocks
    private CompilationServiceImpl compilationService;

    private static final long COMP_ID_1 = 1L;
    private static final long COMP_ID_2 = 2L;
    private static final long EVENT_ID_1 = 101L;
    private static final long EVENT_ID_2 = 102L;
    private static final long USER_ID = 201L;
    private static final long CATEGORY_ID = 301L;

    private Compilation compilation1;
    private Compilation compilation2;
    private Event event;
    private List<Event> eventsList;

    @BeforeEach
    void setUp() {

        LocalDateTime createdOn = LocalDateTime.now();
        LocalDateTime eventDate = createdOn.plusDays(13);
        LocalDateTime publishedOn = createdOn.plusSeconds(13);


        User initiator = User.builder()
                .id(USER_ID)
                .name("test name")
                .mail("test@mail.com")
                .build();

        Category category = Category.builder()
                .id(CATEGORY_ID)
                .name("category name")
                .build();

        Location location = Location.builder()
                .lat(10.213f)
                .lon(124.123f)
                .build();

        event = Event.builder()
                .id(EVENT_ID_1)
                .annotation("annotation 1")
                .title("title 1")
                .description("desc 1")
                .initiator(initiator)
                .category(category)
                .location(location)
                .createdOn(createdOn)
                .eventDate(eventDate)
                .publishedOn(publishedOn)
                .requestModeration(true)
                .paid(true)
                .participantLimit(10)
                .state(EventState.PENDING)
                .build();


        Event event2 = Event.builder()
                .id(EVENT_ID_2)
                .annotation("annotation 2")
                .title("title 2")
                .description("desc 2")
                .initiator(initiator)
                .category(category)
                .location(location)
                .createdOn(createdOn.plusSeconds(3))
                .eventDate(eventDate.plusSeconds(3))
                .publishedOn(publishedOn.plusSeconds(3))
                .requestModeration(true)
                .paid(true)
                .participantLimit(10)
                .state(EventState.PENDING)
                .build();

        eventsList = List.of(event, event2);
        Set<Event> eventsSet = new HashSet<>(eventsList);

        compilation1 = new Compilation();
        compilation1.setId(COMP_ID_1);
        compilation1.setTitle("Compilation 1");
        compilation1.setPinned(true);
        compilation1.setEvents(eventsSet);

        compilation2 = new Compilation();
        compilation2.setId(COMP_ID_2);
        compilation2.setTitle("Compilation 2");
        compilation2.setPinned(false);
        compilation2.setEvents(eventsSet);
    }

    @Test
    void getAll_whenPinnedIsNull_returnsAllCompilationsWithStats() {
        CompilationsParamDto params = new CompilationsParamDto();
        params.setFrom(0);
        params.setSize(10);

        List<Compilation> pageContent = List.of(compilation1, compilation2);
        when(compilationRepository.findAll(ArgumentMatchers.any(PageRequest.class)))
                .thenReturn(new PageImpl<>(pageContent));

        List<ViewStats> viewStats = prepareViewStatsForEvents(List.of(EVENT_ID_1, EVENT_ID_2));
        when(statClient.getViewStats(any()))
                .thenReturn(viewStats);

        List<CompilationDto> result = compilationService.getAll(params);

        assertThat(result, hasSize(2));
        assertThat(result.get(0).getId(), is(COMP_ID_1));
        assertThat(result.get(1).getId(), is(COMP_ID_2));

        verify(compilationRepository).findAll(ArgumentMatchers.any(PageRequest.class));
        verify(statClient).getViewStats(any());
        verifyNoMoreInteractions(eventService);
    }

    @Test
    void getAll_whenPinnedIsTrue_filtersByPinned() {
        CompilationsParamDto params = new CompilationsParamDto();
        params.setFrom(0);
        params.setSize(5);
        params.setPinned(true);

        List<Compilation> pageContent = List.of(compilation1);
        PageRequest pageRequest = PageRequest.of(0, 5);
        when(compilationRepository.findAll(ArgumentMatchers.any(BooleanExpression.class), eq(pageRequest)))
                .thenReturn(new PageImpl<>(pageContent, pageRequest, pageContent.size()));

        List<ViewStats> viewStats = prepareViewStatsForEvents(List.of(EVENT_ID_1));
        when(statClient.getViewStats(any())).thenReturn(viewStats);

        List<CompilationDto> result = compilationService.getAll(params);

        assertThat(result, hasSize(1));
        assertThat(result.get(0).getId(), is(COMP_ID_1));

        verify(compilationRepository).findAll(ArgumentMatchers.any(BooleanExpression.class), eq(pageRequest));
        verify(statClient).getViewStats(any());
    }

    @Test
    void getById_whenExists_returnsDtoWithStats() {
        when(compilationRepository.findById(COMP_ID_1)).thenReturn(Optional.of(compilation1));

        List<ViewStats> viewStats = prepareViewStatsForEvents(List.of(EVENT_ID_1, EVENT_ID_2));
        when(statClient.getViewStats(any())).thenReturn(viewStats);

        CompilationDto result = compilationService.getById(COMP_ID_1);

        assertThat(result.getId(), is(COMP_ID_1));
        assertThat(result.getTitle(), is("Compilation 1"));
        assertThat(result.getPinned(), is(true));

        verify(compilationRepository).findById(COMP_ID_1);
        verify(statClient).getViewStats(any());
    }

    @Test
    void getById_whenNotExists_throwsNotFoundException() {
        long nonExistingId = 999L;
        when(compilationRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> compilationService.getById(nonExistingId)
        );

        verify(compilationRepository).findById(nonExistingId);
        verifyNoInteractions(statClient, eventService);
    }

    @Test
    void create_successfullyCreatesCompilationAndReturnsDtoWithStats() {
        NewCompilationDto dto = new NewCompilationDto();
        dto.setTitle("New Compilation");
        dto.setPinned(false);
        dto.setEvents(Set.of(EVENT_ID_1, EVENT_ID_2));

        when(eventService.getByIds(any())).thenReturn(eventsList);
        when(compilationRepository.save(any())).thenAnswer(invocation -> {
            Compilation c = invocation.getArgument(0);
            c.setId(COMP_ID_1);
            return c;
        });

        List<ViewStats> viewStats = prepareViewStatsForEvents(new ArrayList<>(dto.getEvents()));
        when(statClient.getViewStats(any())).thenReturn(viewStats);

        CompilationDto result = compilationService.create(dto);

        assertThat(result.getTitle(), is("New Compilation"));
        assertThat(result.getPinned(), is(false));
        assertThat(result.getId(), is(COMP_ID_1));

        verify(eventService).getByIds(any());
        verify(compilationRepository).save(any());
        verify(statClient).getViewStats(any());
    }

    @Test
    void delete_whenExists_deletesCompilation() {
        when(compilationRepository.findById(COMP_ID_1)).thenReturn(Optional.of(compilation1));

        compilationService.delete(COMP_ID_1);

        verify(compilationRepository).findById(COMP_ID_1);
        verify(compilationRepository).deleteById(COMP_ID_1);
        verifyNoMoreInteractions(statClient, eventService);
    }

    @Test
    void delete_whenNotExists_throwsNotFoundException() {
        long nonExistingId = 999L;
        when(compilationRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> compilationService.delete(nonExistingId)
        );

        verify(compilationRepository).findById(nonExistingId);
    }

    @Test
    void update_changesTitleAndPinnedAndReturnsDtoWithStats() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("Updated Title");
        request.setPinned(true);
        request.setEvents(Set.of(EVENT_ID_1));

        compilation1.setTitle(request.getTitle());
        compilation1.setPinned(request.getPinned());
        compilation1.setEvents(Set.of(event));

        when(compilationRepository.findById(COMP_ID_1)).
                thenReturn(Optional.of(compilation1));

        List<ViewStats> viewStats = prepareViewStatsForEvents(new ArrayList<>(request.getEvents()));
        when(statClient.getViewStats(any())).thenReturn(viewStats);

        CompilationDto result = compilationService.update(COMP_ID_1, request);

        assertThat(result.getTitle(), is("Updated Title"));
        assertThat(result.getPinned(), is(true));
        assertThat(result.getId(), is(COMP_ID_1));

        verify(compilationRepository).findById(COMP_ID_1);
        verify(compilationRepository).save(eq(compilation1));
        verify(statClient).getViewStats(any());
    }

    @Test
    void update_whenTitleIsBlank_throwsValidationException() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("");

        when(compilationRepository.findById(COMP_ID_1)).thenReturn(Optional.of(compilation1));

        assertThrows(
                ValidationException.class,
                () -> compilationService.update(COMP_ID_1, request)
        );

        verify(compilationRepository).findById(COMP_ID_1);
        verifyNoInteractions(eventService, statClient);
    }

    @Test
    void update_whenTitleTooLong_throwsValidationException() {
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setTitle("A".repeat(51)); // больше 50 символов

        when(compilationRepository.findById(COMP_ID_1)).thenReturn(Optional.of(compilation1));

        assertThrows(
                ValidationException.class,
                () -> compilationService.update(COMP_ID_1, request)
        );

        verify(compilationRepository).findById(COMP_ID_1);
        verifyNoInteractions(eventService, statClient);
    }

    @Test
    void update_whenEventsNotChanged_skipsFetchingEvents() {
        Set<Long> sameEventIds = new HashSet<>(List.of(EVENT_ID_1, EVENT_ID_2));
        UpdateCompilationRequest request = new UpdateCompilationRequest();
        request.setEvents(sameEventIds);

        when(compilationRepository.findById(COMP_ID_1)).thenReturn(Optional.of(compilation1));

        compilationService.update(COMP_ID_1, request);

        verify(compilationRepository).findById(COMP_ID_1);
        verifyNoInteractions(eventService);
        verify(compilationRepository).save(eq(compilation1));
    }

    private List<ViewStats> prepareViewStatsForEvents(List<Long> eventIds) {
        return eventIds.stream()
                .map(id -> {
                    ViewStats vs = new ViewStats();
                    vs.setUri("/events/" + id);
                    vs.setHits(10L);
                    return vs;
                })
                .collect(Collectors.toList());
    }
}