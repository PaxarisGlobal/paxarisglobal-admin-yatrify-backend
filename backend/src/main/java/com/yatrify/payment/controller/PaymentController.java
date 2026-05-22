package com.yatrify.payment.controller;

import com.yatrify.common.ApiResponse;
import com.yatrify.config.security.YatrifyUserPrincipal;
import com.yatrify.payment.dto.PaymentDto;
import com.yatrify.payment.dto.VerifyPaymentRequest;
import com.yatrify.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing APIs")
@SecurityRequirement(name = "bearerAuth")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate/{bookingId}")
    @Operation(summary = "Initiate payment for a booking")
    public ResponseEntity<ApiResponse<PaymentDto>> initiatePayment(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @PathVariable UUID bookingId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.initiatePayment(bookingId)));
    }

    @PostMapping("/verify")
    @Operation(summary = "Verify payment after Razorpay callback")
    public ResponseEntity<ApiResponse<PaymentDto>> verifyPayment(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @Valid @RequestBody VerifyPaymentRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully",
                paymentService.verifyPayment(request)));
    }
}
