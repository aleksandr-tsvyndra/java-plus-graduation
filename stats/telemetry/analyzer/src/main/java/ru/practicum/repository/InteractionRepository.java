package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Interaction;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    List<Interaction> findAllByEventIdIn(Set<Long> eventIds);

    List<Long> findDistinctEventIdByUserIdAndEventIdIn(Long userId, Set<Long> eventIds);

    List<Long> findDistinctEventIdByUserIdOrderByTimestampDesc(Long userId, Pageable pageable);

    Optional<Interaction> findByUserIdAndEventId(Long userId, Long eventId);

}
