package ru.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    @ConfigurationProperties(prefix = "analyzer.kafka.consumer.event-similarity.properties")
    public Properties kafkaEventSimilarityProperties() {
        log.info("Готовим конфигурацию для eventSimilarityConsumer");
        return new Properties();
    }

    @Bean
    @ConfigurationProperties(prefix = "analyzer.kafka.consumer.user-action.properties")
    public Properties kafkaInteractionProperties() {
        log.info("Готовим конфигурацию для interactionConsumer");
        return new Properties();
    }

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> eventSimilarityConsumer() {
        log.info("Создаём бин KafkaEventSimilarityConsumer...");
        return new KafkaConsumer<>(kafkaEventSimilarityProperties());
    }

    @Bean
    public KafkaConsumer<String, UserActionAvro> interactionConsumer() {
        log.info("Создаём бин KafkaInteractionConsumer...");
        return new KafkaConsumer<>(kafkaInteractionProperties());
    }
}
