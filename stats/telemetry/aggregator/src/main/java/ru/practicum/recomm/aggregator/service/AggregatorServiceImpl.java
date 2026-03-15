package ru.practicum.recomm.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.practicum.recommendations.avro.ActionTypeAvro;
import ru.practicum.recommendations.avro.EventSimilarityAvro;
import ru.practicum.recommendations.avro.UserActionAvro;
import ru.practicum.recomm.aggregator.kafka.config.KafkaTopics;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class AggregatorServiceImpl implements AggregatorService {

    private final KafkaTemplate<String, EventSimilarityAvro> kafkaTemplate;

    private final KafkaTopics kafkaTopics;

    private final Map<Long, Map<Long, Double>> weightedUserActionsMatrix = new ConcurrentHashMap<>();

    private final Map<Long, Double> totalWeights = new ConcurrentHashMap<>();

    private final Map<Long, Map<Long, Double>> minWeightsSums = new ConcurrentHashMap<>();

    @Override
    public void processAction(UserActionAvro actionAvro) {
        log.info("Получено действие пользователя: {}", actionAvro);
        List<EventSimilarityAvro> similarityList = updateSimilarities(actionAvro);
        if (!similarityList.isEmpty()) {
            log.info("Было рассчитано {} коэффициентов схожести", similarityList.size());
            sendSimilarities(similarityList);
        }
    }

    public List<EventSimilarityAvro> updateSimilarities(UserActionAvro actionAvro) {
        Double weight = getActionWeight(actionAvro.getActionType());
        Long userId = actionAvro.getUserId();
        Long eventId = actionAvro.getEventId();

        if (!weightedUserActionsMatrix.containsKey(eventId)) {
            log.info("Это первое взаимодействие с мероприятием ID={}", eventId);
            return calculateNewSimilarities(eventId, userId, weight);
        }

        Map<Long, Double> itemWeights = weightedUserActionsMatrix.get(eventId);
        double oldWeight = itemWeights.getOrDefault(userId, 0.0);
        double newWeight = Math.max(oldWeight, weight);

        List<EventSimilarityAvro> similarities = new ArrayList<>();

        if (newWeight != oldWeight) {
            log.info("Получена новая оценка '{}' пользователя ID={} для мероприятия ID={}", weight, userId, eventId);
            itemWeights.put(userId, newWeight);
            double eventAWeightDelta = newWeight - oldWeight;
            totalWeights.merge(eventId, eventAWeightDelta, Double::sum);

            for (Long otherEventId : weightedUserActionsMatrix.keySet()) {
                if (otherEventId.equals(eventId)) {
                    continue;
                }

                Optional<EventSimilarityAvro> similarity = updateSums(userId, eventId, otherEventId, oldWeight, newWeight);
                similarity.ifPresent(similarities::add);
            }
        }
        return similarities;
    }

    private Optional<EventSimilarityAvro> updateSums(
            Long userId,
            Long eventA,
            Long eventB,
            Double eventAOldWeight,
            Double eventANewWeight
    ) {
        Double eventBWeight = weightedUserActionsMatrix.get(eventB).getOrDefault(userId, 0.0);
        if (eventBWeight == 0.0) {
            // Пользователь не взаимодействовал с мероприятием
            return Optional.empty();
        } else {
            // Рассчитываем изменение минимального веса между событиями A и B
            double oldMinAB = Math.min(eventAOldWeight, eventBWeight);
            double newMinAB = Math.min(eventANewWeight, eventBWeight);
            double minABDelta = newMinAB - oldMinAB;

            // Обновляем сумму минимальных весов для пары событий
            double updatedMinWeightsSum = get(eventA, eventB) + minABDelta;
            put(eventA, eventB, updatedMinWeightsSum);

            // Получаем обновлённую сумму минимальных весов
            double minWeightsSum = get(eventA, eventB);

            // Вычисляем нормы (корни из сумм квадратов весов)
            double norm1 = Math.sqrt(totalWeights.getOrDefault(eventA, 0.0));
            double norm2 = Math.sqrt(totalWeights.getOrDefault(eventB, 0.0));

            if (norm1 == 0 || norm2 == 0) {
                return Optional.empty();
            }

            // Вычисляем схожесть по формуле косинусного сходства
            double similarity = minWeightsSum / (norm1 * norm2);

            // Возвращаем результат в виде Avro-объекта
            return Optional.of(createSimilarityAvro(eventA, eventB, similarity));
        }
    }

    private List<EventSimilarityAvro> calculateNewSimilarities(Long eventA, Long user, Double weightA) {
        weightedUserActionsMatrix.computeIfAbsent(eventA, k -> new HashMap<>(Map.of(user, weightA)));
        totalWeights.put(eventA, weightA);
        List<EventSimilarityAvro> similarities = new ArrayList<>();

        for (Map.Entry<Long, Map<Long, Double>> entry : weightedUserActionsMatrix.entrySet()) {
            Long eventB = entry.getKey();
            if (eventB.equals(eventA)) {
                continue; // Пропускаем сравнение с самим собой
            }

            Double weightB = entry.getValue().getOrDefault(user, 0.0);
            if (weightB == 0.0) {
                continue; // Пропускаем события без взаимодействия пользователя
            }

            double minWeight = Math.min(weightA, weightB);
            put(eventA, eventB, minWeight);

            double norm1 = totalWeights.getOrDefault(eventA, 0.0);
            double norm2 = totalWeights.getOrDefault(eventB, 0.0);

            if (norm1 == 0 || norm2 == 0) {
                continue; // Избегаем деления на ноль
            }

            double similarity = minWeight / (Math.sqrt(norm1) * Math.sqrt(norm2));

            similarities.add(createSimilarityAvro(eventA, eventB, similarity));
        }
        return similarities;
    }

    private double get(long eventA, long eventB) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        return minWeightsSums.computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .getOrDefault(second, 0.0);
    }

    private void put(long eventA, long eventB, double sum) {
        long first = Math.min(eventA, eventB);
        long second = Math.max(eventA, eventB);

        minWeightsSums.computeIfAbsent(first, k -> new ConcurrentHashMap<>())
                .put(second, sum);
    }

    private double getActionWeight(ActionTypeAvro type) {
        return switch (type) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> throw new IllegalArgumentException("Неизвестный тип действия: " + type);
        };
    }

    private EventSimilarityAvro createSimilarityAvro(Long eventA, Long eventB, double similarityScore) {
        return EventSimilarityAvro.newBuilder()
                .setEventA(Math.min(eventA, eventB))
                .setEventB(Math.max(eventA, eventB))
                .setScore(similarityScore)
                .setTimestamp(Instant.now())
                .build();
    }

    private void sendSimilarities(List<EventSimilarityAvro> similarityList) {
        for (EventSimilarityAvro similarity : similarityList) {
            try {
                kafkaTemplate.send(kafkaTopics.getEventsSimilarity(), similarity);
                log.info("Отправлено в Kafka: {}", similarity);
            } catch (Exception e) {
                log.error("Ошибка при отправке в Kafka: {}", similarity, e);
            }
        }
    }
}