package ru.practicum.recomm.collector.kafka.config;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "collector.kafka.producer.topics")
@Validated
@Getter
@Setter
public class KafkaTopics {

    @NotNull(message = "userActions не может быть null")
    private String userActions;
}
