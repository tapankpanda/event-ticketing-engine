package com.tapan.eventticketingengine.ticket.repository;

import com.tapan.eventticketingengine.event.enums.EventStatus;
import com.tapan.eventticketingengine.ticket.entity.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {

    List<TicketType> findAllByEvent_IdOrderByNameAsc(Long eventId);

    boolean existsByEvent_IdAndName(Long eventId, String name);

    Optional<TicketType> findByIdAndEvent_Status(
            Long ticketTypeId,
            EventStatus eventStatus
    );
}
