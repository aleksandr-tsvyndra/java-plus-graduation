package ru.practicum.service.interaction;

import ru.practicum.ewm.stats.avro.UserActionAvro;

public interface InteractionService {

    void addInteraction(UserActionAvro value);

}
