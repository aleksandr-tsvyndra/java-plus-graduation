package ru.practicum.api.review;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.review.ReviewDto;

import java.util.List;

@Validated
public interface PublicReviewApi {

    @GetMapping("/reviews/{eventId}")
    List<ReviewDto> getEventReviewsByPublic(@Positive @PathVariable Long eventId,
                                            @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                            @Positive @RequestParam(defaultValue = "10") Integer size);

}
