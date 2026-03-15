package ru.practicum.recomm.analyzer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.recomm.analyzer.model.EventSimilarity;
import ru.practicum.recomm.analyzer.model.UserAction;
import ru.practicum.recomm.analyzer.repository.EventSimilarityRepository;
import ru.practicum.recomm.analyzer.repository.UserActionRepository;
import ru.practicum.recomm.analyzer.service.api.RecommendationService;
import ru.practicum.recommendations.messages.InteractionsCountRequest;
import ru.practicum.recommendations.messages.RecommendedEvent;
import ru.practicum.recommendations.messages.SimilarEventsRequest;
import ru.practicum.recommendations.messages.UserPredictionsRequest;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final UserActionRepository userActionRepository;
    private final EventSimilarityRepository eventSimilarityRepository;

    @Override
    public List<RecommendedEvent> getRecommendationsForUser(UserPredictionsRequest request) {
        log.info("Генерация рекомендаций для пользователя {}", request.getUserId());

        long userId = request.getUserId();
        long maxResults = request.getMaxResults(); // Всегда возвращает long (в proto3 0, если не задано)
        if (maxResults <= 0) {
            maxResults = 10;
        }

        // Получаем последние N взаимодействий пользователя
        List<UserAction> recentActions = userActionRepository.findByUserId(userId)
                .stream()
                .sorted(Comparator.comparing(UserAction::getInteractAt).reversed())
                .limit(maxResults)
                .toList();

        if (recentActions.isEmpty()) {
            return Collections.emptyList();
        }

        // Формируем список идентификаторов событий, с которыми взаимодействовал пользователь
        Set<Long> userInteractedEvents = recentActions.stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        // Находим все события, похожие на те, с которыми взаимодействовал пользователь
        List<EventSimilarity> similarEvents = eventSimilarityRepository
                .findAllBySourceEventIdInOrTargetEventIdIn(userInteractedEvents, userInteractedEvents);

        // Выбираем уникальные непросмотренные мероприятия
        Set<Long> recommendedEvents = similarEvents.stream()
                .flatMap(es -> Stream.of(es.getSourceEventId(), es.getTargetEventId()))
                .filter(eventId -> !userInteractedEvents.contains(eventId))
                .distinct()
                .limit(maxResults)
                .collect(Collectors.toSet());

        // Рассчитываем взвешенные оценки
        Map<Long, Double> weightedScores = new HashMap<>();
        for (EventSimilarity similarity : similarEvents) {
            for (Long baseEventId : userInteractedEvents) {
                Long candidateEventId = getOtherEvent(similarity, baseEventId);

                if (candidateEventId == null || userInteractedEvents.contains(candidateEventId)) {
                    continue;
                }

                double userScore = getUserScore(baseEventId);
                double similarityScore = similarity.getSimilarityScore();
                weightedScores.put(candidateEventId,
                        weightedScores.getOrDefault(candidateEventId, 0.0) + (userScore * similarityScore));
            }
        }

        // Сортируем и формируем ответ
        return weightedScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(entry -> RecommendedEvent.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .toList();
    }

    @Override
    public List<RecommendedEvent> getSimilarEvents(SimilarEventsRequest request) {
        log.info("Поиск похожих мероприятий для события {}", request.getEventId());

        long eventId = request.getEventId();
        long userId = request.getUserId();
        long maxResults = request.getMaxResults(); // Всегда возвращает long (в proto3 0, если не задано)
        if (maxResults <= 0) {
            maxResults = 10;
        }

        // Получаем список всех похожих событий
        List<EventSimilarity> allSimilarities = eventSimilarityRepository
                .findAllBySourceEventIdOrTargetEventId(eventId, eventId);

        // Получаем список событий, которые пользователь уже просматривал
        Set<Long> viewedEvents = userActionRepository.findByUserId(userId)
                .stream()
                .map(UserAction::getEventId)
                .collect(Collectors.toSet());

        // Фильтруем только непросмотренные события
        List<RecommendedEvent> result = allSimilarities.stream()
                .filter(similarity -> {
                    Long otherEvent = getOtherEvent(similarity, eventId);
                    return otherEvent != null && !viewedEvents.contains(otherEvent);
                })
                .map(similarity -> {
                    Long otherEvent = getOtherEvent(similarity, eventId);
                    return RecommendedEvent.newBuilder()
                            .setEventId(otherEvent)
                            .setScore(similarity.getSimilarityScore())
                            .build();
                })
                .sorted(Comparator.comparingDouble(RecommendedEvent::getScore).reversed())
                .limit(maxResults)
                .toList();

        log.info("Найдено {} похожих мероприятий", result.size());
        return result;
    }

    @Override
    public List<RecommendedEvent> getInteractionsCount(InteractionsCountRequest request) {
        log.info("Подсчёт взаимодействий для событий: {}", request.getEventIdsList());

        Map<Long, Double> interactionCounts = userActionRepository.findByEventIdIn(request.getEventIdsList())
                .stream()
                .collect(Collectors.groupingBy(
                        UserAction::getEventId,
                        Collectors.summingDouble(UserAction::getScore)
                ));

        return interactionCounts.entrySet().stream()
                .map(entry -> RecommendedEvent.newBuilder()
                        .setEventId(entry.getKey())
                        .setScore(entry.getValue())
                        .build())
                .sorted(Comparator.comparingDouble(RecommendedEvent::getScore).reversed())
                .collect(Collectors.toList());
    }

    private Long getOtherEvent(EventSimilarity similarity, Long baseEventId) {
        if (similarity.getSourceEventId().equals(baseEventId)) {
            return similarity.getTargetEventId();
        } else if (similarity.getTargetEventId().equals(baseEventId)) {
            return similarity.getSourceEventId();
        }
        return null;
    }

    private double getUserScore(Long eventId) {
        return userActionRepository.findByEventId(eventId)
                .stream()
                .mapToDouble(UserAction::getScore)
                .average()
                .orElse(1.0);
    }
}