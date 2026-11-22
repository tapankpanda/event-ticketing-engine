package com.tapan.eventticketingengine.ticket.exception;

public class TicketTypeNotFoundException extends RuntimeException{

    public TicketTypeNotFoundException(Long ticketTypeId) {
        super("Ticket category not found with ID: " + ticketTypeId);
    }
}
