package ru.practicum.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.common.TopicPartition;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaInteractionConsumer implements AutoCloseable {
    private final Consumer<String, UserActionAvro> consumer;

    @Value("${analyzer.kafka.topics.user-action-topic}")
    private String userActionTopic;

    public void subscribeToTopics() {
        this.consumer.subscribe(List.of(userActionTopic));
    }

    public ConsumerRecords<String, UserActionAvro> poll() {
        return this.consumer.poll(Duration.ofMillis(1000));
    }

    public void commitAsync() {
        this.consumer.commitAsync();
    }

    public void commitAsync(Map<TopicPartition, OffsetAndMetadata> var1) {
        this.consumer.commitAsync(var1, (offsets, exception) -> {
            if (exception != null) {
                log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
            }
        });
    }

    public void wakeup() {
        this.consumer.wakeup();
    }

    @Override
    public void close() throws Exception {
        try {
            this.consumer.commitSync();
            log.info("Закрываем KafkaHubConsumer...");
            this.consumer.close();
        } catch (Exception e) {
            log.error("Ошибка при попытке закрыть KafkaHubConsumer!", e);
            throw e;
        }
    }
}
