package com.yatrify.trip.controller;

import com.yatrify.common.ApiResponse;
import com.yatrify.common.PageResponse;
import com.yatrify.config.security.YatrifyUserPrincipal;
import com.yatrify.trip.dto.CreateTripRequest;
import com.yatrify.trip.dto.TripDto;
import com.yatrify.trip.model.Trip;
import com.yatrify.trip.service.TripService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/trips")
@RequiredArgsConstructor
@Tag(name = "Trips", description = "Trip management and discovery APIs")
public class TripController {

    private final TripService tripService;

    @GetMapping
    @Operation(summary = "Get all published trips with pagination and filtering")
    public ResponseEntity<ApiResponse<PageResponse<TripDto>>> getTrips(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "startDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(ApiResponse.success(
                tripService.getPublishedTrips(page, size, sortBy, sortDir)));
    }

    @GetMapping("/search")
    @Operation(summary = "Search trips by keyword and filters")
    public ResponseEntity<ApiResponse<PageResponse<TripDto>>> searchTrips(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Trip.TripType tripType,
            @RequestParam(required = false) String departureCity,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "1") int seats,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                tripService.searchTrips(keyword, tripType, departureCity, minPrice, maxPrice, seats, page, size)));
    }

    @GetMapping("/featured")
    @Operation(summary = "Get featured trips")
    public ResponseEntity<ApiResponse<List<TripDto>>> getFeaturedTrips() {
        return ResponseEntity.ok(ApiResponse.success(tripService.getFeaturedTrips()));
    }

    @GetMapping("/trending")
    @Operation(summary = "Get trending trips")
    public ResponseEntity<ApiResponse<List<TripDto>>> getTrendingTrips() {
        return ResponseEntity.ok(ApiResponse.success(tripService.getTrendingTrips()));
    }

    @GetMapping("/{slug}")
    @Operation(summary = "Get trip details by slug")
    public ResponseEntity<ApiResponse<TripDto>> getTripBySlug(
            @PathVariable String slug,
            @AuthenticationPrincipal YatrifyUserPrincipal principal) {
        String genericUserId = principal != null ? principal.getGenericUserId() : null;
        return ResponseEntity.ok(ApiResponse.success(tripService.getTripBySlug(slug, genericUserId)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('YATRIFY_ORGANIZER')")
    @Operation(summary = "Create a new trip", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TripDto>> createTrip(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @Valid @RequestBody CreateTripRequest request) {
        TripDto trip = tripService.createTrip(principal.getGenericUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Trip created successfully", trip));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('YATRIFY_ORGANIZER')")
    @Operation(summary = "Publish a trip", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<TripDto>> publishTrip(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success("Trip published successfully",
                tripService.publishTrip(id, principal.getGenericUserId())));
    }

    @GetMapping("/my-trips")
    @PreAuthorize("hasRole('ORGANIZER') or hasRole('YATRIFY_ORGANIZER')")
    @Operation(summary = "Get organizer's trips", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<TripDto>>> getMyTrips(
            @AuthenticationPrincipal YatrifyUserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                tripService.getOrganizerTrips(principal.getGenericUserId(), page, size)));
    }
}
