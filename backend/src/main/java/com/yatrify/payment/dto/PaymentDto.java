package com.yatrify.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yatrify.payment.model.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentDto {
    private UUID id;
    private String paymentReference;
    private String bookingReference;
    private BigDecimal amount;
    private String currency;
    private Payment.PaymentStatus status;
    private Map<String, Object> gatewayData;
    private LocalDateTime paidAt;
}
