package ru.practicum.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.Consumer;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaUserActionConsumer implements AutoCloseable {
    private final Consumer<String, UserActionAvro> consumer;

    @Value("${aggregator.kafka.topics.user-action-topic}")
    private String userActionTopic;

    public void subscribeToTopics() {
        this.consumer.subscribe(List.of(userActionTopic));
    }

    public ConsumerRecords<String, UserActionAvro> poll() {
        return this.consumer.poll(Duration.ofMillis(100));
    }

    public void commitAsync() {
        this.consumer.commitAsync();
    }

    public void wakeup() {
        this.consumer.wakeup();
    }

    @Override
    public void close() throws Exception {
        try {
            this.consumer.commitSync();
            log.info("Закрываем KafkaUserActionConsumer...");
            this.consumer.close();
        } catch (Exception e) {
            log.error("Ошибка при попытке закрыть KafkaUserActionConsumer!", e);
            throw e;
        }
    }
}
