package com.yatrify.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yatrify.booking.model.Booking;
import com.yatrify.booking.model.BookingTraveler;
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
public class BookingDto {

    private UUID id;
    private String bookingReference;
    private TripSummary trip;
    private Integer numAdults;
    private Integer numChildren;
    private Integer numInfants;
    private Integer totalTravelers;
    private BigDecimal baseAmount;
    private BigDecimal discountAmount;
    private BigDecimal gstAmount;
    private BigDecimal totalAmount;
    private BigDecimal amountPaid;
    private Booking.BookingStatus status;
    private String specialRequests;
    private String cancellationReason;
    private LocalDateTime cancelledAt;
    private LocalDateTime confirmedAt;
    private List<TravelerDto> travelers;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class TripSummary {
        private UUID id;
        private String title;
        private String coverImageUrl;
        private LocalDate startDate;
        private LocalDate endDate;
        private String departureCity;
        private List<String> destinations;
        private String organizerName;
    }

    @Data
    @Builder
    public static class TravelerDto {
        private UUID id;
        private String firstName;
        private String lastName;
        private LocalDate dateOfBirth;
        private BookingTraveler.Gender gender;
        private BookingTraveler.TravelerType travelerType;
        private String idType;
        private String idNumber;
        private String dietaryPreference;
    }
}
