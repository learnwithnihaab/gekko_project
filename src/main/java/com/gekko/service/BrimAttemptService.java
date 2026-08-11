package com.gekko.service;

import com.gekko.entity.BrimOutboundAttempt;
import com.gekko.entity.OrderEntity;

import java.util.List;

public interface BrimAttemptService {
    BrimOutboundAttempt createAttempt(OrderEntity order, String idempotencyKey, String payload);
    void markSuccess(Long attemptId);
    void markFailure(Long attemptId, Throwable t);
    List<BrimOutboundAttempt> listPending();
    BrimOutboundAttempt findById(Long id);
}
