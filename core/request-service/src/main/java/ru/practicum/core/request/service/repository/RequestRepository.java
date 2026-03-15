package ru.practicum.core.request.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.core.api.util.enums.RequestStatus;
import ru.practicum.core.request.dto.EventRequestsCount;
import ru.practicum.core.request.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findByRequesterId(Long requesterId);

    Boolean existsByRequesterIdAndEventId(Long requesterId, Long eventId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long requesterId);

    List<ParticipationRequest> findAllByEventId(Long eventId);

    @Query("""
            SELECT COUNT(r)
            FROM ParticipationRequest r
            WHERE r.eventId = :id
              AND r.status = :status
            """)
    Long countByEventIdAndStatus(
            @Param("id") Long eventId,
            @Param("status") RequestStatus status);

    @Query("""
            SELECT NEW ru.practicum.core.request.dto.EventRequestsCount(r.eventId, COUNT(r.id))
            FROM ParticipationRequest r
            WHERE r.status = :status
              AND r.eventId IN :eventIds
            GROUP BY r.eventId
            ORDER BY COUNT(r.id) ASC
            """)
    List<EventRequestsCount> countByStatusForEvents(
            @Param("eventIds") List<Long> eventIds,
            @Param("status") RequestStatus status);
}
