package com.tapan.eventticketingengine.event.exception;

public class EventNotFoundException extends RuntimeException{

    public EventNotFoundException(Long eventId) {
        super("Event not found with ID: " + eventId);
    }
}
