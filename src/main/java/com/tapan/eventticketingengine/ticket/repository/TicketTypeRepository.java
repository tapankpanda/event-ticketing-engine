package com.tapan.eventticketingengine.ticket.repository;

import com.tapan.eventticketingengine.event.enums.EventStatus;
import com.tapan.eventticketingengine.ticket.entity.TicketType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findAllByEvent_IdOrderByNameAsc(Long eventId);

    boolean existsByEvent_IdAndName(Long eventId, String name);

    Optional<TicketType> findByIdAndEvent_Status(
            Long ticketTypeId,
            EventStatus eventStatus
    );

    // Verify that a ticket category belongs to the requested published event
    Optional<TicketType> findByIdAndEvent_IdAndEvent_Status(
            Long ticketTypeId,
            Long eventId,
            EventStatus eventStatus
    );

    // Atomically reserve inventory if sufficient tickets are available
    @Modifying(flushAutomatically = true)
    @Query("""
            UPDATE TicketType t
            SET t.availableQuantity = t.availableQuantity - :quantity,
                t.reservedQuantity = t.reservedQuantity + :quantity,
                t.updatedAt = :updatedAt
            WHERE t.id = :ticketTypeId
              AND t.availableQuantity >= :quantity
            """)
    int reserveInventory(
            @Param("ticketTypeId") Long ticketTypeId,
            @Param("quantity") int quantity,
            @Param("updatedAt") Instant updatedAt
    );
}
