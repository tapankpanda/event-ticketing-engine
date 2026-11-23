package com.tapan.eventticketingengine.booking.repository;

import com.tapan.eventticketingengine.booking.entity.IdempotencyRecord;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface IdempotencyRecordRepository
        extends JpaRepository<IdempotencyRecord, Long> {

    @EntityGraph(attributePaths = {
            "booking",
            "booking.ticketType",
            "booking.ticketType.event"
    })
    @Transactional(readOnly = true)
    Optional<IdempotencyRecord> findByUser_IdAndIdempotencyKey(
            Long userId,
            String idempotencyKey
    );
}
