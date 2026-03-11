package ru.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.practicum.dto.enums.RequestStatus;

import java.time.LocalDateTime;

import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "participation_requests", schema = "public")
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    Long id;

    @Builder.Default
    LocalDateTime created = LocalDateTime.now();

    @Column(name = "event_id")
    Long eventId;

    @Column(name = "requester_id")
    Long requesterId;

    @Enumerated(value = EnumType.STRING)
    RequestStatus status;

}