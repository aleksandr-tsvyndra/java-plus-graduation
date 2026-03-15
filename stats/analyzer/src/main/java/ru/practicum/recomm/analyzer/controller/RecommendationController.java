package ru.practicum.recomm.analyzer.controller;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.recomm.analyzer.service.api.RecommendationService;
import ru.practicum.recommendations.messages.InteractionsCountRequest;
import ru.practicum.recommendations.messages.RecommendedEvent;
import ru.practicum.recommendations.messages.SimilarEventsRequest;
import ru.practicum.recommendations.messages.UserPredictionsRequest;
import ru.practicum.recommendations.services.RecommendationsControllerGrpc;

@GrpcService
@Slf4j
@RequiredArgsConstructor
public class RecommendationController extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService recommendationService;

    @Override
    public void getSimilarEvents(SimilarEventsRequest eventsRequestProto,
                                 StreamObserver<RecommendedEvent> responseObserver) {
        if (eventsRequestProto == null) {
            log.warn("Получен пустой запрос на получение похожих событий");
            responseObserver.onError(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Запрос не должен быть null")));
            return;
        }

        try {
            log.debug("Обработка запроса на получение похожих событий: {}", eventsRequestProto);
            recommendationService.getSimilarEvents(eventsRequestProto)
                    .forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке запроса на получение похожих событий", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера")));
        }
    }

    @Override
    public void getRecommendationsForUser(UserPredictionsRequest request,
                                          StreamObserver<RecommendedEvent> responseObserver) {
        if (request == null) {
            log.warn("Получен пустой запрос на получение рекомендаций");
            responseObserver.onError(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Запрос не должен быть null")));
            return;
        }

        try {
            log.debug("Обработка запроса на получение рекомендаций: {}", request);
            recommendationService.getRecommendationsForUser(request)
                    .forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка при обработке запроса на получение рекомендаций", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера")));
        }
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequest request,
                                     StreamObserver<RecommendedEvent> responseObserver) {
        if (request == null) {
            log.warn("Получен пустой запрос на подсчёт взаимодействий");
            responseObserver.onError(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Запрос не должен быть null")));
            return;
        }

        try {
            log.debug("Обработка запроса на подсчёт взаимодействий: {}", request);
            recommendationService.getInteractionsCount(request)
                    .forEach(responseObserver::onNext);
            responseObserver.onCompleted();
        } catch (StatusRuntimeException e) {
            log.error("gRPC-ошибка при обработке запроса на подсчёт взаимодействий", e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при обработке запроса на подсчёт взаимодействий", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера")));
        }
    }
}