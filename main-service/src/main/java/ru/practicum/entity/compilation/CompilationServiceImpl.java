package ru.practicum.entity.compilation;

import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

        if (compilations.isEmpty()) {
            return List.of();
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
                () -> new NotFoundException(String.format("Compilation with id=%d not found", compId))
        );

        List<Long> eventIds = compilation.getEvents().stream()
                .map(Event::getId)
                .toList();

        List<ViewStats> viewStats = makeRequestToStatsService(eventIds);
        Map<Long, Long> hitsByEventIds = getHitsByEventIds(viewStats);
        return CompilationMapper.toDto(compilation, hitsByEventIds);
    }

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {

        if (compilationRepository.existsByTitle(dto.getTitle())) {
            throw new ConflictException("Compilation with title " + dto.getTitle() + " already exists");
        }

        List<Event> events = eventService.getByIds(dto.getEvents());
        Compilation entity = CompilationMapper.toEntity(dto, new HashSet<>(events));
        entity = compilationRepository.save(entity);

        List<ViewStats> viewStats = makeRequestToStatsService(dto.getEvents());
        var compilationDto = CompilationMapper.toDto(entity, getHitsByEventIds(viewStats));
        log.info("Новая подборка событий: {}", CompilationMapper.toLog(compilationDto));

        return compilationDto;
    }

    @Override
    @Transactional
    public void delete(Long compId) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException(String.format("Compilation with id=%d not found", compId))
        );
        compilationRepository.delete(compilation);
        log.info("Удалена подборка событий id: {}", compilation.getId());
    }

    @Override
    @Transactional
    public CompilationDto update(Long compId, UpdateCompilationRequest request) {

        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new NotFoundException(String.format("Compilation with id=%d not found", compId))
        );
        updateFields(compilation, request);

        List<Long> ids = compilation.getEvents().stream()
                .map(Event::getId)
                .toList();

        List<ViewStats> viewStats = makeRequestToStatsService(ids);
        var compilationDto = CompilationMapper.toDto(compilation, getHitsByEventIds(viewStats));
        log.info("Подборка событий обновлена: {}", CompilationMapper.toLog(compilationDto));

        return compilationDto;
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
            if (!old.getTitle().equals(request.getTitle())) {
                if (compilationRepository.existsByTitle(request.getTitle())) {
                    throw new ConflictException(String.format("Compilation '%s' already exists", request.getTitle()));
                }

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
