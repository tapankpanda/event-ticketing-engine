package com.tapan.eventticketingengine.event.dto;

import com.tapan.eventticketingengine.event.enums.EventStatus;

import java.time.Instant;

public record EventResponse(

        Long id,

        String title,

        String description,

        String venue,

        Instant startAt,

        Instant endAt,

        EventStatus status,

        Instant createdAt,

        Instant updatedAt
) {
}
