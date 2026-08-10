package com.gekko.controller;

import com.gekko.service.IntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Webhook endpoint to receive notifications from BRIM or other systems.
 * BRIM can POST contract creation events to this endpoint if you prefer webhooks over Kafka.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final IntegrationService integrationService;

    public NotificationController(IntegrationService integrationService) {
        this.integrationService = integrationService;
    }

    @PostMapping("/brim")
    public ResponseEntity<Void> brimNotification(@RequestBody Map<String, Object> payload) {
        // Forward to integration service - same handling as Kafka consumer
        integrationService.handleBrimContractEvent(payload);
        return ResponseEntity.accepted().build();
    }
}
