package ru.practicum.recomm.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "user_actions",
        schema = "public",
        indexes = {
                @Index(name = "idx_user_event",
                        columnList = "user_id, event_id",
                        unique = true)
        })
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserAction {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "score", nullable = false)
    private Double score;

    @Column(name = "interact_at", nullable = false)
    private LocalDateTime interactAt;
}