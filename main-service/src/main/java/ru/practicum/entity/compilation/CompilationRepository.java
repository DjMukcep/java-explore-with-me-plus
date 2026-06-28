package ru.practicum.entity.compilation;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.Optional;


public interface CompilationRepository extends JpaRepository<Compilation, Long>,
        QuerydslPredicateExecutor<Compilation> {

    boolean existsByTitle(String title);

    @EntityGraph(attributePaths = "events")
    Optional<Compilation> findWithEventsById(Long id);
}
