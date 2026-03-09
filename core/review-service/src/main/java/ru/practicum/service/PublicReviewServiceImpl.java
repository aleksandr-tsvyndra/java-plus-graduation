package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.review.ReviewDto;
import ru.practicum.feignClient.PublicEventServiceClient;
import ru.practicum.mapper.ReviewMapper;
import ru.practicum.model.Review;
import ru.practicum.repository.ReviewRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PublicReviewServiceImpl implements PublicReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    private final PublicEventServiceClient publicEventClient;

    @Override
    public List<ReviewDto> getEventReviewsByPublic(Long eventId, Integer from, Integer size) {
        publicEventClient.validateEventExistingById(eventId);
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Review> reviews = reviewRepository.findAllByEventId(eventId, pageRequest);
        return reviews
                .stream()
                .map(reviewMapper::toReviewDto)
                .toList();
    }
}
