package com.tapan.eventticketingengine.ticket.controller;

import com.tapan.eventticketingengine.ticket.dto.CreateTicketTypeRequest;
import com.tapan.eventticketingengine.ticket.dto.TicketTypeAvailabilityResponse;
import com.tapan.eventticketingengine.ticket.dto.TicketTypeResponse;
import com.tapan.eventticketingengine.ticket.service.TicketTypeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @PostMapping("/organizer/events/{eventId}/ticket-types")
    public ResponseEntity<TicketTypeResponse> createTicketType(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateTicketTypeRequest request,
            Authentication authentication
    ) {

        TicketTypeResponse response = ticketTypeService.createTicketType(
                eventId,
                request,
                authentication.getName()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/events/{eventId}/ticket-types")
    public ResponseEntity<List<TicketTypeResponse>> getTicketTypesByEvent(
            @PathVariable Long eventId
    ) {

        return ResponseEntity.ok(
                ticketTypeService.getTicketTypesByEvent(eventId)
        );
    }

    @GetMapping("/ticket-types/{ticketTypeId}")
    public ResponseEntity<TicketTypeResponse> getTicketType(
            @PathVariable Long ticketTypeId
    ) {

        return ResponseEntity.ok(
                ticketTypeService.getTicketType(ticketTypeId)
        );
    }

    @GetMapping("/ticket-types/{ticketTypeId}/availability")
    public ResponseEntity<TicketTypeAvailabilityResponse> getTicketTypeAvailability(
            @PathVariable Long ticketTypeId
    ) {

        return ResponseEntity.ok(
                ticketTypeService.getTicketTypeAvailability(ticketTypeId)
        );
    }
}
