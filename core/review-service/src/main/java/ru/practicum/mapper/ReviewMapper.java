package ru.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.practicum.dto.review.NewReviewDto;
import ru.practicum.dto.review.ReviewDto;
import ru.practicum.model.Review;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface ReviewMapper {

    ReviewDto toReviewDto(Review review);

    Review toReview(NewReviewDto dto);

}
