package ru.practicum.service.similarity;

import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

public interface EventSimilarityService {

    void addSimilarity(EventSimilarityAvro value);

}
