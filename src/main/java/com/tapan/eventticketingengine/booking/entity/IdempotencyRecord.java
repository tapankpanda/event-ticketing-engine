package com.tapan.eventticketingengine.booking.entity;

import com.tapan.eventticketingengine.common.entity.BaseAuditableEntity;
import com.tapan.eventticketingengine.user.entity.User;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_idempotency_user_key",
                        columnNames = {"user_id", "idempotency_key"}
                )
        }
)
public class IdempotencyRecord extends BaseAuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    protected IdempotencyRecord() {

    }

    public IdempotencyRecord(
            User user,
            Booking booking,
            String idempotencyKey,
            String requestHash
    ) {
        this.user = Objects.requireNonNull(
                user,
                "User must not be null"
        );

        this.booking = Objects.requireNonNull(
                booking,
                "Booking must not be null"
        );

        if (!Objects.equals(user.getId(), booking.getUser().getId())) {
            throw new IllegalArgumentException(
                    "Idempotency record user must match booking owner"
            );
        }

        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException(
                    "Idempotency key must not be blank"
            );
        }

        if (idempotencyKey.length() > 128) {
            throw new IllegalArgumentException(
                    "Idempotency key cannot exceed 128 characters"
            );
        }

        if (requestHash == null || !requestHash.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException(
                    "Request hash must be a valid SHA-256 hexadecimal hash"
            );
        }

        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Booking getBooking() {
        return booking;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRequestHash() {
        return requestHash;
    }
}
