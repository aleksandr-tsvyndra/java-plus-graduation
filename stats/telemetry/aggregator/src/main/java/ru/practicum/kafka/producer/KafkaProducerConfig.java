package ru.practicum.kafka.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaProducerConfig {

    @Bean
    @ConfigurationProperties(prefix = "aggregator.kafka.producer.properties")
    public Properties kafkaProducerProperties() {
        log.info("Готовим конфигурацию для Kafka-продюсера");
        return new Properties();
    }

    @Bean
    public KafkaProducer<String, EventSimilarityAvro> kafkaProducer() {
        log.info("Создаём бин KafkaProducer...");
        return new KafkaProducer<>(kafkaProducerProperties());
    }
}
