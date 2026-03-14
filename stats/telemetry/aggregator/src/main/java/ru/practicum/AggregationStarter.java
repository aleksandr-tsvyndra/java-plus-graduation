package ru.practicum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.kafka.KafkaUserActionConsumer;
import ru.practicum.kafka.KafkaEventSimilarityProducer;
import ru.practicum.service.EventSimilarityService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final KafkaUserActionConsumer consumer;
    private final KafkaEventSimilarityProducer producer;

    private final EventSimilarityService similarityService;

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));
        try (producer; consumer) {
            consumer.subscribeToTopics();
            while (true) {
                ConsumerRecords<String, UserActionAvro> records = consumer.poll();
                for (ConsumerRecord<String, UserActionAvro> record : records) {
                    log.info("Вызываем метод сервиса aggregateEventSimilarity для агрегации сообщения");
                    List<EventSimilarityAvro> eventSimilarityAvros = similarityService.updateEventSimilarity(record.value());
                    for (EventSimilarityAvro eventSimilarity : eventSimilarityAvros) {
                        producer.send(eventSimilarity);
                    }
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            // игнорируем - закрываем консьюмер и продюсер
        } catch (Exception e) {
            log.error("Ошибка во время работы консьюмера и продюсера в классе AggregationStarter", e);
        }
    }
}
