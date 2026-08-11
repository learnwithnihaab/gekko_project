package com.gekko.service;

import com.gekko.entity.BrimOutboundAttempt;
import com.gekko.entity.OrderEntity;
import com.gekko.repository.BrimOutboundAttemptRepository;
import com.gekko.service.impl.BrimAttemptServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class BrimAttemptServiceImplTest {

    private BrimOutboundAttemptRepository repo;
    private BrimAttemptServiceImpl service;

    @BeforeEach
    void setup() {
        repo = mock(BrimOutboundAttemptRepository.class);
        service = new BrimAttemptServiceImpl(repo, new com.fasterxml.jackson.databind.ObjectMapper());
    }

    @Test
    void createAndMarkSuccess() {
        OrderEntity order = new OrderEntity();
        order.setId(1L);
        order.setExternalId("ext-1");

        BrimOutboundAttempt a = new BrimOutboundAttempt();
        a.setId(10L);
        a.setOrderId(order.getId());
        a.setOrderExternalId(order.getExternalId());
        a.setIdempotencyKey("order-ext-1");
        a.setPayload("payload");
        a.setStatus("PENDING");
        a.setAttempts(0);
        a.setCreatedAt(OffsetDateTime.now());

        when(repo.save(any(BrimOutboundAttempt.class))).thenReturn(a);
        when(repo.findById(10L)).thenReturn(Optional.of(a));

        BrimOutboundAttempt created = service.createAttempt(order, "order-ext-1", "payload");
        assertNotNull(created);
        assertEquals("PENDING", created.getStatus());

        service.markSuccess(10L);
        verify(repo, times(2)).save(any(BrimOutboundAttempt.class));
    }

    @Test
    void markFailureUpdatesStatus() {
        BrimOutboundAttempt a = new BrimOutboundAttempt();
        a.setId(11L);
        a.setStatus("PENDING");
        when(repo.findById(11L)).thenReturn(Optional.of(a));
        when(repo.save(any(BrimOutboundAttempt.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.markFailure(11L, new RuntimeException("boom"));
        assertEquals("FAILED", a.getStatus());
    }
}
