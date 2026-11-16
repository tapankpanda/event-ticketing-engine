package com.tapan.eventticketingengine.auth.dto;

import com.tapan.eventticketingengine.user.enums.UserRole;

import java.time.Instant;

public record RegistrationResponse(

        Long id,

        String fullName,

        String email,

        UserRole role,

        Instant createdAt
) {
}
