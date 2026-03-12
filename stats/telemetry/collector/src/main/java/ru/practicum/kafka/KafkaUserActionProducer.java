package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUserActionProducer implements AutoCloseable, DisposableBean {
    private final KafkaProducer<String, UserActionAvro> kafkaProducer;

    @Value("${collector.kafka.producer.topics.user-action-topic}")
    private String userActionTopic;

    public void send(UserActionAvro userAction) {
        try {
            ProducerRecord<String, UserActionAvro> record = new ProducerRecord<>(userActionTopic, userAction);
            log.info("Отправляем в топик {} запись: {}", userActionTopic, record);
            kafkaProducer.send(record);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения!", e);
        }
    }

    public void flush() {
        kafkaProducer.flush();
    }

    @Override
    public void close() {
        try {
            kafkaProducer.flush();
            log.info("Закрываем KafkaEventProducer...");
            kafkaProducer.close(Duration.ofSeconds(10));
        } catch (Exception e) {
            log.error("Ошибка при попытке закрыть KafkaEventProducer!", e);
            throw e;
        }
    }

    @Override
    public void destroy() {
        close();
    }
}
