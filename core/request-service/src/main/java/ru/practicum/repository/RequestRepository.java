package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.model.Request;

import java.util.List;
import java.util.Set;

public interface RequestRepository extends JpaRepository<Request, Long> {

    List<Request> findAllByRequesterId(Long userId);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByIdIn(Set<Long> requestIds);

    boolean existsByEventIdAndRequesterId(Long eventId, Long userId);
}
