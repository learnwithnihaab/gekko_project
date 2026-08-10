package com.gekko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point for Gekko service.
 * This bootstraps Spring, component scanning and configuration.
 */
@SpringBootApplication
public class GekkoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GekkoApplication.class, args);
    }
}
