package com.tapan.eventticketingengine.booking.service;

import com.tapan.eventticketingengine.booking.dto.BookingResponse;
import com.tapan.eventticketingengine.booking.entity.Booking;
import com.tapan.eventticketingengine.booking.entity.IdempotencyRecord;
import com.tapan.eventticketingengine.booking.exception.InsufficientInventoryException;
import com.tapan.eventticketingengine.booking.repository.BookingRepository;
import com.tapan.eventticketingengine.booking.repository.IdempotencyRecordRepository;
import com.tapan.eventticketingengine.event.enums.EventStatus;
import com.tapan.eventticketingengine.ticket.entity.TicketType;
import com.tapan.eventticketingengine.ticket.exception.TicketTypeNotFoundException;
import com.tapan.eventticketingengine.ticket.repository.TicketTypeRepository;
import com.tapan.eventticketingengine.user.entity.User;
import com.tapan.eventticketingengine.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
public class BookingReservationProcessor {

    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final BookingRepository bookingRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final Clock clock;
    private final Duration holdDuration;

    public BookingReservationProcessor(
            UserRepository userRepository,
            TicketTypeRepository ticketTypeRepository,
            BookingRepository bookingRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            Clock clock,
            @Value("${booking.reservation.hold-duration:PT10M}")
            Duration holdDuration
    ) {
        if (holdDuration.isZero() || holdDuration.isNegative()) {
            throw new IllegalArgumentException(
                    "Booking hold duration must be positive"
            );
        }

        this.userRepository = userRepository;
        this.ticketTypeRepository = ticketTypeRepository;
        this.bookingRepository = bookingRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.clock = clock;
        this.holdDuration = holdDuration;
    }

    @Transactional
    public BookingResponse reserve(
            Long userId,
            Long eventId,
            Long ticketTypeId,
            int quantity,
            String idempotencyKey,
            String requestHash
    ) {

        // Retrieve the authenticated customer
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated customer account no longer exists"
                ));

        // Verify that the ticket category belongs to the requested published event
        TicketType ticketType = ticketTypeRepository
                .findByIdAndEvent_IdAndEvent_Status(
                        ticketTypeId,
                        eventId,
                        EventStatus.PUBLISHED
                )
                .orElseThrow(() ->
                        new TicketTypeNotFoundException(ticketTypeId)
                );

        // Calculate the hold deadline using the injected UTC Clock
        Instant now = clock.instant();
        Instant expiresAt = now.plus(holdDuration);

        // Create and flush the HELD booking
        Booking booking = new Booking(
                user,
                ticketType,
                quantity,
                expiresAt
        );

        booking = bookingRepository.saveAndFlush(booking);

        // Create and flush the idempotency record
        IdempotencyRecord idempotencyRecord = new IdempotencyRecord(
                user,
                booking,
                idempotencyKey,
                requestHash
        );

        idempotencyRecordRepository.saveAndFlush(idempotencyRecord);

        // Atomically move inventory from available to reserved
        int updatedRows = ticketTypeRepository.reserveInventory(
                ticketTypeId,
                quantity,
                now
        );

        if (updatedRows != 1) {
            throw new InsufficientInventoryException(ticketTypeId);
        }

        // Build the response while the transaction is active
        return toResponse(booking);
    }

    private BookingResponse toResponse(Booking booking) {

        TicketType ticketType = booking.getTicketType();

        return new BookingResponse(
                booking.getId(),
                ticketType.getEvent().getId(),
                ticketType.getEvent().getTitle(),
                ticketType.getId(),
                ticketType.getName(),
                booking.getQuantity(),
                booking.getStatus(),
                booking.getExpiresAt(),
                booking.getConfirmationReference(),
                booking.getConfirmedAt(),
                booking.getCreatedAt()
        );
    }
}
