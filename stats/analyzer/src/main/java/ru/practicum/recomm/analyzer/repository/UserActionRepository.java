package ru.practicum.recomm.analyzer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.recomm.analyzer.model.UserAction;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserActionRepository extends JpaRepository<UserAction, Long> {

    List<UserAction> findByUserId(Long userId);

    List<UserAction> findByEventIdIn(List<Long> eventIds);

    List<UserAction> findByEventId(Long eventId);

    Optional<UserAction> findByUserIdAndEventId(Long userId, Long eventId);
}