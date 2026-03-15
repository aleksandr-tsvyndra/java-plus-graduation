package ru.practicum.recomm.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.recomm.analyzer.model.EventSimilarity;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    Optional<EventSimilarity> findBySourceEventIdAndTargetEventId(Long sourceEventId, Long targetEventId);

    @Query("""
            SELECT es
            FROM EventSimilarity es
            WHERE es.sourceEventId = :sourceEventId OR es.targetEventId = :targetEventId
            """)
    List<EventSimilarity> findAllBySourceEventIdOrTargetEventId(Long sourceEventId, Long targetEventId);

    @Query("""
            SELECT DISTINCT es
            FROM EventSimilarity es
            WHERE es.sourceEventId IN :sourceEventIds OR es.targetEventId IN :targetEventIds
            """)
    List<EventSimilarity> findAllBySourceEventIdInOrTargetEventIdIn(Set<Long> sourceEventIds, Set<Long> targetEventIds);
}