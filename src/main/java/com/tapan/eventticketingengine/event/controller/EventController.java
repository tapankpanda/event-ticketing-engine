package com.tapan.eventticketingengine.event.controller;

import com.tapan.eventticketingengine.event.dto.CreateEventRequest;
import com.tapan.eventticketingengine.event.dto.EventResponse;
import com.tapan.eventticketingengine.event.dto.UpdateEventRequest;
import com.tapan.eventticketingengine.event.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping("/organizer/events")
    public ResponseEntity<EventResponse> createEvent(
            @Valid @RequestBody CreateEventRequest request,
            Authentication authentication
    ) {
        EventResponse response = eventService.createEvent(
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/organizer/events/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody UpdateEventRequest request,
            Authentication authentication
    ) {
        EventResponse response = eventService.updateEvent(
                eventId,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/organizer/events/{eventId}/publish")
    public ResponseEntity<EventResponse> publishEvent(
            @PathVariable Long eventId,
            Authentication authentication
    ) {
        EventResponse response = eventService.publishEvent(
                eventId,
                authentication.getName()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventResponse>> getAllPublishedEvents() {
        return ResponseEntity.ok(
                eventService.getAllPublishedEvents()
        );
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventResponse> getPublishedEvent(
            @PathVariable Long eventId
    ) {
        return ResponseEntity.ok(
                eventService.getPublishedEvent(eventId)
        );
    }

}
