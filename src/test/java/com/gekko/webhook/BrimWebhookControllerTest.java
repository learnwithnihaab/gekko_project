package com.gekko.webhook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gekko.repository.OrderRepository;
import com.gekko.repository.OutboxRepository;
import com.gekko.repository.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class BrimWebhookControllerTest {

    private OrderRepository orderRepository;
    private SubscriptionRepository subscriptionRepository;
    private OutboxRepository outboxRepository;
    private BrimWebhookController controller;
    private ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        orderRepository = mock(OrderRepository.class);
        subscriptionRepository = mock(SubscriptionRepository.class);
        outboxRepository = mock(OutboxRepository.class);
        controller = new BrimWebhookController(orderRepository, subscriptionRepository, outboxRepository, mapper);
    }

    @Test
    void receiveBrimWebhook_missingFields_returnsBadRequest() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("orderExternalId", "");

        var resp = controller.receiveBrimWebhook(null, payload);
        assertEquals(400, resp.getStatusCodeValue());
    }
}
