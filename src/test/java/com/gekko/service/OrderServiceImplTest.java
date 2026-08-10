package com.gekko.service;

import com.gekko.dto.OrderRequest;
import com.gekko.entity.OrderEntity;
import com.gekko.outbox.OutboxEvent;
import com.gekko.repository.CustomerRepository;
import com.gekko.repository.OrderRepository;
import com.gekko.repository.OutboxRepository;
import com.gekko.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrderServiceImplTest {

    private CustomerRepository customerRepository;
    private OrderRepository orderRepository;
    private OutboxRepository outboxRepository;
    private OrderServiceImpl orderService;

    @BeforeEach
    void setup() {
        customerRepository = mock(CustomerRepository.class);
        orderRepository = mock(OrderRepository.class);
        outboxRepository = mock(OutboxRepository.class);

        orderService = new OrderServiceImpl(customerRepository, orderRepository, outboxRepository);
    }

    @Test
    void createOrder_createsCustomerAndOrderAndOutbox() {
        OrderRequest req = new OrderRequest();
        req.setExternalId("ext-1");
        req.setCustomerId(100L);
        req.setProductCode("HPWS-1Y");
        req.setAmount(BigDecimal.valueOf(49.99));

        when(customerRepository.findByExternalId("100")).thenReturn(Optional.empty());
        when(orderRepository.save(Mockito.any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(outboxRepository.save(Mockito.any(OutboxEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderEntity order = orderService.createOrder(req);

        assertNotNull(order);
        assertEquals("ext-1", order.getExternalId());

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository, times(1)).save(outboxCaptor.capture());

        OutboxEvent ev = outboxCaptor.getValue();
        assertEquals("Order", ev.getAggregateType());
        assertEquals("OrderCreated", ev.getType());
        assertTrue(ev.getPayload().contains("ext-1"));
    }
}
