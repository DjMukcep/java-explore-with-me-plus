package ru.practicum.entity.request;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.dto.event.EventRequestsCountDto;

import java.util.List;

public interface RequestRepository extends JpaRepository<Request, Long> {

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    long countByEventIdAndStatus(Long eventId, RequestStatus status);

    List<Request> findByUserId(Long userId);

    List<Request> findByIdIn(List<Long> ids);

    List<Request> findByEventId(Long eventId);


    @Query("SELECT new ru.practicum.dto.event.EventRequestsCountDto(r.event.id, COUNT(r.id)) " +
            "FROM Request r " +
            "WHERE r.event.id IN :ids AND r.status = :status " +
            "GROUP BY r.event.id")
    List<EventRequestsCountDto> countByEventIdInAndStatus(@Param("ids") List<Long> ids,
                                                          @Param("status") RequestStatus status);
}
