package ru.practicum.mapper;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;
import ru.practicum.dto.review.NewReviewDto;
import ru.practicum.dto.review.ReviewDto;
import ru.practicum.model.Review;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-03-05T17:08:14+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.3 (Amazon.com Inc.)"
)
@Component
public class ReviewMapperImpl implements ReviewMapper {

    @Override
    public ReviewDto toReviewDto(Review review) {
        if ( review == null ) {
            return null;
        }

        ReviewDto reviewDto = new ReviewDto();

        return reviewDto;
    }

    @Override
    public Review toReview(NewReviewDto dto) {
        if ( dto == null ) {
            return null;
        }

        Review review = new Review();

        return review;
    }
}
