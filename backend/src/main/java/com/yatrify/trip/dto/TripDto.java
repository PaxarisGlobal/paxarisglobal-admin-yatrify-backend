package com.yatrify.trip.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yatrify.trip.model.Trip;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TripDto {

    private UUID id;
    private String title;
    private String slug;
    private String description;
    private String shortDescription;
    private Trip.TripType tripType;
    private String subType;
    private List<String> destinations;
    private String coverImageUrl;
    private List<String> galleryImages;
    private Integer durationDays;
    private Integer durationNights;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime registrationDeadline;
    private String departureCity;
    private String departureLocation;
    private Integer totalSeats;
    private Integer availableSeats;
    private Integer minSeats;
    private BigDecimal pricePerPerson;
    private BigDecimal basePrice;
    private BigDecimal childPrice;
    private BigDecimal infantPrice;
    private BigDecimal discountPercentage;
    private BigDecimal gstPercentage;
    private String currency;
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
    private Trip.TripStatus status;
    private Boolean isFeatured;
    private Boolean isTrending;
    private BigDecimal rating;
    private Integer totalReviews;
    private Integer totalBookings;
    private Integer viewCount;
    private OrganizerSummaryDto organizer;
    private List<TripItineraryDto> itineraries;
    private List<TripHotelDto> hotels;
    private boolean isAvailable;
    private boolean isWishlisted;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class OrganizerSummaryDto {
        private UUID id;
        private String organizationName;
        private String logoUrl;
        private BigDecimal rating;
        private Integer totalReviews;
        private Boolean isVerified;
    }

    @Data
    @Builder
    public static class TripItineraryDto {
        private UUID id;
        private Integer dayNumber;
        private String title;
        private String description;
        private List<String> activities;
        private java.util.Map<String, Boolean> meals;
        private String accommodation;
        private String transport;
        private String imageUrl;
    }

    @Data
    @Builder
    public static class TripHotelDto {
        private UUID id;
        private String hotelName;
        private String city;
        private Integer starRating;
        private Integer checkInDay;
        private Integer checkOutDay;
        private String roomType;
        private String imageUrl;
    }
}
