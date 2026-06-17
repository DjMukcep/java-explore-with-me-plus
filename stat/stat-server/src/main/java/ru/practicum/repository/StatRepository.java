package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.EndpointHitEntity;

import java.time.LocalDateTime;
import java.util.List;

public interface StatRepository extends JpaRepository<EndpointHitEntity, Long> {

    List<EndpointHitEntity> findAllByTimestampBetween(LocalDateTime start, LocalDateTime end);
}