package ru.practicum.core.event.model.enums.events;

import lombok.Getter;

@Getter
public enum EventSort {
    EVENT_DATE("eventDate"),
    RATING("rating");

    private final String sortField;

    EventSort(String sortField) {
        this.sortField = sortField;
    }
}