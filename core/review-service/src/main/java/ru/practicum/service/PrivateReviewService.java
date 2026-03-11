package ru.practicum.service;

import ru.practicum.dto.review.NewReviewDto;
import ru.practicum.dto.review.ReviewDto;
import ru.practicum.dto.review.UpdateReviewDto;

import java.util.List;

public interface PrivateReviewService {

    ReviewDto addReview(Long userId, Long eventId, NewReviewDto dto);

    void deleteReviewByAuthor(Long userId, Long reviewId);

    ReviewDto getReviewById(Long userId, Long reviewId);

    List<ReviewDto> getReviewsByAuthor(Long userId);

    ReviewDto updateReview(Long userId, Long reviewId, UpdateReviewDto dto);

}
