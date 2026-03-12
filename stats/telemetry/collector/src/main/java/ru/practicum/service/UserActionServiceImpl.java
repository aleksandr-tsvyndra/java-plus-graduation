package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.KafkaUserActionProducer;
import ru.practicum.stats.proto.ActionTypeProto;
import ru.practicum.stats.proto.UserActionProto;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActionServiceImpl implements UserActionService {
    private final KafkaUserActionProducer producer;

    @Override
    public void collectUserAction(UserActionProto userAction) {
        log.debug("Билдим UserActionAvro из объекта UserActionProto: {}", userAction);
        UserActionAvro actionAvro = UserActionAvro.newBuilder()
                .setUserId(userAction.getUserId())
                .setEventId(userAction.getEventId())
                .setActionType(mapActionType(userAction.getActionType()))
                .setTimestamp(Instant.ofEpochSecond(userAction.getTimestamp().getSeconds(),
                        userAction.getTimestamp().getNanos()))
                .build();
        log.debug("Отправляем UserActionAvro в топик Kafka");
        producer.send(actionAvro);
        producer.flush();
    }

    private ActionTypeAvro mapActionType(ActionTypeProto action) {
        return switch (action) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            default -> null;
        };
    }
}
