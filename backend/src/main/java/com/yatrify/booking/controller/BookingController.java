package com.yatrify.booking.controller;

import com.yatrify.booking.dto.BookingDto;
import com.yatrify.booking.dto.CreateBookingRequest;
import com.yatrify.booking.service.BookingService;
import com.yatrify.common.ApiResponse;
import com.yatrify.common.PageResponse;
import com.yatrify.config.security.YatrifyUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Trip booking management APIs")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Create a new booking")
    public ResponseEntity<ApiResponse<BookingDto>> createBooking(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @Valid @RequestBody CreateBookingRequest request) {
        BookingDto booking = bookingService.createBooking(principal.getGenericUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Booking created successfully. Please complete payment.", booking));
    }

    @GetMapping("/my-bookings")
    @Operation(summary = "Get current user's bookings")
    public ResponseEntity<ApiResponse<PageResponse<BookingDto>>> getMyBookings(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getUserBookings(principal.getGenericUserId(), page, size)));
    }

    @GetMapping("/reference/{reference}")
    @Operation(summary = "Get booking by reference number")
    public ResponseEntity<ApiResponse<BookingDto>> getBookingByReference(
            @PathVariable String reference) {
        return ResponseEntity.ok(ApiResponse.success(
                bookingService.getBookingByReference(reference)));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public ResponseEntity<ApiResponse<BookingDto>> cancelBooking(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "Cancelled by user");
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully",
                bookingService.cancelBooking(id, reason, principal.getGenericUserId())));
    }
}
