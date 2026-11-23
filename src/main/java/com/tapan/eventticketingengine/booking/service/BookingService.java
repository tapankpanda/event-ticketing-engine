package com.tapan.eventticketingengine.booking.service;

import com.tapan.eventticketingengine.booking.dto.BookingResponse;
import com.tapan.eventticketingengine.booking.dto.CreateBookingRequest;
import com.tapan.eventticketingengine.booking.entity.Booking;
import com.tapan.eventticketingengine.booking.entity.IdempotencyRecord;
import com.tapan.eventticketingengine.booking.exception.IdempotencyKeyConflictException;
import com.tapan.eventticketingengine.booking.repository.IdempotencyRecordRepository;
import com.tapan.eventticketingengine.event.entity.Event;
import com.tapan.eventticketingengine.ticket.entity.TicketType;

import org.hibernate.exception.ConstraintViolationException;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class BookingService {

    private static final int MAX_IDEMPOTENCY_KEY_LENGTH = 128;

    private static final String IDEMPOTENCY_UNIQUE_CONSTRAINT =
            "uk_idempotency_user_key";

    private final BookingReservationProcessor reservationProcessor;
    private final IdempotencyRecordRepository idempotencyRecordRepository;

    public BookingService(
            BookingReservationProcessor reservationProcessor,
            IdempotencyRecordRepository idempotencyRecordRepository
    ) {
        this.reservationProcessor = reservationProcessor;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
    }

    public BookingResponse reserve(
            Long userId,
            CreateBookingRequest request,
            String idempotencyKey
    ) {

        validateRequest(userId, request);
        validateIdempotencyKey(idempotencyKey);

        Long eventId = request.getEventId();
        Long ticketTypeId = request.getTicketTypeId();
        int quantity = request.getQuantity();

        // Generate a canonical SHA-256 request hash
        String requestHash = generateRequestHash(
                userId,
                eventId,
                ticketTypeId,
                quantity
        );

        // Check whether this customer has already used this key
        Optional<IdempotencyRecord> existingRecord =
                idempotencyRecordRepository.findByUser_IdAndIdempotencyKey(
                        userId,
                        idempotencyKey
                );

        if (existingRecord.isPresent()) {
            return resolveExistingRequest(
                    existingRecord.get(),
                    requestHash
            );
        }

        // Attempt a new reservation in a separate transaction
        try {
            return reservationProcessor.reserve(
                    userId,
                    eventId,
                    ticketTypeId,
                    quantity,
                    idempotencyKey,
                    requestHash
            );

        } catch (DataIntegrityViolationException exception) {

            // Only recover from a duplicate idempotency-key violation
            if (!isIdempotencyKeyConstraintViolation(exception)) {
                throw exception;
            }

            // The processor transaction has rolled back
            // Retrieve the winning request in a fresh transaction
            IdempotencyRecord winningRecord =
                    idempotencyRecordRepository
                            .findByUser_IdAndIdempotencyKey(
                                    userId,
                                    idempotencyKey
                            )
                            .orElseThrow(() -> exception);

            return resolveExistingRequest(
                    winningRecord,
                    requestHash
            );
        }
    }

    private BookingResponse resolveExistingRequest(
            IdempotencyRecord existingRecord,
            String requestHash
    ) {

        // Same key with different request data is not allowed
        if (!existingRecord.getRequestHash().equals(requestHash)) {
            throw new IdempotencyKeyConflictException();
        }

        // Same key and same request: return the original booking
        return toResponse(existingRecord.getBooking());
    }

    private String generateRequestHash(
            Long userId,
            Long eventId,
            Long ticketTypeId,
            int quantity
    ) {

        // Stable canonical representation of the request
        String canonicalRequest =
                "userId=" + userId + "\n" +
                        "eventId=" + eventId + "\n" +
                        "ticketTypeId=" + ticketTypeId + "\n" +
                        "quantity=" + quantity;

        try {
            MessageDigest messageDigest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = messageDigest.digest(
                    canonicalRequest.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is unavailable",
                    exception
            );
        }
    }

    private boolean isIdempotencyKeyConstraintViolation(
            DataIntegrityViolationException exception
    ) {

        Throwable cause = exception;

        while (cause != null) {

            if (cause instanceof ConstraintViolationException violation) {

                String constraintName = violation.getConstraintName();

                if (constraintName != null &&
                        constraintName.endsWith(
                                IDEMPOTENCY_UNIQUE_CONSTRAINT
                        )) {
                    return true;
                }
            }

            if (cause instanceof SQLException sqlException) {

                // MySQL error 1062 means duplicate entry
                if (sqlException.getErrorCode() == 1062 &&
                        sqlException.getMessage() != null &&
                        sqlException.getMessage().contains(
                                IDEMPOTENCY_UNIQUE_CONSTRAINT
                        )) {
                    return true;
                }
            }

            cause = cause.getCause();
        }

        return false;
    }

    private BookingResponse toResponse(Booking booking) {

        TicketType ticketType = booking.getTicketType();
        Event event = ticketType.getEvent();

        return new BookingResponse(
                booking.getId(),
                event.getId(),
                event.getTitle(),
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

    private void validateRequest(
            Long userId,
            CreateBookingRequest request
    ) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "Authenticated user ID must be valid"
            );
        }

        if (request == null ||
                request.getEventId() == null ||
                request.getTicketTypeId() == null ||
                request.getQuantity() == null) {
            throw new IllegalArgumentException(
                    "Reservation request fields are required"
            );
        }

        if (request.getEventId() <= 0 ||
                request.getTicketTypeId() <= 0) {
            throw new IllegalArgumentException(
                    "Event ID and ticket type ID must be positive"
            );
        }

        if (request.getQuantity() < 1 ||
                request.getQuantity() > 10) {
            throw new IllegalArgumentException(
                    "Reservation quantity must be between 1 and 10"
            );
        }
    }

    private void validateIdempotencyKey(String idempotencyKey) {

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency-Key header is required"
            );
        }

        if (idempotencyKey.length() > MAX_IDEMPOTENCY_KEY_LENGTH) {
            throw new IllegalArgumentException(
                    "Idempotency key cannot exceed 128 characters"
            );
        }
    }
}
