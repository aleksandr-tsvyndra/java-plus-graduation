package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.review.PublicReviewApi;
import ru.practicum.dto.review.ReviewDto;
import ru.practicum.service.PublicReviewService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PublicReviewController implements PublicReviewApi {
    private final PublicReviewService reviewService;

    @Override
    public List<ReviewDto> getEventReviewsByPublic(Long eventId, Integer from, Integer size) {
        log.info("GET-запрос публичного контроллера на получение списка отзывов к ивенту с id={}", eventId);
        return reviewService.getEventReviewsByPublic(eventId, from, size);
    }
}
