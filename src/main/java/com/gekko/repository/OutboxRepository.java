package com.gekko.repository;

import com.gekko.outbox.OutboxEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {
    Page<OutboxEvent> findByPublishedFalse(Pageable pageable);

    @Modifying
    @Transactional
    @Query("update OutboxEvent o set o.published = true where o.id = :id and o.published = false")
    int markPublishedById(@Param("id") Long id);
}
