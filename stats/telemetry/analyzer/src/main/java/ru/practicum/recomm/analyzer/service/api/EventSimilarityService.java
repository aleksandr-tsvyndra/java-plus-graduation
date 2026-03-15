package ru.practicum.recomm.analyzer.service.api;

import ru.practicum.recommendations.avro.EventSimilarityAvro;

public interface EventSimilarityService {

    void handleEventSimilarity(EventSimilarityAvro eventSimilarityAvro);

}