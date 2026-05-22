package com.yatrify.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyPaymentRequest {
    @NotBlank private String orderId;
    @NotBlank private String paymentId;
    @NotBlank private String signature;
}
