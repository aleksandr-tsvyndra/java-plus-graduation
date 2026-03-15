package ru.practicum.recomm.collector.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.recomm.collector.kafka.config.KafkaTopics;
import ru.practicum.recommendations.avro.ActionTypeAvro;
import ru.practicum.recommendations.avro.UserActionAvro;
import ru.practicum.recommendations.messages.ActionTypeProto;
import ru.practicum.recommendations.messages.UserActionProto;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {
    private final KafkaTemplate<String, UserActionAvro> kafkaTemplate;
    private final KafkaTopics kafkaTopics;

    private static final Map<ActionTypeProto, ActionTypeAvro> ACTION_TYPE_MAP = new ConcurrentHashMap<>();

    static {
        ACTION_TYPE_MAP.put(ActionTypeProto.ACTION_VIEW, ActionTypeAvro.VIEW);
        ACTION_TYPE_MAP.put(ActionTypeProto.ACTION_LIKE, ActionTypeAvro.LIKE);
        ACTION_TYPE_MAP.put(ActionTypeProto.ACTION_REGISTER, ActionTypeAvro.REGISTER);
    }

    @Override
    public void newUserAction(UserActionProto actionProto) {
        log.debug("Обработка нового пользовательского действия: {}", actionProto);

        try {
            UserActionAvro actionAvro = convertToAvro(actionProto);
            sendToKafka(actionAvro);
            log.info("Событие успешно отправлено в Kafka: {}", actionAvro);
        } catch (Exception e) {
            log.error("Ошибка отправки события в Kafka для пользователя {} и события {}",
                    actionProto.getUserId(), actionProto.getEventId(), e);
            throw new RuntimeException("Ошибка отправки события в Kafka", e);
        }
    }

    private void sendToKafka(UserActionAvro actionAvro) {
        String topic = kafkaTopics.getUserActions();
        log.trace("Отправка события в топик: {}", topic);
        kafkaTemplate.send(topic, actionAvro);
    }

    private UserActionAvro convertToAvro(UserActionProto actionProto) {
        log.trace("Начало преобразования protobuf-данных: {}", actionProto);

        Assert.notNull(actionProto.getTimestamp(), "Временная метка не может быть null");

        // Проверка корректности значений seconds и nanos
        if (actionProto.getTimestamp().getSeconds() < 0 || actionProto.getTimestamp().getNanos() < 0) {
            throw new IllegalArgumentException("Временные метки не могут быть отрицательными");
        }

        UserActionAvro actionAvro = UserActionAvro.newBuilder()
                .setEventId(actionProto.getEventId())
                .setUserId(actionProto.getUserId())
                .setActionType(getAvroType(actionProto.getActionType()))
                .setTimestamp(Instant.ofEpochSecond(
                        actionProto.getTimestamp().getSeconds(),
                        actionProto.getTimestamp().getNanos()))
                .build();

        log.debug("Результат преобразования для пользователя {}: {}", actionProto.getUserId(), actionAvro);
        return actionAvro;
    }

    private ActionTypeAvro getAvroType(ActionTypeProto proto) {
        log.trace("Преобразование типа действия: {}", proto);

        ActionTypeAvro type = ACTION_TYPE_MAP.get(proto);
        if (type == null) {
            log.warn("Неизвестное действие: {}", proto);
            throw new IllegalArgumentException("Неизвестное действие: " + proto);
        }

        log.debug("Тип действия {} преобразован в {}", proto, type);
        return type;
    }
}