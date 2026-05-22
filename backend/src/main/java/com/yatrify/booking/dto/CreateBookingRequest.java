package com.yatrify.booking.dto;

import com.yatrify.booking.model.BookingTraveler;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateBookingRequest {

    @NotNull(message = "Trip ID is required")
    private UUID tripId;

    @NotNull(message = "Number of adults is required")
    @Min(value = 1, message = "At least 1 adult is required")
    private Integer numAdults;

    @Min(value = 0, message = "Number of children cannot be negative")
    private Integer numChildren = 0;

    @Min(value = 0, message = "Number of infants cannot be negative")
    private Integer numInfants = 0;

    @NotEmpty(message = "Traveler details are required")
    @Valid
    private List<TravelerRequest> travelers;

    private String specialRequests;

    @Data
    public static class TravelerRequest {
        @NotBlank(message = "First name is required")
        private String firstName;

        private String lastName;

        @Past(message = "Date of birth must be in the past")
        private LocalDate dateOfBirth;

        private BookingTraveler.Gender gender;

        private BookingTraveler.TravelerType travelerType = BookingTraveler.TravelerType.ADULT;

        private String idType;
        private String idNumber;
        private String passportNumber;
        private LocalDate passportExpiry;
        private String nationality;
        private String dietaryPreference;
        private String medicalConditions;
    }
}
