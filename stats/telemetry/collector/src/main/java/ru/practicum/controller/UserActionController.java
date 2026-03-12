package ru.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.service.UserActionService;
import ru.practicum.stats.proto.UserActionControllerGrpc.UserActionControllerImplBase;
import ru.practicum.stats.proto.UserActionProto;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class UserActionController extends UserActionControllerImplBase {
    private final UserActionService userActionService;

    @Override
    public void collectUserAction(UserActionProto userAction, StreamObserver<Empty> responseObserver) {
        log.info("Сервис Controller получил для обработки объект UserActionProto: {}", userAction.toString());
        try {
            userActionService.collectUserAction(userAction);
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
            log.info("Объект UserActionProto успешно обработан");
        } catch (Exception e) {
            log.info("Ошибка в Controller при обработке объекта UserActionProto!", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)));
        }
    }
}
