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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatServiceImpl implements StatService {

    private final StatRepository statRepository;

    @Override
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

    @Override
    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, boolean unique) {
        if (end.isBefore(start)) {
            throw new ValidationException("End date must not be before start date");
        }

        List<EndpointHitEntity> hits = statRepository.findAllByTimestampBetween(start, end);

        List<EndpointHitEntity> filteredHits = filterByUris(hits, uris);

        Map<GroupKey, List<String>> groupedHits = groupByAppAndUri(filteredHits);

        return mapToViewStats(groupedHits, unique);
    }

    private List<EndpointHitEntity> filterByUris(List<EndpointHitEntity> hits, List<String> uris) {
        if (uris == null || uris.isEmpty()) {
            return hits;
        }
        return hits.stream()
                .filter(hit -> uris.stream()
                        .anyMatch(uri -> hit.getUri().startsWith(uri)))
                .toList();
    }

    private Map<GroupKey, List<String>> groupByAppAndUri(List<EndpointHitEntity> hits) {
        return hits.stream()
                .collect(Collectors.groupingBy(
                        hit -> new GroupKey(hit.getApp(), hit.getUri()),
                        Collectors.mapping(EndpointHitEntity::getIp, Collectors.toList())
                ));
    }

    private List<ViewStats> mapToViewStats(Map<GroupKey, List<String>> groupedHits, boolean unique) {
        return groupedHits.entrySet().stream()
                .map(entry -> new ViewStats(
                        entry.getKey().app(),
                        entry.getKey().uri(),
                        unique ? entry.getValue().stream().distinct().count()
                                : entry.getValue().size()
                ))
                .sorted(Comparator.comparingLong(ViewStats::getHits).reversed())
                .collect(Collectors.toList());
    }

    private record GroupKey(String app, String uri) {
    }
}