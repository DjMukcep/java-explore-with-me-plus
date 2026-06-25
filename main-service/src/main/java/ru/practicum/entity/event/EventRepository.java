package ru.practicum.entity.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    Optional<Event> findByIdAndState(Long id, EventState state);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);
}