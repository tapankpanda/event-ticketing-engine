package com.tapan.eventticketingengine.event.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateEventRequest(

        @NotBlank(message = "Event title is required")
        @Size(max = 150, message = "Event title cannot exceed 150 characters")
        String title,

        @Size(max = 2000, message = "Event description cannot exceed 2000 characters")
        String description,

        @NotBlank(message = "Event venue is required")
        @Size(max = 255, message = "Event venue cannot exceed 255 characters")
        String venue,

        @NotNull(message = "Event start time is required")
        Instant startAt,

        @NotNull(message = "Event end time is required")
        Instant endAt
) {

    public CreateEventRequest {
        if (title != null) {
            title = title.strip();
        }

        if (description != null) {
            description = description.strip();
        }

        if (venue != null) {
            venue = venue.strip();
        }
    }

    @AssertTrue(message = "Event end time must be later than event start time")
    public boolean isEndAtAfterStartAt() {
        return startAt == null
                || endAt == null
                || endAt.isAfter(startAt);
    }
}
