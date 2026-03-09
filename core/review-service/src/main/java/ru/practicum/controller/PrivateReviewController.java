package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.review.PrivateReviewApi;
import ru.practicum.dto.review.NewReviewDto;
import ru.practicum.dto.review.ReviewDto;
import ru.practicum.dto.review.UpdateReviewDto;
import ru.practicum.service.PrivateReviewService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PrivateReviewController implements PrivateReviewApi {
    private final PrivateReviewService reviewService;

    @Override
    public ReviewDto addReview(Long userId, Long eventId, NewReviewDto dto) {
        log.info("POST-запрос на добавление отзыва к ивенту с id={}", eventId);
        return reviewService.addReview(userId, eventId, dto);
    }

    @Override
    public ReviewDto updateReview(Long userId, Long reviewId, UpdateReviewDto dto) {
        log.info("PATCH-запрос на обновление отзыва с id={}", reviewId);
        return reviewService.updateReview(userId, reviewId, dto);
    }

    @Override
    public void deleteReviewByAuthor(Long userId, Long reviewId) {
        log.info("DELETE-запрос на удаление автором отзыва с id={}", reviewId);
        reviewService.deleteReviewByAuthor(userId, reviewId);
    }

    @Override
    public ReviewDto getReviewById(Long userId, Long reviewId) {
        log.info("GET-запрос на просмотр автором отзыва с id={}", reviewId);
        return reviewService.getReviewById(userId, reviewId);
    }

    @Override
    public List<ReviewDto> getReviewsByAuthor(Long userId) {
        log.info("GET-запрос на просмотр всех отзывов автором с id={}", userId);
        return reviewService.getReviewsByAuthor(userId);
    }
}
