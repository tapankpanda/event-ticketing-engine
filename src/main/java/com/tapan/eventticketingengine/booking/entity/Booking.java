package com.tapan.eventticketingengine.booking.entity;

import com.tapan.eventticketingengine.booking.enums.BookingStatus;
import com.tapan.eventticketingengine.common.entity.BaseAuditableEntity;
import com.tapan.eventticketingengine.ticket.entity.TicketType;
import com.tapan.eventticketingengine.user.entity.User;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(
        name = "bookings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bookings_confirmation_reference",
                        columnNames = "confirmation_reference"
                )
        },
        indexes = {
                @Index(
                        name = "idx_bookings_user_created",
                        columnList = "user_id, created_at"
                ),
                @Index(
                        name = "idx_bookings_status_expires",
                        columnList = "status, expires_at"
                )
        },
        check = {
                @CheckConstraint(
                        name = "ck_bookings_quantity",
                        constraint = "quantity BETWEEN 1 AND 10"
                )
        }
)
public class Booking extends BaseAuditableEntity {

    private static final int MIN_QUANTITY = 1;
    private static final int MAX_QUANTITY = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private BookingStatus status = BookingStatus.HELD;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(
            name = "confirmation_reference",
            nullable = true,
            length = 36
    )
    private String confirmationReference;

    @Column(name = "confirmed_at")
    private Instant confirmedAt;

    protected Booking() {
    }

    public Booking(
            User user,
            TicketType ticketType,
            int quantity,
            Instant expiresAt
    ) {
        this.user = Objects.requireNonNull(
                user,
                "User must not be null"
        );

        this.ticketType = Objects.requireNonNull(
                ticketType,
                "Ticket type must not be null"
        );

        if (quantity < MIN_QUANTITY || quantity > MAX_QUANTITY) {
            throw new IllegalArgumentException(
                    "Booking quantity must be between 1 and 10"
            );
        }

        this.quantity = quantity;

        this.expiresAt = Objects.requireNonNull(
                expiresAt,
                "Expiry time must not be null"
        );
    }

    // Confirms a booking before its hold deadline
    public void confirm(UUID confirmationReference, Instant confirmedAt) {

        ensureHeld();

        UUID reference = Objects.requireNonNull(
                confirmationReference,
                "Confirmation reference must not be null"
        );

        Instant confirmationTime = Objects.requireNonNull(
                confirmedAt,
                "Confirmation time must not be null"
        );

        if (!confirmationTime.isBefore(expiresAt)) {
            throw new IllegalStateException(
                    "Booking hold has expired"
            );
        }

        this.confirmationReference = reference.toString();
        this.confirmedAt = confirmationTime;
        this.status = BookingStatus.CONFIRMED;
    }

    // Cancels a booking that is currently HELD
    public void cancel(Instant cancelledAt) {

        ensureHeld();

        Instant cancellationTime = Objects.requireNonNull(
                cancelledAt,
                "Cancellation time must not be null"
        );

        if (!cancellationTime.isBefore(expiresAt)) {
            throw new IllegalStateException(
                    "Booking hold has expired"
            );
        }

        this.status = BookingStatus.CANCELLED;
    }

    // Marks a HELD booking as EXPIRED
    public void expire() {

        ensureHeld();
        this.status = BookingStatus.EXPIRED;
    }

    // Protects against invalid booking state transitions
    private void ensureHeld() {

        if (this.status != BookingStatus.HELD) {
            throw new IllegalStateException(
                    "Only HELD bookings can be confirmed, cancelled or expired"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public TicketType getTicketType() {
        return ticketType;
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
}
