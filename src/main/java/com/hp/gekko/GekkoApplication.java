package com.hp.gekko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * GEKKO Application - Main Entry Point
 * 
 * This is the main Spring Boot application class that starts the GEKKO platform.
 * 
 * @EnableAsync - Enables asynchronous method execution (for BRIM callbacks, email sending, etc.)
 * @EnableScheduling - Enables scheduled task execution (cron jobs like license fetching)
 * @EnableCaching - Enables caching mechanism (Redis for performance optimization)
 * @SpringBootApplication - Marks this as a Spring Boot application with auto-configuration
 */
@SpringBootApplication
@EnableAsync        // Allow @Async methods for non-blocking operations
@EnableScheduling   // Allow @Scheduled methods for cron jobs
@EnableCaching      // Allow @Cacheable methods for Redis caching
public class GekkoApplication {

    /**
     * Main method - Entry point of the application
     * Starts the Spring Boot embedded Tomcat server and loads the application context
     * 
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(GekkoApplication.class, args);
        System.out.println("\n\n========== GEKKO Platform Started Successfully ==========");
        System.out.println("API Gateway: http://localhost:8080/api");
        System.out.println("Environment: Development");
        System.out.println("========================================================\n\n");
    }
}
