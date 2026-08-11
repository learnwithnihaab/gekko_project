package com.gekko.integration;

import com.gekko.entity.BrimOutboundAttempt;
import com.gekko.entity.OrderEntity;
import com.gekko.service.BrimAttemptService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.*;

public class BrimIntegrationServiceTest {

    private BrimClient brimClient;
    private BrimAttemptService attemptService;
    private BrimIntegrationService integrationService;

    @BeforeEach
    void setup() {
        brimClient = mock(BrimClient.class);
        attemptService = mock(BrimAttemptService.class);
        integrationService = new BrimIntegrationService(brimClient, attemptService);
    }

    @Test
    void triggerCreateContract_marksSuccessOnOk() throws InterruptedException {
        OrderEntity order = new OrderEntity();
        order.setId(5L);
        order.setExternalId("ext-5");
        order.setProductCode("HPWS-1Y");

        BrimOutboundAttempt a = new BrimOutboundAttempt();
        a.setId(100L);
        a.setOrderId(order.getId());

        when(attemptService.createAttempt(any(), anyString(), anyString())).thenReturn(a);
        when(brimClient.createContract(order)).thenReturn(Mono.just("ok"));

        integrationService.triggerCreateContract(order);

        // allow async subscribe to run
        Thread.sleep(200);

        verify(attemptService, times(1)).markSuccess(100L);
    }
}
