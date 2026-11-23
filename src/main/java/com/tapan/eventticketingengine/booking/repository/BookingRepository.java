package com.tapan.eventticketingengine.booking.repository;

import com.tapan.eventticketingengine.booking.entity.Booking;
import com.tapan.eventticketingengine.booking.enums.BookingStatus;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdAndUser_Id(Long bookingId, Long userId);

    Page<Booking> findAllByUser_IdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    // Acquire a pessimistic write lock before changing booking state
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM Booking b
            WHERE b.id = :bookingId
            """)
    Optional<Booking> findByIdForUpdate(
            @Param("bookingId") Long bookingId
    );

    // Find overdue HELD bookings for the expiry process
    List<Booking> findTop100ByStatusAndExpiresAtLessThanEqualOrderByExpiresAtAsc(
            BookingStatus status,
            Instant now
    );
}
