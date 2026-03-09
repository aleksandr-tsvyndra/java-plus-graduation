package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.review.NewReviewDto;
import ru.practicum.dto.review.ReviewDto;
import ru.practicum.dto.review.UpdateReviewDto;
import ru.practicum.dto.enums.EventState;
import ru.practicum.dto.enums.RequestStatus;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.feignClient.ParticipationRequestServiceClient;
import ru.practicum.feignClient.PublicEventServiceClient;
import ru.practicum.feignClient.UserServiceClient;
import ru.practicum.mapper.ReviewMapper;
import ru.practicum.model.Review;
import ru.practicum.repository.ReviewRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateReviewServiceImpl implements PrivateReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    private final UserServiceClient userServiceClient;
    private final PublicEventServiceClient eventServiceClient;
    private final ParticipationRequestServiceClient requestServiceClient;

    @Override
    @Transactional
    public ReviewDto addReview(Long userId, Long eventId, NewReviewDto dto) {
        UserShortDto userShortDto = userServiceClient.getUserShortDtoById(userId);
        EventFullDto eventFullDto = eventServiceClient.getEventFullDtoByIdClient(eventId);
        if (reviewRepository.findByEventIdAndAuthorId(eventId, userId).isPresent()) {
            throw new ConflictException("Юзер с id=" + userId + " уже написал отзыв к ивенту с id=" + eventId + "!");
        }
        verifyReview(userShortDto, eventFullDto);
        Review review = reviewMapper.toReview(dto);
        review.setAuthorId(userShortDto.getId());
        review.setEventId(eventFullDto.getId());
        review.setCreatedOn(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);
        return reviewMapper.toReviewDto(savedReview);
    }

    @Override
    @Transactional
    public ReviewDto updateReview(Long userId, Long reviewId, UpdateReviewDto dto) {
        userServiceClient.getUserById(userId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыва с id=" + reviewId + " нет в БД!"));
        if (!review.getAuthorId().equals(userId)) {
            throw new ConflictException("Пользователь не является автором отзыва");
        }
        if (dto.getText() == null || dto.getText().isBlank() || dto.getText().equals(review.getText())) {
            return reviewMapper.toReviewDto(review);
        }
        review.setText(dto.getText());
        review.setLastUpdatedOn(LocalDateTime.now());
        Review updatedReview = reviewRepository.save(review);
        return reviewMapper.toReviewDto(updatedReview);
    }

    @Override
    @Transactional
    public void deleteReviewByAuthor(Long userId, Long reviewId) {
        UserShortDto userShortDto = userServiceClient.getUserShortDtoById(userId);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыва с id=" + reviewId + " нет в БД!"));
        if (!userShortDto.getId().equals(review.getAuthorId())) {
            throw new ConflictException("Пользователь не является автором отзыва");
        }
        reviewRepository.deleteById(reviewId);
    }

    @Override
    public ReviewDto getReviewById(Long userId, Long reviewId) {
        userServiceClient.getUserById(userId);
        Review review = reviewRepository.findByIdAndAuthorId(reviewId, userId)
                .orElseThrow(() -> new NotFoundException("Юзер с id=" + reviewId + " не писал отзыв с id=" + reviewId + "!"));
        return reviewMapper.toReviewDto(review);
    }

    @Override
    public List<ReviewDto> getReviewsByAuthor(Long userId) {
        userServiceClient.getUserById(userId);
        return reviewRepository.findAllByAuthorId(userId)
                .stream()
                .map(reviewMapper::toReviewDto)
                .toList();
    }

    private void verifyReview(UserShortDto user, EventFullDto event) {
        if (user.getId().equals(event.getInitiator().getId())) {
            throw new ConflictException("Инициатор ивента не может оставлять отзыв на свой ивент!");
        }
        if (!event.getState().equals(EventState.PUBLISHED.toString())) {
            throw new ConflictException("Чтобы оставить отзыв, статус ивента должен быть PUBLISHED!");
        }
        if (!event.getEventDate().plusHours(1).isBefore(LocalDateTime.now())) {
            throw new ConflictException("Нельзя оставить отзыв на ивент, который ещё не закончился!");
        }
        ParticipationRequestDto requestDto = requestServiceClient.getUserRequestByUserIdAndEventId(user.getId(), event.getId());

        if (!requestDto.getStatus().equals(RequestStatus.CONFIRMED.toString())) {
            throw new ConflictException("Чтобы оставить отзыв, статус заявки юзера на участие в ивенте должен быть CONFIRMED!");
        }
    }
}
