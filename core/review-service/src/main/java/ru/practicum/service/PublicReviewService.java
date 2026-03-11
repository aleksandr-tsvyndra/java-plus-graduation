package ru.practicum.service;

import ru.practicum.dto.review.ReviewDto;

import java.util.List;

public interface PublicReviewService {

    List<ReviewDto> getEventReviewsByPublic(Long eventId, Integer from, Integer size);

}
