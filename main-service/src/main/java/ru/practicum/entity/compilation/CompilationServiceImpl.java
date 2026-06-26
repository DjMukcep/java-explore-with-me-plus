package ru.practicum.entity.compilation;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ru.practicum.StatClient;
import ru.practicum.StatsRequest;
import ru.practicum.ViewStats;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationsParamDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.entity.compilation.QCompilation;
import ru.practicum.entity.event.Event;
import ru.practicum.entity.event.EventService;
import ru.practicum.exception.NotFoundException;
import ru.practicum.exception.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CompilationRepository compilationRepository;
    private final StatClient statClient;
    private final EventService eventService;

    @Override
    public List<CompilationDto> getAll(CompilationsParamDto dto) {

        int pageNumber = dto.getFrom() / dto.getSize();
        PageRequest pageRequest = PageRequest.of(pageNumber, dto.getSize());
        List<Compilation> compilations;
        if (dto.getPinned() == null) {
            compilations = compilationRepository.findAll(pageRequest).getContent();
        } else {
            BooleanExpression condition = QCompilation.compilation.pinned.eq(dto.getPinned());
            compilations = compilationRepository.findAll(condition, pageRequest).getContent();
        }

        List<Long> eventIds = compilations.stream()
                .flatMap(compilation -> compilation.getEvents().stream())
                .distinct()
                .map(Event::getId)
                .toList();

        List<ViewStats> viewStats = makeRequestToStatsService(eventIds);
        Map<Long, Long> hitsMap = getHitsByEventIds(viewStats);

        return compilations.stream()
                .map(compilation -> CompilationMapper.toDto(compilation, hitsMap))
                .toList();
    }

    @Override
    public CompilationDto getById(Long compId) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException(String.format("Подборка с id=%d не найдена", compId))
        );
        List<ViewStats> viewStats = makeRequestToStatsService(List.of(compId));
        Map<Long, Long> hitsByEventIds = getHitsByEventIds(viewStats);
        return CompilationMapper.toDto(compilation, hitsByEventIds);
    }

    @Override
    public CompilationDto create(NewCompilationDto dto) {

        List<Event> events = eventService.getByIds(dto.getEvents());
        Compilation entity = CompilationMapper.toEntity(dto, new HashSet<>(events));
        entity = compilationRepository.save(entity);
        log.info("created compilation: {}", entity);

        List<ViewStats> viewStats = makeRequestToStatsService(dto.getEvents());
        return CompilationMapper.toDto(entity, getHitsByEventIds(viewStats));
    }

    @Override
    public void delete(Long compId) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException(String.format("Подборка с id=%d не найдена", compId))
        );
        compilationRepository.deleteById(compId);
        log.info("deleted compilation: {}", compilation);
    }

    @Override
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException(String.format("Подборка с id=%d не найдена", compId))
        );
        updateFields(compilation, request);
        compilationRepository.save(compilation);
        log.info("updated compilation: {}", compilation);

        List<Long> ids = compilation.getEvents().stream()
                .map(Event::getId)
                .toList();

        List<ViewStats> viewStats = makeRequestToStatsService(ids);
        return CompilationMapper.toDto(compilation, getHitsByEventIds(viewStats));
    }

    private Map<Long, Long> getHitsByEventIds(List<ViewStats> stats) {
        Map<Long, Long> hitsMap = new HashMap<>();
        for (ViewStats stat : stats) {
            String uri = stat.getUri();
            int lastSlashIndex = stat.getUri().lastIndexOf('/');
            if (lastSlashIndex == -1 || lastSlashIndex == uri.length() - 1) {
                continue;
            }
            long eventId = Long.parseLong(uri.substring(lastSlashIndex + 1));
            hitsMap.merge(eventId, stat.getHits(), Long::sum);
        }
        return hitsMap;
    }

    private List<ViewStats> makeRequestToStatsService(Collection<Long> eventIds) {
        List<String> uris = eventIds.stream()
                .map(id -> "/events/" + id)
                .toList();

        LocalDateTime end = LocalDateTime.of(9999, 12, 31, 23, 59, 59);

        StatsRequest request = new StatsRequest(
                LocalDateTime.now().format(FORMATTER),
                end.format(FORMATTER),
                uris,
                false
        );

        return statClient.getViewStats(request);
    }

    private void updateFields(Compilation old, UpdateCompilationRequest request) {

        if (request.getTitle() != null) {
            if (request.getTitle().isBlank()) {
                throw new ValidationException("Title can't be empty!");
            }

            if (request.getTitle().length() > 50) {
                throw new ValidationException("Title length should be <50");
            }

            if (!old.getTitle().equals(request.getTitle())) {
                old.setTitle(request.getTitle());
            }
        }

        if (request.getPinned() != null && !old.getPinned().equals(request.getPinned())) {
            old.setPinned(request.getPinned());
        }

        Set<Long> oldIds = old.getEvents().stream()
                .map(Event::getId)
                .collect(Collectors.toUnmodifiableSet());

        if (request.getEvents() != null && !oldIds.equals(request.getEvents())) {
            List<Event> newEvents = eventService.getByIds(request.getEvents());
            old.setEvents(new HashSet<>(newEvents));
        }
    }
}
