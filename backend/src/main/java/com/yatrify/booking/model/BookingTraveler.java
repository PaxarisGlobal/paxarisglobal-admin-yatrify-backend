package com.yatrify.booking.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "booking_travelers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingTraveler {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "traveler_type")
    @Builder.Default
    private TravelerType travelerType = TravelerType.ADULT;

    @Column(name = "id_type")
    private String idType;

    @Column(name = "id_number")
    private String idNumber;

    @Column(name = "passport_number")
    private String passportNumber;

    @Column(name = "passport_expiry")
    private LocalDate passportExpiry;

    @Column(name = "nationality")
    @Builder.Default
    private String nationality = "Indian";

    @Column(name = "dietary_preference")
    private String dietaryPreference;

    @Column(name = "medical_conditions")
    private String medicalConditions;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum TravelerType {
        ADULT, CHILD, INFANT
    }

    public enum Gender {
        MALE, FEMALE, OTHER
    }
}
