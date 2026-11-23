package com.tapan.eventticketingengine.booking.dto;

import com.tapan.eventticketingengine.booking.enums.BookingStatus;

import java.time.Instant;

public class BookingResponse {

    private final Long bookingId;
    private final Long eventId;
    private final String eventTitle;
    private final Long ticketTypeId;
    private final String ticketTypeName;
    private final int quantity;
    private final BookingStatus status;
    private final Instant expiresAt;
    private final String confirmationReference;
    private final Instant confirmedAt;
    private final Instant createdAt;

    public BookingResponse(
            Long bookingId,
            Long eventId,
            String eventTitle,
            Long ticketTypeId,
            String ticketTypeName,
            int quantity,
            BookingStatus status,
            Instant expiresAt,
            String confirmationReference,
            Instant confirmedAt,
            Instant createdAt
    ) {
        this.bookingId = bookingId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.ticketTypeId = ticketTypeId;
        this.ticketTypeName = ticketTypeName;
        this.quantity = quantity;
        this.status = status;
        this.expiresAt = expiresAt;
        this.confirmationReference = confirmationReference;
        this.confirmedAt = confirmedAt;
        this.createdAt = createdAt;
    }

    public Long getBookingId() {
        return bookingId;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public Long getTicketTypeId() {
        return ticketTypeId;
    }

    public String getTicketTypeName() {
        return ticketTypeName;
    }

    public int getQuantity() {
        return quantity;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public String getConfirmationReference() {
        return confirmationReference;
    }

    public Instant getConfirmedAt() {
        return confirmedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
