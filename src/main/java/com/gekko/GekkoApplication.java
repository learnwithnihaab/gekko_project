package com.gekko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application entry point for Gekko service.
 * This bootstraps Spring, component scanning and configuration.
 *
 * @EnableKafka - enables Kafka listener infrastructure so @KafkaListener works
 * @EnableScheduling - enables @Scheduled methods (like License polling every 4 hours)
 */
@SpringBootApplication
@EnableKafka
@EnableScheduling
public class GekkoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GekkoApplication.class, args);
    }
}
