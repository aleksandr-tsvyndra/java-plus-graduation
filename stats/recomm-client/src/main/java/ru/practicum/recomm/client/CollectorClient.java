package ru.practicum.recomm.client;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.recommendations.messages.UserActionProto;
import ru.practicum.recommendations.services.UserActionControllerGrpc;

@Component
public class CollectorClient {

    @GrpcClient("collector")
    private UserActionControllerGrpc.UserActionControllerBlockingStub collector;

    public void newUserAction(UserActionProto action) {
        collector.collectUserAction(action);
    }
}