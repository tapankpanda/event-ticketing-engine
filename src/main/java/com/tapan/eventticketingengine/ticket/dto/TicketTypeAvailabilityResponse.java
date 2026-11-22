package com.tapan.eventticketingengine.ticket.dto;

public record TicketTypeAvailabilityResponse(

        Long ticketTypeId,

        String name,

        int totalQuantity,

        int availableQuantity
) {
}
