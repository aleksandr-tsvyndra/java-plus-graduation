package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

@EnableDiscoveryClient
@SpringBootApplication
@ConfigurationPropertiesScan
public class Aggregator {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(Aggregator.class, args);
        AggregationStarter aggregationStarter = context.getBean(AggregationStarter.class);

        Runtime.getRuntime().addShutdownHook(new Thread(aggregationStarter::stop));

        Thread aggregatorThread = new Thread(aggregationStarter::start);
        aggregatorThread.setName("AggregatorThread");
        aggregatorThread.start();
    }
}
