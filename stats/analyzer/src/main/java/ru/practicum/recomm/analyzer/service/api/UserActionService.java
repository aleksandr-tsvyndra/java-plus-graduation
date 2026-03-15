package ru.practicum.recomm.analyzer.service.api;

import ru.practicum.recommendations.avro.UserActionAvro;

public interface UserActionService {

    void handleUserAction(UserActionAvro userActionAvro);

}