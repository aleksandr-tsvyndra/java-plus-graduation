package ru.practicum.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventSimilarityProducer implements AutoCloseable, DisposableBean {
    private final Producer<String, EventSimilarityAvro> producer;

    @Value("aggregator.kafka.topics.event-similarity-topic")
    private String eventSimilarityTopic;

    public void send(EventSimilarityAvro event) {
        try {
            ProducerRecord<String, EventSimilarityAvro> record = new ProducerRecord<>(eventSimilarityTopic, event);
            log.info("Отправляем в топик {} запись: {}", eventSimilarityTopic, record);
            producer.send(record);
        } catch (Exception e) {
            log.error("Ошибка при отправке сообщения!", e);
        }
    }

    public void flush() {
        producer.flush();
    }

    @Override
    public void close() {
        try {
            producer.flush();
            log.info("Закрываем KafkaEventSimilarityProducer...");
            producer.close(Duration.ofSeconds(10));
        } catch (Exception e) {
            log.error("Ошибка при попытке закрыть KafkaEventSimilarityProducer!", e);
            throw e;
        }
    }

    @Override
    public void destroy() {
        close();
    }
}
