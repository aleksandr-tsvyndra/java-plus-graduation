package ru.practicum.api.review;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.practicum.dto.review.NewReviewDto;
import ru.practicum.dto.review.ReviewDto;
import ru.practicum.dto.review.UpdateReviewDto;

import java.util.List;

@Validated
@RequestMapping("/users/{userId}/reviews")
public interface PrivateReviewApi {

    @PostMapping("/events/{eventId}")
    @ResponseStatus(HttpStatus.CREATED)
    ReviewDto addReview(@Positive @PathVariable Long userId,
                        @Positive @PathVariable Long eventId,
                        @RequestBody @Valid NewReviewDto dto);

    @PatchMapping("/{reviewId}")
    ReviewDto updateReview(@Positive @PathVariable Long userId,
                           @Positive @PathVariable Long reviewId,
                           @RequestBody @Valid UpdateReviewDto dto);

    @DeleteMapping("/{reviewId}/events/{eventId}")
    void deleteReviewByAuthor(@Positive @PathVariable Long userId,
                              @Positive @PathVariable Long reviewId);

    @GetMapping("/{reviewId}")
    ReviewDto getReviewById(@Positive @PathVariable Long userId,
                            @Positive @PathVariable Long reviewId);

    @GetMapping
    List<ReviewDto> getReviewsByAuthor(@Positive @PathVariable Long userId);

}
