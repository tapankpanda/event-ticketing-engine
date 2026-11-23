package com.tapan.eventticketingengine.booking.controller;

import com.tapan.eventticketingengine.booking.dto.BookingResponse;
import com.tapan.eventticketingengine.booking.dto.CreateBookingRequest;
import com.tapan.eventticketingengine.booking.service.BookingService;
import com.tapan.eventticketingengine.user.entity.User;
import com.tapan.eventticketingengine.user.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final UserRepository userRepository;

    public BookingController(
            BookingService bookingService,
            UserRepository userRepository
    ) {
        this.bookingService = bookingService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> reserveBooking(

            @Valid @RequestBody CreateBookingRequest request,
            @RequestHeader(
                    name = "Idempotency-Key",
                    required = false
            )
            String idempotencyKey,

            @AuthenticationPrincipal UserDetails userDetails

    ) {

        // Obtain the authenticated customer's email from JWT authentication
        String authenticatedEmail = userDetails.getUsername();

        User customer = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated customer account no longer exists"
                ));

        BookingResponse response = bookingService.reserve(
                customer.getId(),
                request,
                idempotencyKey
        );

        // Return HTTP 201 Created with the booking resource location
        URI location = URI.create(
                "/api/v1/bookings/" + response.getBookingId()
        );

        return ResponseEntity
                .created(location)
                .body(response);
    }
}
