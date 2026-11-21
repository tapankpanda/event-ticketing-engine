package com.tapan.eventticketingengine.event.exception;

public class EventNotDraftException extends RuntimeException{

    public EventNotDraftException(Long eventId) {
        super("Event with ID " + eventId + " is not in DRAFT status");
    }
}
