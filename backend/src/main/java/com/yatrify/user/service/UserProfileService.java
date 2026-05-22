package com.yatrify.user.service;

import com.yatrify.common.exception.BusinessException;
import com.yatrify.common.exception.ResourceNotFoundException;
import com.yatrify.config.security.YatrifyUserPrincipal;
import com.yatrify.user.dto.UpdateProfileRequest;
import com.yatrify.user.dto.UserProfileDto;
import com.yatrify.user.dto.UserSyncRequest;
import com.yatrify.user.model.UserProfile;
import com.yatrify.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    /**
     * Sync user from generic platform. Creates profile if new, updates if exists.
     */
    @Transactional
    public UserProfileDto syncUser(UserSyncRequest request) {
        return userProfileRepository.findByGenericUserId(request.getGenericUserId())
                .map(existing -> {
                    existing.setEmail(request.getEmail());
                    existing.setFirstName(request.getFirstName());
                    if (request.getLastName() != null) existing.setLastName(request.getLastName());
                    if (request.getPhone() != null) existing.setPhone(request.getPhone());
                    if (request.getProfilePictureUrl() != null) existing.setProfilePictureUrl(request.getProfilePictureUrl());
                    return mapToDto(userProfileRepository.save(existing));
                })
                .orElseGet(() -> {
                    UserProfile profile = UserProfile.builder()
                            .genericUserId(request.getGenericUserId())
                            .email(request.getEmail())
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .phone(request.getPhone())
                            .profilePictureUrl(request.getProfilePictureUrl())
                            .build();
                    return mapToDto(userProfileRepository.save(profile));
                });
    }

    @Cacheable(value = "userProfiles", key = "#genericUserId")
    public UserProfileDto getByGenericUserId(String genericUserId) {
        return userProfileRepository.findActiveByGenericUserId(genericUserId)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "genericUserId", genericUserId));
    }

    public UserProfileDto getById(UUID id) {
        return userProfileRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", id));
    }

    public UserProfile getEntityById(UUID id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "id", id));
    }

    public UserProfile getCurrentUserEntity() {
        YatrifyUserPrincipal principal = getCurrentPrincipal();
        return userProfileRepository.findActiveByGenericUserId(principal.getGenericUserId())
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "genericUserId", principal.getGenericUserId()));
    }

    @Transactional
    @CacheEvict(value = "userProfiles", key = "#genericUserId")
    public UserProfileDto updateProfile(String genericUserId, UpdateProfileRequest request) {
        UserProfile profile = userProfileRepository.findActiveByGenericUserId(genericUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "genericUserId", genericUserId));

        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getFirstName() != null) profile.setFirstName(request.getFirstName());
        if (request.getLastName() != null) profile.setLastName(request.getLastName());
        if (request.getDateOfBirth() != null) profile.setDateOfBirth(request.getDateOfBirth());
        if (request.getGender() != null) profile.setGender(request.getGender());
        if (request.getAddressLine1() != null) profile.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null) profile.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null) profile.setCity(request.getCity());
        if (request.getState() != null) profile.setState(request.getState());
        if (request.getCountry() != null) profile.setCountry(request.getCountry());
        if (request.getPincode() != null) profile.setPincode(request.getPincode());
        if (request.getEmergencyContactName() != null) profile.setEmergencyContactName(request.getEmergencyContactName());
        if (request.getEmergencyContactPhone() != null) profile.setEmergencyContactPhone(request.getEmergencyContactPhone());
        if (request.getBio() != null) profile.setBio(request.getBio());
        if (request.getPreferredLanguage() != null) profile.setPreferredLanguage(request.getPreferredLanguage());
        if (request.getAccessibilityNeeds() != null) profile.setAccessibilityNeeds(request.getAccessibilityNeeds());
        if (request.getTravelPreferences() != null) profile.setTravelPreferences(request.getTravelPreferences());

        // Mark onboarding complete if key fields filled
        if (profile.getPhone() != null && profile.getCity() != null && profile.getDateOfBirth() != null) {
            profile.setOnboardingCompleted(true);
        }

        return mapToDto(userProfileRepository.save(profile));
    }

    @Transactional
    public UserProfileDto uploadProfilePicture(String genericUserId, String imageUrl) {
        UserProfile profile = userProfileRepository.findActiveByGenericUserId(genericUserId)
                .orElseThrow(() -> new ResourceNotFoundException("UserProfile", "genericUserId", genericUserId));
        profile.setProfilePictureUrl(imageUrl);
        return mapToDto(userProfileRepository.save(profile));
    }

    public UserProfileDto getCurrentUserProfile() {
        YatrifyUserPrincipal principal = getCurrentPrincipal();
        return getByGenericUserId(principal.getGenericUserId());
    }

    private YatrifyUserPrincipal getCurrentPrincipal() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof YatrifyUserPrincipal principal)) {
            throw new BusinessException("Not authenticated", "NOT_AUTHENTICATED", org.springframework.http.HttpStatus.UNAUTHORIZED);
        }
        return principal;
    }

    public UserProfileDto mapToDto(UserProfile profile) {
        return UserProfileDto.builder()
                .id(profile.getId())
                .genericUserId(profile.getGenericUserId())
                .email(profile.getEmail())
                .phone(profile.getPhone())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .fullName(profile.getFullName())
                .profilePictureUrl(profile.getProfilePictureUrl())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .addressLine1(profile.getAddressLine1())
                .addressLine2(profile.getAddressLine2())
                .city(profile.getCity())
                .state(profile.getState())
                .country(profile.getCountry())
                .pincode(profile.getPincode())
                .emergencyContactName(profile.getEmergencyContactName())
                .emergencyContactPhone(profile.getEmergencyContactPhone())
                .bio(profile.getBio())
                .preferredLanguage(profile.getPreferredLanguage())
                .accessibilityNeeds(profile.getAccessibilityNeeds())
                .travelPreferences(profile.getTravelPreferences())
                .isVerified(profile.getIsVerified())
                .verificationStatus(profile.getVerificationStatus())
                .onboardingCompleted(profile.getOnboardingCompleted())
                .isActive(profile.getIsActive())
                .createdAt(profile.getCreatedAt())
                .build();
    }
}
