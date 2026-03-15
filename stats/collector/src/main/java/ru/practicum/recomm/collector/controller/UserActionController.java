package ru.practicum.recomm.collector.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.recomm.collector.service.CollectorService;
import ru.practicum.recommendations.messages.UserActionProto;
import ru.practicum.recommendations.services.UserActionControllerGrpc;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final CollectorService collectorService;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        if (request == null) {
            log.warn("Получен пустой запрос");
            responseObserver.onError(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription("Запрос не должен быть null")));
            return;
        }

        try {
            log.debug("Обработка запроса: {}", request);
            collectorService.newUserAction(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.error("Ошибка валидации данных: {}", e.getMessage(), e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INVALID_ARGUMENT.withDescription(e.getMessage())));
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке запроса", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL.withDescription("Внутренняя ошибка сервера")));
        }
    }
}