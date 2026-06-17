package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.exception.ValidationException;
import ru.practicum.model.EndpointHitEntity;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatService {

    private final StatRepository statRepository;

    @Transactional
    public void saveHit(EndpointHit dto) {
        EndpointHitEntity entity = EndpointHitEntity.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();

        entity = statRepository.save(entity);
        log.info("New hit saved: {}", entity);
    }

    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, boolean unique) {
        if (end.isBefore(start)) {
            throw new ValidationException("End date must not be before start date");
        }

        List<EndpointHitEntity> hits = statRepository.findAllByTimestampBetween(start, end);

        return buildStats(hits, uris, unique);
    }

    private List<ViewStats> buildStats(List<EndpointHitEntity> hits,
                                       List<String> uris,
                                       boolean unique) {
        return hits.stream()
                .filter(hit -> uris == null || uris.isEmpty() || uris.contains(hit.getUri()))
                .collect(Collectors.groupingBy(
                        hit -> new GroupKey(hit.getApp(), hit.getUri()),
                        Collectors.mapping(EndpointHitEntity::getIp, Collectors.toList())
                ))
                .entrySet().stream()
                .map(entry -> new ViewStats(
                        entry.getKey().app(),
                        entry.getKey().uri(),
                        unique ? (long) entry.getValue().stream().distinct().count()
                                : (long) entry.getValue().size()
                ))
                .sorted(Comparator.comparingLong(ViewStats::getHits).reversed())
                .collect(Collectors.toList());
    }

    private record GroupKey(String app, String uri) {
    }
}