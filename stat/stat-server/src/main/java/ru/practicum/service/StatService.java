package ru.practicum.service;

import ru.practicum.EndpointHit;
import ru.practicum.ViewStats;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    void saveHit(EndpointHit dto);

    List<ViewStats> getStats(LocalDateTime start, LocalDateTime end,
                             List<String> uris, boolean unique);
}