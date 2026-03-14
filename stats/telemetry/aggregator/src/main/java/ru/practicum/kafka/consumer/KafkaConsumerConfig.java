package ru.practicum.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    @Bean
    @ConfigurationProperties(prefix = "aggregator.kafka.consumer.properties")
    public Properties kafkaConsumerProperties() {
        log.info("Готовим конфигурацию для Kafka-консьюмера");
        return new Properties();
    }

    @Bean
    public KafkaConsumer<String, UserActionAvro> kafkaConsumer() {
        log.info("Создаём бин KafkaConsumer...");
        return new KafkaConsumer<>(kafkaConsumerProperties());
    }
}
