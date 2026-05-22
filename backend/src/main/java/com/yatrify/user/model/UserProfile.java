package com.yatrify.user.model;

import com.yatrify.common.BaseEntity;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity {

    @Column(name = "generic_user_id", nullable = false, unique = true)
    private String genericUserId;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "address_line1")
    private String addressLine1;

    @Column(name = "address_line2")
    private String addressLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "country")
    @Builder.Default
    private String country = "India";

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "emergency_contact_name")
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone")
    private String emergencyContactPhone;

    @Column(name = "preferred_language")
    @Builder.Default
    private String preferredLanguage = "en";

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "accessibility_needs")
    private String accessibilityNeeds;

    @Type(JsonBinaryType.class)
    @Column(name = "travel_preferences", columnDefinition = "jsonb")
    private List<String> travelPreferences;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "onboarding_completed")
    @Builder.Default
    private Boolean onboardingCompleted = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public String getFullName() {
        if (lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName;
        }
        return firstName;
    }

    public enum Gender {
        MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    }

    public enum VerificationStatus {
        PENDING, UNDER_REVIEW, VERIFIED, REJECTED
    }
}
