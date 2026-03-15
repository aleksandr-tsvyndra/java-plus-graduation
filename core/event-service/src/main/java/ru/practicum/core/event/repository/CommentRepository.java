package ru.practicum.core.event.repository;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.util.StringUtils;
import ru.practicum.core.event.dto.comments.AdminCommentParams;
import ru.practicum.core.event.model.Comment;
import ru.practicum.core.event.model.enums.comments.CommentStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {


    Optional<Comment> findById(Long id);

    Optional<Comment> findByIdAndStatus(Long id, CommentStatus status);

    List<Comment> findByAuthorIdAndStatus(Long userId, CommentStatus status);

    List<Comment> findByEventIdAndStatus(Long eventId, CommentStatus status);

    Boolean existsByAuthorIdAndEventId(Long userId, Long eventId);

    class AdminCommentSpecification {
        public static Specification<Comment> withAdminCommentParams(AdminCommentParams params) {
            return (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                // Фильтр по списку ID комментариев
                if (params.getComments() != null && !params.getComments().isEmpty()) {
                    predicates.add(root.get("id").in(params.getComments()));
                }

                // Поиск по тексту (чувствительность к регистру отключена)
                if (StringUtils.hasText(params.getText())) {
                    String likePattern = "%" + params.getText().toLowerCase() + "%";
                    predicates.add(criteriaBuilder.like(
                            criteriaBuilder.lower(root.get("text")),
                            likePattern
                    ));
                }

                // Фильтр по событиям
                if (params.getEvents() != null && !params.getEvents().isEmpty()) {
                    predicates.add(root.get("event").in(params.getEvents()));
                }

                // Фильтр по авторам
                if (params.getAuthors() != null && !params.getAuthors().isEmpty()) {
                    predicates.add(root.get("author").in(params.getAuthors()));
                }

                // Фильтр по статусам
                if (params.getStatus() != null && !params.getStatus().isEmpty()) {
                    predicates.add(root.get("status").in(params.getStatus()));
                }

                // Диапазон даты создания
                if (params.getCreatedDateStart() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            root.get("createdDate"),
                            params.getCreatedDateStart()
                    ));
                }
                if (params.getCreatedDateEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                            root.get("createdDate"),
                            params.getCreatedDateEnd()
                    ));
                }

                // Диапазон даты публикации
                if (params.getPublishedDateStart() != null) {
                    predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                            root.get("publishedDate"),
                            params.getPublishedDateStart()
                    ));
                }
                if (params.getPublishedDateEnd() != null) {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(
                            root.get("publishedDate"),
                            params.getPublishedDateEnd()
                    ));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
        }
    }
}