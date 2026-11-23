package com.tapan.eventticketingengine.booking.exception;

public class InsufficientInventoryException extends RuntimeException {

    public InsufficientInventoryException(Long ticketTypeId) {
        super(
                "Insufficient inventory for ticket category ID: " + ticketTypeId
        );
    }
}
