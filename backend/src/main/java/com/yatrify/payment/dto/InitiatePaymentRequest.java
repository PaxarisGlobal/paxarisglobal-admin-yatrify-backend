package com.yatrify.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class InitiatePaymentRequest {
    @NotNull(message = "Booking ID is required")
    private UUID bookingId;
}
