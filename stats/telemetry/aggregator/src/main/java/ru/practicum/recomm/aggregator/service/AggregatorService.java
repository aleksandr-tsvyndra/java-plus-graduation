package ru.practicum.recomm.aggregator.service;

import ru.practicum.recommendations.avro.UserActionAvro;

public interface AggregatorService {

    void processAction(UserActionAvro actionAvro);
}