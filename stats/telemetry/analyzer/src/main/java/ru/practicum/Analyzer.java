package ru.practicum;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.processor.InteractionProcessor;
import ru.practicum.processor.SimilarityProcessor;

@Slf4j
@EnableDiscoveryClient
@SpringBootApplication
@ConfigurationPropertiesScan
public class Analyzer {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Analyzer.class, args);

        final InteractionProcessor interactionProcessor = context.getBean(InteractionProcessor.class);
        final SimilarityProcessor similarityProcessor = context.getBean(SimilarityProcessor.class);

        log.info("Запускаем в отдельном потоке обработчик пользовательских действий");
        Thread interactionThread = new Thread(interactionProcessor);
        interactionThread.setName("InteractionProcessorThread");
        interactionThread.start();

        log.info("Запускаем в отдельном потоке обработчик схожести мероприятий");
        Thread similarityThread = new Thread(similarityProcessor);
        similarityThread.setName("SimilarityProcessorThread");
        similarityThread.start();
    }
}
