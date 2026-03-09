package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import ru.practicum.dto.enums.EventState;

import java.time.LocalDateTime;

@Entity
@Table(name = "events", schema = "public")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    Long id;

    @Column(name = "annotation", length = 2000)
    String annotation;

    @Column(name = "category_id", nullable = false)
    Long categoryId;

    @Transient
    Long views;

    @Column(name = "confirmed_requests")
    Integer confirmedRequests;

    @Column(name = "created_on", nullable = false)
    LocalDateTime createdOn;

    @Column(name = "published_on")
    LocalDateTime publishedOn;

    @Column(name = "description", length = 7000)
    String description;

    @Column(name = "event_date")
    LocalDateTime eventDate;

    @Column(name = "initiator_id", nullable = false)
    Long initiatorId;

    @Embedded
    Location location;

    Boolean paid;

    @Column(name = "participant_limit")
    Integer participantLimit;

    @Enumerated(value = EnumType.STRING)
    EventState state;

    @Column(name = "request_moderation")
    Boolean requestModeration;

    String title;
}

