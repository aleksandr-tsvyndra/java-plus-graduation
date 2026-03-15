package ru.practicum.core.api.exception.model;

import org.springframework.http.HttpStatus;

public record ApiError(
        HttpStatus status,
        String title,
        String message
) {
}
