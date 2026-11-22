package com.tapan.eventticketingengine.ticket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTicketTypeRequest(

        @NotBlank(message = "Ticket category name is required")
        @Size(max = 100, message = "Ticket category name cannot exceed 100 characters")
        String name,

        @NotNull(message = "Ticket category price is required")
        @DecimalMin(
                value = "0.00",
                inclusive = true,
                message = "Ticket category price cannot be negative"
        )
        @Digits(
                integer = 8,
                fraction = 2,
                message = "Ticket category price must have at most 8 integer digits and 2 decimal places"
        )
        BigDecimal price,

        @NotNull(message = "Total ticket quantity is required")
        @Positive(message = "Total ticket quantity must be greater than zero")
        Integer totalQuantity
) {

    public CreateTicketTypeRequest {
        if (name != null) {
            name = name.strip();
        }
    }
}
