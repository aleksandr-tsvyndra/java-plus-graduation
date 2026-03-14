package ru.practicum.service;

import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.producer.KafkaEventSimilarityProducer;

public interface EventSimilarityService {

    void aggregateEventSimilarity(KafkaEventSimilarityProducer producer, UserActionAvro userAction);

}
