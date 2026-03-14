package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.Similarity;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SimilarityRepository extends JpaRepository<Similarity, Long> {

    @Query("SELECT s FROM Similarity s WHERE s.eventId1 = :eventId OR s.eventId2 = :eventId")
    List<Similarity> findByEventId1OrEventId2(@Param("eventId") Long eventId);

    List<Similarity> findTopByEventId1InOrEventId2InOrderBySimilarityDesc(Set<Long> eventIds1,
                                                                      Set<Long> eventIds2,
                                                                      Pageable pageable);

    Optional<Similarity> findByEventId1AndEventId2(Long eventIdA, Long eventIdB);

}
