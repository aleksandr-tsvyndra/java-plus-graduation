package ru.practicum.recomm.analyzer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "event_similarity", schema = "public")
@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class EventSimilarity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(name = "source_event_id")
    private Long sourceEventId;

    @Column(name = "target_event_id")
    private Long targetEventId;

    @Column(name = "similarity_score")
    private Double similarityScore;

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt;
}