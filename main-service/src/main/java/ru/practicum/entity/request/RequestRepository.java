package ru.practicum.entity.request;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    long countByEvent_IdAndStatus(Long eventId,RequestStatus status);

    List<Request> findByUserId(Long userId);
}
