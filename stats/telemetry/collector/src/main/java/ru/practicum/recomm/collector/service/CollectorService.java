package ru.practicum.recomm.collector.service;

import ru.practicum.recommendations.messages.UserActionProto;

public interface CollectorService {

    void newUserAction(UserActionProto actionProto);

}