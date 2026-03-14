package ru.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.kafka.KafkaEventSimilarityConsumer;
import ru.practicum.service.similarity.EventSimilarityService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimilarityProcessor implements Runnable {
    private final KafkaEventSimilarityConsumer consumer;
    private final EventSimilarityService eventSimilarityService;

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try (consumer) {
            consumer.subscribeToTopics();
            while (true) {
                ConsumerRecords<String, EventSimilarityAvro> records = consumer.poll();
                if (!records.isEmpty()) {
                    int count = 0;
                    for (var record : records) {
                        log.info("EventSimilarityConsumer получил из Kafka сообщение: {}", record);
                        eventSimilarityService.addSimilarity(record.value());
                        log.info("Схожесть мероприятий успешно обработана.");
                        currentOffsets.put(new TopicPartition(record.topic(), record.partition()),
                                new OffsetAndMetadata(record.offset() + 1));
                        if (count % 10 == 0) {
                            consumer.commitAsync(currentOffsets);
                        }
                        count++;
                    }
                    consumer.commitAsync();
                }
            }
        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер
        } catch (Exception e) {
            log.error("Ошибка во время обработки схожести мероприятий консьюмером UserActionConsumer", e);
        }
    }
}
