package ru.practicum.service.review;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.review.ReviewDto;
import ru.practicum.exception.NotFoundException;
import ru.practicum.mapper.ReviewMapper;
import ru.practicum.model.Review;
import ru.practicum.repository.ReviewRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminReviewServiceImpl implements AdminReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    @Override
    public List<ReviewDto> getReviewsByAdmin(String text,
                                             List<Long> users,
                                             List<Long> events,
                                             Integer from,
                                             Integer size) {
        Specification<Review> spec = Specification.where(null);
        if (text != null && !text.isBlank()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("text")), "%" + text.toLowerCase() + "%"));
        }
        if (users != null && !users.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("author").get("id").in(users));
        }
        if (events != null && !events.isEmpty()) {
            spec = spec.and((root, query, criteriaBuilder) ->
                    root.get("event").get("id").in(events));
        }
        Pageable pageable = PageRequest.of(from / size, size);
        List<Review> reviews = reviewRepository.findAll(spec, pageable).toList();
        return reviews
                .stream()
                .map(reviewMapper::toReviewDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteReview(Long reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new NotFoundException("Отзыва с id=" + reviewId + " нет в БД!");
        }
        reviewRepository.deleteById(reviewId);
    }
}
