package ru.practicum.entity.comment;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = "author")
    Optional<Comment> findWithAuthorById(Long id);

    @EntityGraph(attributePaths = "author")
    List<Comment> findAllByAuthorId(Long id);
}
