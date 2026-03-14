package ru.practicum.service.recommendation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.model.Interaction;
import ru.practicum.model.Similarity;
import ru.practicum.repository.InteractionRepository;
import ru.practicum.repository.SimilarityRepository;
import ru.practicum.stats.proto.InteractionsCountRequestProto;
import ru.practicum.stats.proto.RecommendedEventProto;
import ru.practicum.stats.proto.SimilarEventsRequestProto;
import ru.practicum.stats.proto.UserPredictionsRequestProto;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {
    private final InteractionRepository interactionRepo;
    private final SimilarityRepository similarityRepo;

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedEventProto> getInteractionsCount(InteractionsCountRequestProto request) {
        Set<Long> eventIds = new HashSet<>(request.getEventIdList());

        List<Interaction> userActions = findAllByEventIds(eventIds);

        Map<Long, Double> actionMap = userActions.stream()
                .collect(Collectors.groupingBy(
                        Interaction::getEventId,
                        Collectors.summingDouble(Interaction::getRating)));

        return actionMap.entrySet().stream()
                .map(o -> RecommendedEventProto.newBuilder()
                        .setEventId(o.getKey())
                        .setScore(o.getValue())
                        .build())
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedEventProto> getSimilarEvents(SimilarEventsRequestProto request) {
        List<Similarity> similarPair = findAllContainsEventId(request.getEventId());

        Set<Long> ids = similarPair.stream().map(Similarity::getEventId1).collect(Collectors.toSet());
        Set<Long> otherIds = similarPair.stream().map(Similarity::getEventId2).collect(Collectors.toSet());
        ids.addAll(otherIds);

        Set<Long> userEventIds = findAllByUserIdAndEventIdIn(request.getUserId(), ids);

        similarPair.removeIf(o -> userEventIds.contains(o.getEventId1()) && userEventIds.contains(o.getEventId2()));

        return similarPair.stream()
                .sorted(Comparator.comparing(Similarity::getSimilarity, Comparator.reverseOrder()))
                .limit(request.getMaxResults())
                .map(o -> {
                    long eventId = Objects.equals(o.getEventId1(), request.getEventId())
                            ? o.getEventId2()
                            : o.getEventId1();
                    return RecommendedEventProto.newBuilder()
                            .setEventId(eventId)
                            .setScore(o.getSimilarity())
                            .build();
                }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecommendedEventProto> getRecommendationsForUser(UserPredictionsRequestProto request) {
        Set<Long> actionIds = findByUserIdOrderByTimestampDesc(request.getUserId(),
                request.getMaxResults());

        List<Similarity> similarities = findNPairContainsEventIdsSortedDescScore(actionIds,
                request.getMaxResults());

        Map<Long, Double> eventIds = similarities.stream()
                .collect(Collectors.toMap(o -> actionIds.contains(o.getEventId1()) ? o.getEventId2() : o.getEventId1(),
                        Similarity::getSimilarity, Double::max));

        return eventIds.entrySet().stream().map(o -> RecommendedEventProto.newBuilder()
                .setEventId(o.getKey())
                .setScore(o.getValue())
                .build()).toList();
    }

    private List<Interaction> findAllByEventIds(Set<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyList();
        }
        return interactionRepo.findAllByEventIdIn(eventIds).stream().toList();
    }

    private List<Similarity> findAllContainsEventId(long eventId) {
        return similarityRepo.findByEventId1OrEventId2(eventId);
    }

    private List<Similarity> findNPairContainsEventIdsSortedDescScore(Set<Long> eventIds, long maxResults) {
        Pageable pageable = PageRequest.of(0, (int) maxResults);
        return similarityRepo.findTopByEventId1InOrEventId2InOrderBySimilarityDesc(eventIds, eventIds, pageable);
    }

    private Set<Long> findAllByUserIdAndEventIdIn(long userId, Set<Long> eventIds) {
        List<Long> result = interactionRepo.findDistinctEventIdByUserIdAndEventIdIn(userId, eventIds);
        return new HashSet<>(result);
    }

    private Set<Long> findByUserIdOrderByTimestampDesc(long userId, long maxResult) {
        Pageable pageable = PageRequest.of(0, (int) maxResult);
        List<Long> eventIds = interactionRepo.findDistinctEventIdByUserIdOrderByTimestampDesc(userId, pageable);
        return new HashSet<>(eventIds);
    }
}
