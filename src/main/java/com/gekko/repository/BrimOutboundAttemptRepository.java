package com.gekko.repository;

import com.gekko.entity.BrimOutboundAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrimOutboundAttemptRepository extends JpaRepository<BrimOutboundAttempt, Long> {
    List<BrimOutboundAttempt> findByStatus(String status);
}
