package ru.practicum.api.review;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.review.ReviewDto;

import java.util.List;

@Validated
public interface AdminReviewApi {

    @GetMapping("/admin/reviews")
    List<ReviewDto> getReviewsByAdmin(@RequestParam(required = false) String text,
                                      @RequestParam(required = false) List<Long> users,
                                      @RequestParam(required = false) List<Long> events,
                                      @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                      @Positive @RequestParam(defaultValue = "10") int size);

    @DeleteMapping("/admin/reviews/{reviewId}")
    void deleteReview(@Positive @PathVariable Long reviewId);

}
