package ru.practicum.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.KafkaInteractionConsumer;
import org.apache.kafka.common.TopicPartition;
import ru.practicum.service.interaction.InteractionService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InteractionProcessor implements Runnable {
    private final KafkaInteractionConsumer consumer;
    private final InteractionService interactionService;

    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @Override
    public void run() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try (consumer) {
            consumer.subscribeToTopics();
            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll();
                if (!records.isEmpty()) {
                    int count = 0;
                    for (var record : records) {
                        log.info("UserActionConsumer получил из Kafka сообщение: {}", record);
                        interactionService.addInteraction(record.value());
                        log.info("Действия пользователя успешно добавлены.");
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
            log.error("Ошибка во время обработки действий пользователя консьюмером UserActionConsumer", e);
        }
    }
}
