package ru.practicum.service.event;

import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventAdminRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    List<EventFullDto> getEventsForAdmin(List<Long> users, List<String> states,
                                         List<Long> categories, LocalDateTime rangeStart,
                                         LocalDateTime rangeEnd, Pageable pageable);

    EventFullDto updateEventByAdmin(Long eventId, UpdateEventAdminRequest updateRequest);
}