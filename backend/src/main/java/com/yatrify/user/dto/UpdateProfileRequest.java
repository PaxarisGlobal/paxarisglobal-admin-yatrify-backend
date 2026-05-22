package com.yatrify.user.dto;

import com.yatrify.user.model.UserProfile;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateProfileRequest {
    private String phone;
    private String firstName;
    private String lastName;
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
}
