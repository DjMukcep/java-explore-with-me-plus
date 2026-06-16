package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;
import ru.practicum.model.EndpointHitEntity;
import ru.practicum.repository.StatRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatService {

    private final StatRepository statRepository;

    @Transactional
    public void saveHit(EndpointHit dto) {
        log.warn("Saving hit: app={}, uri={}, ip={}, timestamp={}",
                dto.getApp(), dto.getUri(), dto.getIp(), dto.getTimestamp());

        EndpointHitEntity entity = EndpointHitEntity.builder()
                .app(dto.getApp())
                .uri(dto.getUri())
                .ip(dto.getIp())
                .timestamp(dto.getTimestamp())
                .build();

        statRepository.save(entity);
        log.warn("Hit saved successfully");
    }

    @Transactional(readOnly = true)
    public List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                                    List<String> uris, boolean unique) {
        log.warn("Getting stats: start={}, end={}, uris={}, unique={}",
                start, end, uris, unique);

        List<ViewStats> stats;
        if (unique) {
            stats = statRepository.findStatsUnique(start, end, uris);
        } else {
            stats = statRepository.findStats(start, end, uris);
        }

        log.warn("Stats retrieved: {} records", stats.size());
        return stats;
    }
}