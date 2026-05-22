package com.yatrify.payment.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.yatrify.booking.model.Booking;
import com.yatrify.booking.repository.BookingRepository;
import com.yatrify.booking.service.BookingService;
import com.yatrify.common.exception.BusinessException;
import com.yatrify.common.exception.ResourceNotFoundException;
import com.yatrify.config.properties.YatrifyProperties;
import com.yatrify.payment.dto.InitiatePaymentRequest;
import com.yatrify.payment.dto.PaymentDto;
import com.yatrify.payment.dto.VerifyPaymentRequest;
import com.yatrify.payment.model.Payment;
import com.yatrify.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;
    private final YatrifyProperties properties;

    @Transactional
    public PaymentDto initiatePayment(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new BusinessException("Booking is not in pending state", "INVALID_BOOKING_STATUS");
        }

        try {
            RazorpayClient razorpay = new RazorpayClient(
                    properties.getRazorpay().getKeyId(),
                    properties.getRazorpay().getKeySecret());

            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", booking.getBookingReference());
            orderRequest.put("payment_capture", 1);

            Order order = razorpay.orders.create(orderRequest);

            Payment payment = Payment.builder()
                    .booking(booking)
                    .paymentReference(generatePaymentRef())
                    .gatewayOrderId(order.get("id"))
                    .amount(booking.getTotalAmount())
                    .currency("INR")
                    .status(Payment.PaymentStatus.INITIATED)
                    .build();

            paymentRepository.save(payment);

            Map<String, Object> gatewayData = new HashMap<>();
            gatewayData.put("orderId", order.get("id").toString());
            gatewayData.put("amount", booking.getTotalAmount().multiply(BigDecimal.valueOf(100)).intValue());
            gatewayData.put("currency", "INR");
            gatewayData.put("keyId", properties.getRazorpay().getKeyId());

            return PaymentDto.builder()
                    .id(payment.getId())
                    .paymentReference(payment.getPaymentReference())
                    .bookingReference(booking.getBookingReference())
                    .amount(booking.getTotalAmount())
                    .status(payment.getStatus())
                    .gatewayData(gatewayData)
                    .build();

        } catch (RazorpayException e) {
            log.error("Razorpay payment initiation failed: {}", e.getMessage());
            throw new BusinessException("Payment initiation failed. Please try again.", "PAYMENT_INIT_FAILED");
        }
    }

    @Transactional
    public PaymentDto verifyPayment(VerifyPaymentRequest request) {
        Payment payment = paymentRepository.findByGatewayOrderId(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "orderId", request.getOrderId()));

        boolean signatureValid = verifyRazorpaySignature(
                request.getOrderId(), request.getPaymentId(), request.getSignature());

        if (!signatureValid) {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Invalid payment signature");
            paymentRepository.save(payment);
            throw new BusinessException("Payment verification failed", "PAYMENT_VERIFICATION_FAILED");
        }

        payment.setGatewayPaymentId(request.getPaymentId());
        payment.setGatewaySignature(request.getSignature());
        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // Update booking
        Booking booking = payment.getBooking();
        booking.setAmountPaid(payment.getAmount());
        bookingService.confirmBooking(booking.getId());

        return PaymentDto.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .bookingReference(booking.getBookingReference())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }

    private boolean verifyRazorpaySignature(String orderId, String paymentId, String signature) {
        try {
            String payload = orderId + "|" + paymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    properties.getRazorpay().getKeySecret().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(hash);
            return generatedSignature.equals(signature);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    private String generatePaymentRef() {
        return "PAY" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
