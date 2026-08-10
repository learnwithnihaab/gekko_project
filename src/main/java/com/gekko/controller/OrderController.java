package com.gekko.controller;

import com.gekko.dto.OrderRequest;
import com.gekko.entity.OrderEntity;
import com.gekko.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Exposes REST APIs used by Storefront / Argon.
 * In production these calls come via APIGEE which performs client credentials authentication.
 */
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Create order endpoint. Upstream (store) posts new order which Gekko must process and forward to BRIM.
     */
    @PostMapping
    public ResponseEntity<OrderEntity> createOrder(@Valid @RequestBody OrderRequest req) {
        OrderEntity order = orderService.createAndProcessOrder(req);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{externalId}")
    public ResponseEntity<OrderEntity> getByExternalId(@PathVariable String externalId) {
        return orderService.findByExternalId(externalId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
