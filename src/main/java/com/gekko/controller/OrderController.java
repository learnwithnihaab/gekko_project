package com.gekko.controller;

import com.gekko.dto.OrderRequest;
import com.gekko.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("ok");
    }

    /**
     * Example create order endpoint that validates input.
     * In the starter this returns a placeholder response.
     */
    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequest req) {
        // In a real implementation call orderService.createAndProcessOrder(req)
        // If a referenced resource is missing throw new ResourceNotFoundException("Customer not found: " + req.getCustomerId());

        // Placeholder created response
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", "created", "externalId", req.getExternalId()));
    }
}
