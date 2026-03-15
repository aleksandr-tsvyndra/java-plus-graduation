package ru.practicum.recomm.analyzer.service.api;

import ru.practicum.recommendations.messages.InteractionsCountRequest;
import ru.practicum.recommendations.messages.RecommendedEvent;
import ru.practicum.recommendations.messages.SimilarEventsRequest;
import ru.practicum.recommendations.messages.UserPredictionsRequest;

import java.util.List;

public interface RecommendationService {

    List<RecommendedEvent> getSimilarEvents(SimilarEventsRequest similarEventsRequest);

    List<RecommendedEvent> getRecommendationsForUser(UserPredictionsRequest userPredictionsRequest);

    List<RecommendedEvent> getInteractionsCount(InteractionsCountRequest interactionsCountRequest);

}