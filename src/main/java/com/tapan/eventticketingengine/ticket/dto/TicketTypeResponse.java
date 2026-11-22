package com.tapan.eventticketingengine.ticket.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TicketTypeResponse(

        Long id,

        Long eventId,

        String name,

        BigDecimal price,

        int totalQuantity,

        int availableQuantity,

        int reservedQuantity,

        int soldQuantity,

        Instant createdAt,

        Instant updatedAt
) {
}
