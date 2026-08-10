package com.gekko;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GekkoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GekkoApplication.class, args);
    }
}
