package com.yatrify.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yatrify.user.model.UserProfile;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserProfileDto {

    private UUID id;
    private String genericUserId;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String fullName;
    private String profilePictureUrl;
    private LocalDate dateOfBirth;
    private UserProfile.Gender gender;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String pincode;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String bio;
    private String preferredLanguage;
    private String accessibilityNeeds;
    private List<String> travelPreferences;
    private Boolean isVerified;
    private UserProfile.VerificationStatus verificationStatus;
    private Boolean onboardingCompleted;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
