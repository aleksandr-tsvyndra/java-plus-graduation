package ru.practicum.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    @ConfigurationProperties(prefix = "collector.kafka.producer.properties")
    public Properties kafkaProducerProperties() {
        log.info("Подготавливаем конфигурацию для продюсера Kafka...");
        return new Properties();
    }

    @Bean
    public KafkaProducer<String, SpecificRecordBase> kafkaProducer() {
        log.info("Создаём бин KafkaProducer...");
        return new KafkaProducer<>(kafkaProducerProperties());
    }
}
