package com.yatrify.organizer.service;

import com.yatrify.common.exception.BusinessException;
import com.yatrify.common.exception.ResourceNotFoundException;
import com.yatrify.auth.AuthService;
import com.yatrify.auth.JwtTokenService;
import com.yatrify.auth.dto.AuthResponse;
import com.yatrify.config.security.YatrifyUserPrincipal;
import com.yatrify.organizer.dto.CreateOrganizerRequest;
import com.yatrify.organizer.dto.OrganizerDto;
import com.yatrify.organizer.dto.OrganizerRegistrationResult;
import com.yatrify.organizer.model.OrganizerProfile;
import com.yatrify.organizer.repository.OrganizerProfileRepository;
import com.yatrify.user.model.UserProfile;
import com.yatrify.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganizerService {

    private final OrganizerProfileRepository organizerProfileRepository;
    private final UserProfileService userProfileService;
    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    @Transactional
    public OrganizerRegistrationResult registerOrganizer(
            YatrifyUserPrincipal principal,
            CreateOrganizerRequest request) {
        UserProfile user = userProfileService.getCurrentUserEntity();

        if (organizerProfileRepository.existsByUserProfileId(user.getId())) {
            throw new BusinessException("You already have an organizer profile", "ORGANIZER_EXISTS");
        }

        OrganizerProfile organizer = OrganizerProfile.builder()
                .userProfile(user)
                .organizationName(request.getOrganizationName())
                .organizationType(request.getOrganizationType())
                .gstin(request.getGstin())
                .panNumber(request.getPanNumber())
                .website(request.getWebsite())
                .description(request.getDescription())
                .bankAccountNumber(request.getBankAccountNumber())
                .bankIfscCode(request.getBankIfscCode())
                .bankName(request.getBankName())
                .bankAccountHolder(request.getBankAccountHolder())
                .build();

        OrganizerDto dto = mapToDto(organizerProfileRepository.save(organizer));

        String keycloakUsername = resolveKeycloakUsername(user, principal);
        log.info("Become organizer: assigning Paxaris product role for user '{}'", keycloakUsername);
        authService.assignOrganizerRole(keycloakUsername);

        List<String> updatedRoles = authService.mergeOrganizerRoles(principal.getRoles());

        String token = jwtTokenService.createToken(
                keycloakUsername,
                principal.getEmail() != null ? principal.getEmail() : user.getEmail(),
                updatedRoles);

        AuthResponse auth = AuthResponse.builder()
                .token(token)
                .genericUserId(keycloakUsername)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(updatedRoles)
                .build();

        return OrganizerRegistrationResult.builder()
                .organizer(dto)
                .auth(auth)
                .build();
    }

    private String resolveKeycloakUsername(UserProfile user, YatrifyUserPrincipal principal) {
        if (user.getGenericUserId() != null && !user.getGenericUserId().isBlank()) {
            return user.getGenericUserId();
        }
        if (principal.getGenericUserId() != null && !principal.getGenericUserId().isBlank()) {
            return principal.getGenericUserId();
        }
        return authService.deriveUsername(user.getEmail());
    }

    public OrganizerDto getMyOrganizerProfile(String genericUserId) {
        return organizerProfileRepository.findByGenericUserId(genericUserId)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("OrganizerProfile", "genericUserId", genericUserId));
    }

    public OrganizerDto getPublicProfile(UUID organizerId) {
        return organizerProfileRepository.findById(organizerId)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("OrganizerProfile", "id", organizerId));
    }

    @Transactional
    public OrganizerDto updateOrganizer(String genericUserId, CreateOrganizerRequest request) {
        OrganizerProfile organizer = organizerProfileRepository.findByGenericUserId(genericUserId)
                .orElseThrow(() -> new ResourceNotFoundException("OrganizerProfile", "genericUserId", genericUserId));

        if (request.getOrganizationName() != null) organizer.setOrganizationName(request.getOrganizationName());
        if (request.getDescription() != null) organizer.setDescription(request.getDescription());
        if (request.getWebsite() != null) organizer.setWebsite(request.getWebsite());
        if (request.getBankAccountNumber() != null) organizer.setBankAccountNumber(request.getBankAccountNumber());
        if (request.getBankIfscCode() != null) organizer.setBankIfscCode(request.getBankIfscCode());
        if (request.getBankName() != null) organizer.setBankName(request.getBankName());
        if (request.getBankAccountHolder() != null) organizer.setBankAccountHolder(request.getBankAccountHolder());

        return mapToDto(organizerProfileRepository.save(organizer));
    }

    public OrganizerDto mapToDto(OrganizerProfile organizer) {
        return OrganizerDto.builder()
                .id(organizer.getId())
                .userProfileId(organizer.getUserProfile() != null ? organizer.getUserProfile().getId() : null)
                .organizationName(organizer.getOrganizationName())
                .organizationType(organizer.getOrganizationType())
                .gstin(organizer.getGstin())
                .panNumber(organizer.getPanNumber())
                .website(organizer.getWebsite())
                .description(organizer.getDescription())
                .logoUrl(organizer.getLogoUrl())
                .coverImageUrl(organizer.getCoverImageUrl())
                .isVerified(organizer.getIsVerified())
                .verificationStatus(organizer.getVerificationStatus())
                .verificationNotes(organizer.getVerificationNotes())
                .rating(organizer.getRating())
                .totalReviews(organizer.getTotalReviews())
                .totalTripsConducted(organizer.getTotalTripsConducted())
                .isActive(organizer.getIsActive())
                .ownerName(organizer.getUserProfile() != null ? organizer.getUserProfile().getFullName() : null)
                .ownerEmail(organizer.getUserProfile() != null ? organizer.getUserProfile().getEmail() : null)
                .createdAt(organizer.getCreatedAt())
                .build();
    }
}
