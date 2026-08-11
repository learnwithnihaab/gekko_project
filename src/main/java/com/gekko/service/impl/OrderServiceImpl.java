package com.gekko.service.impl;

import com.gekko.dto.OrderRequest;
import com.gekko.entity.Customer;
import com.gekko.entity.OrderEntity;
import com.gekko.integration.BrimIntegrationService;
import com.gekko.outbox.OutboxEvent;
import com.gekko.repository.CustomerRepository;
import com.gekko.repository.OrderRepository;
import com.gekko.repository.OutboxRepository;
import com.gekko.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * OrderServiceImpl - basic implementation which:
 * - ensures customer exists (creates if missing)
 * - persists OrderEntity with status NEW
 * - writes an outbox event (to be published to Kafka)
 * - triggers an asynchronous BRIM contract creation call via BrimIntegrationService
 */
@Service
public class OrderServiceImpl implements OrderService {

    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final BrimIntegrationService brimIntegrationService;

    public OrderServiceImpl(CustomerRepository customerRepository, OrderRepository orderRepository, OutboxRepository outboxRepository, BrimIntegrationService brimIntegrationService) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.outboxRepository = outboxRepository;
        this.brimIntegrationService = brimIntegrationService;
    }

    @Override
    @Transactional
    public OrderEntity createOrder(OrderRequest req) {
        // find or create customer
        Optional<Customer> existing = customerRepository.findByExternalId(String.valueOf(req.getCustomerId()));
        Customer customer = existing.orElseGet(() -> {
            Customer c = new Customer();
            c.setExternalId(String.valueOf(req.getCustomerId()));
            c.setName("Customer-" + req.getCustomerId());
            c.setEmail(null);
            return customerRepository.save(c);
        });

        OrderEntity order = new OrderEntity();
        order.setExternalId(req.getExternalId());
        order.setCustomer(customer);
        order.setProductCode(req.getProductCode());
        order.setAmount(req.getAmount() == null ? BigDecimal.ZERO : req.getAmount());
        order.setCurrency("USD");
        order.setStatus("NEW");

        order = orderRepository.save(order);

        // Add outbox event so a separate publisher can send to Kafka/BRIM
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("Order");
        event.setAggregateId(order.getExternalId());
        event.setType("OrderCreated");
        // Minimal payload - in real system serialize DTO
        String payload = String.format("{\"externalId\":\"%s\",\"orderId\":%d}", order.getExternalId(), order.getId());
        event.setPayload(payload);
        outboxRepository.save(event);

        // Trigger asynchronous BRIM contract creation (resilient call with idempotency header)
        try {
            brimIntegrationService.triggerCreateContract(order);
        } catch (Exception ex) {
            // Don't fail the request if BRIM call scheduling fails; outbox + publisher still ensures eventual delivery via Kafka
            // Log and continue
        }

        return order;
    }
}
