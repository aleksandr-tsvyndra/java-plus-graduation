package ru.practicum.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.KafkaEventSimilarityProducer;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EventSimilarityServiceImpl implements EventSimilarityService {
    private final Map<Long, Map<Long, Double>> eventWeight = new HashMap<>();
    private final Map<Long, Double> eventTotalWeight = new HashMap<>();
    private final Map<Long, Map<Long, Double>> eventMinPairWeight = new HashMap<>();

    private static final double VIEW_WEIGHT = 0.4;
    private static final double REGISTER_WEIGHT = 0.8;
    private static final double LIKE_WEIGHT = 1.0;

    @Override
    public List<EventSimilarityAvro> updateEventSimilarity(UserActionAvro userAction) {
        Long eventA = userAction.getEventId();
        Long userId = userAction.getUserId();
        double newActionWeight = getWeightByActionType(userAction.getActionType());

        Map<Long, Double> userWeight = eventWeight.computeIfAbsent(eventA, e -> new HashMap<>());
        double oldActionWeight = userWeight.getOrDefault(userId, 0.0);

        if (newActionWeight > oldActionWeight) {
            userWeight.put(userId, newActionWeight);
            updateEventTotalWeight(eventA, oldActionWeight, newActionWeight);

            List<EventSimilarityAvro> eventSimilarityAvros = new ArrayList<>();
            for (var entry : eventWeight.entrySet()) {
                Long eventB = entry.getKey();
                if (!eventA.equals(eventB) && entry.getValue().containsKey(userId)) {
                    updateMinPairWeight(eventA, eventB, newActionWeight, oldActionWeight, userId);
                    double similarityRatio = calculateSimilarityRatio(eventA, eventB);
                    eventSimilarityAvros.add(buildEventSimilarityAvro(eventA, eventB, similarityRatio, userAction.getTimestamp()));
                }
            }
            return eventSimilarityAvros;
        }
        return List.of();
    }

    private double getWeightByActionType(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> VIEW_WEIGHT;
            case REGISTER -> REGISTER_WEIGHT;
            case LIKE -> LIKE_WEIGHT;
        };
    }

    private void updateMinPairWeight(long eventA, long eventB, double newActionWeight, double oldActionWeight, long userId) {
        log.info("Обновляем сумму минимальных весов для мероприятий с id={} и id={}", eventA, eventB);
        double eventBWeight = eventWeight.get(eventB).get(userId);

        double oldMinWeight = Math.min(oldActionWeight, eventBWeight);
        double newMinWeight = Math.min(newActionWeight, eventBWeight);

        if (oldMinWeight != newMinWeight) {
            double newMinPairWeight = getMinPairWeight(eventA, eventB) - oldMinWeight + newMinWeight;
            putMinPairWeight(eventA, eventB, newMinPairWeight);
            log.info("Сумма минимальных весов успешно обновилась. Новая сумма = {}", newMinPairWeight);
        }
    }

    private void updateEventTotalWeight(long eventId, double oldActionWeight, double newActionWeight) {
        log.info("Обновляем частную сумму весов мероприятия с id={}", eventId);
        double oldTotalWeight = eventTotalWeight.getOrDefault(eventId, 0.0);

        log.info("Старая частная сумма весов = {}", oldTotalWeight);
        double newTotalWeight = oldTotalWeight - oldActionWeight + newActionWeight;

        eventTotalWeight.put(eventId, newTotalWeight);
        log.info("Новая частная сумма весов после обновления = {}", newTotalWeight);
    }

    private double getMinPairWeight(long eventA, long eventB) {
        long first  = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return eventMinPairWeight
                .computeIfAbsent(first, e -> new HashMap<>())
                .getOrDefault(second, 0.0);
    }

    private void putMinPairWeight(long eventA, long eventB, double sum) {
        long first  = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        eventMinPairWeight
                .computeIfAbsent(first, e -> new HashMap<>())
                .put(second, sum);
    }

    private double calculateSimilarityRatio(long eventA, long eventB) {
        log.info("Вычисляем коэффициент сходства двух мероприятий с id: {} и {}", eventA, eventB);
        double minPairWeight = getMinPairWeight(eventA, eventB);
        double eventATotalWeight = eventTotalWeight.get(eventA);
        double eventBTotalWeight = eventTotalWeight.get(eventB);

        return minPairWeight / (Math.sqrt(eventATotalWeight) * Math.sqrt(eventBTotalWeight));
    }

    private EventSimilarityAvro buildEventSimilarityAvro(long eventA, long eventB, double score, Instant timestamp) {
        long first  = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return EventSimilarityAvro.newBuilder()
                .setEventA(first)
                .setEventB(second)
                .setScore(score)
                .setTimestamp(timestamp)
                .build();
    }
}
