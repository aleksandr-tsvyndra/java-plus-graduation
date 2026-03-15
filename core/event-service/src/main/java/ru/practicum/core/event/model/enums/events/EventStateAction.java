package ru.practicum.core.event.model.enums.events;

import ru.practicum.core.api.util.enums.EventState;

public enum EventStateAction {
    PUBLISH_EVENT,
    REJECT_EVENT,
    SEND_TO_REVIEW,
    CANCEL_REVIEW
}