package com.yatrify.trip.dto;

import com.yatrify.trip.model.Trip;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CreateTripRequest {

    @NotBlank(message = "Trip title is required")
    @Size(min = 10, max = 500, message = "Title must be between 10 and 500 characters")
    private String title;

    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    @Size(max = 500, message = "Short description cannot exceed 500 characters")
    private String shortDescription;

    @NotNull(message = "Trip type is required")
    private Trip.TripType tripType;

    private String subType;

    @NotEmpty(message = "At least one destination is required")
    private List<String> destinations;

    @NotNull(message = "Duration in days is required")
    @Min(value = 1, message = "Duration must be at least 1 day")
    private Integer durationDays;

    @NotNull(message = "Duration in nights is required")
    @Min(value = 0, message = "Duration nights cannot be negative")
    private Integer durationNights;

    @NotNull(message = "Start date is required")
    @Future(message = "Start date must be in the future")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Registration deadline is required")
    private LocalDateTime registrationDeadline;

    private String departureCity;
    private String departureLocation;

    @NotNull(message = "Total seats is required")
    @Min(value = 1, message = "Must have at least 1 seat")
    @Max(value = 500, message = "Cannot exceed 500 seats")
    private Integer totalSeats;

    @NotNull(message = "Price per person is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal pricePerPerson;

    @NotNull(message = "Base price is required")
    private BigDecimal basePrice;

    private BigDecimal childPrice;
    private BigDecimal infantPrice;
    private BigDecimal discountPercentage;
    private BigDecimal gstPercentage;

    private List<String> inclusions;
    private List<String> exclusions;
    private String termsAndConditions;
    private String cancellationPolicy;
    private List<String> highlights;
    private List<String> languagesSpoken;

    private Trip.DifficultyLevel difficultyLevel;
    private Integer ageRestrictionMin;
    private Integer ageRestrictionMax;
    private Boolean isVisaRequired;
    private Boolean isInternational;

    @Valid
    private List<ItineraryRequest> itineraries;

    @Valid
    private List<HotelRequest> hotels;

    @Data
    public static class ItineraryRequest {
        @NotNull
        private Integer dayNumber;
        @NotBlank
        private String title;
        private String description;
        private List<String> activities;
        private Map<String, Boolean> meals;
        private String accommodation;
        private String transport;
    }

    @Data
    public static class HotelRequest {
        @NotBlank
        private String hotelName;
        private String city;
        private Integer starRating;
        private Integer checkInDay;
        private Integer checkOutDay;
        private String roomType;
    }
}
