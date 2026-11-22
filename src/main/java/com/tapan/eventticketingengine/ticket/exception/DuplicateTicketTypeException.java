package com.tapan.eventticketingengine.ticket.exception;

public class DuplicateTicketTypeException extends RuntimeException{

    public DuplicateTicketTypeException(String categoryName) {
        super("Ticket category already exists with name: " + categoryName);
    }
}
