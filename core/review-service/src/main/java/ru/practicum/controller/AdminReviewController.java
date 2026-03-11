package ru.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.api.review.AdminReviewApi;
import ru.practicum.dto.review.ReviewDto;
import ru.practicum.service.AdminReviewService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AdminReviewController implements AdminReviewApi {
    private final AdminReviewService reviewService;

    @Override
    public List<ReviewDto> getReviewsByAdmin(String text,
                                             List<Long> users,
                                             List<Long> events,
                                             int from,
                                             int size) {
        log.info("GET-запрос админ-контроллера на поиск отзывов по заданным параметрам");
        log.info("Параметры поиска: text={}, users={}, events={}", text, users, events);
        return reviewService.getReviewsByAdmin(text, users, events, from, size);
    }

    @Override
    public void deleteReview(Long reviewId) {
        log.info("DELETE-запрос админ-контроллера на удаление отзыва с id={}", reviewId);
        reviewService.deleteReview(reviewId);
    }
}
