package com.tapan.eventticketingengine.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Locale;

public record LoginRequest (

        @NotBlank(message = "Email is required")
        @Email(message = "Please provide a valid email address")
        @Size(
                max = 255,
                message = "Email can not exceed 255 characters"
        )
        String email,

        @NotBlank(message = "Password is required")
        @Size(
                max = 72,
                message = "Password must not exceed 72 characters"
        )
        String password
){
    public LoginRequest {
        if (email != null) {
            email = email.strip().toLowerCase(Locale.ROOT);
        }
    }
}
