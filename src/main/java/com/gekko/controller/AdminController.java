package com.gekko.controller;

import com.gekko.outbox.OutboxEvent;
import com.gekko.repository.OutboxRepository;
import com.gekko.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AdminController - small admin APIs for inspecting failed orders and outbox events and triggering retries.
 * These endpoints are intended for internal use and should be protected (JWT + admin role) in production.
 */
@RestController
@RequestMapping("/internal/admin")
public class AdminController {

    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;

    public AdminController(OrderRepository orderRepository, OutboxRepository outboxRepository) {
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
    }

    @GetMapping("/failed-orders")
    public ResponseEntity<?> getFailedOrders() {
        List<?> failed = orderRepository.findAll().stream()
                .filter(o -> "FAILED".equals(((com.gekko.entity.OrderEntity)o).getStatus()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(failed);
    }

    @GetMapping("/outbox/pending")
    public ResponseEntity<?> getPendingOutbox() {
        List<OutboxEvent> pending = outboxRepository.findByPublishedFalse(org.springframework.data.domain.PageRequest.of(0, 100)).getContent();
        return ResponseEntity.ok(pending);
    }

    @PostMapping("/outbox/{id}/retry")
    public ResponseEntity<?> retryOutboxEvent(@PathVariable Long id) {
        OutboxEvent e = outboxRepository.findById(id).orElse(null);
        if (e == null) return ResponseEntity.notFound().build();
        e.setPublished(false);
        outboxRepository.save(e);
        return ResponseEntity.ok().build();
    }
}
