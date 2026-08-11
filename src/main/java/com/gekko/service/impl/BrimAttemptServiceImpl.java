package com.gekko.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gekko.entity.BrimOutboundAttempt;
import com.gekko.entity.OrderEntity;
import com.gekko.repository.BrimOutboundAttemptRepository;
import com.gekko.service.BrimAttemptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BrimAttemptServiceImpl implements BrimAttemptService {

    private static final Logger log = LoggerFactory.getLogger(BrimAttemptServiceImpl.class);

    private final BrimOutboundAttemptRepository repo;
    private final ObjectMapper objectMapper;

    public BrimAttemptServiceImpl(BrimOutboundAttemptRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @Override
    public BrimOutboundAttempt createAttempt(OrderEntity order, String idempotencyKey, String payload) {
        BrimOutboundAttempt a = new BrimOutboundAttempt();
        a.setOrderId(order.getId());
        a.setOrderExternalId(order.getExternalId());
        a.setIdempotencyKey(idempotencyKey);
        a.setPayload(payload);
        a.setStatus("PENDING");
        a.setAttempts(0);
        return repo.save(a);
    }

    @Override
    public void markSuccess(Long attemptId) {
        BrimOutboundAttempt a = repo.findById(attemptId).orElse(null);
        if (a == null) return;
        a.setStatus("SUCCESS");
        a.setLastAttemptAt(OffsetDateTime.now());
        a.setAttempts(a.getAttempts() + 1);
        repo.save(a);
    }

    @Override
    public void markFailure(Long attemptId, Throwable t) {
        BrimOutboundAttempt a = repo.findById(attemptId).orElse(null);
        if (a == null) return;
        a.setStatus("FAILED");
        a.setLastAttemptAt(OffsetDateTime.now());
        a.setAttempts(a.getAttempts() + 1);
        repo.save(a);
    }

    @Override
    public java.util.List<BrimOutboundAttempt> listPending() {
        return repo.findByStatus("PENDING");
    }

    @Override
    public BrimOutboundAttempt findById(Long id) {
        return repo.findById(id).orElse(null);
    }
}
