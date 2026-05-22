package com.yatrify.organizer.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.yatrify.organizer.model.OrganizerProfile;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizerDto {
    private UUID id;
    private UUID userProfileId;
    private String organizationName;
    private OrganizerProfile.OrganizationType organizationType;
    private String gstin;
    private String panNumber;
    private String website;
    private String description;
    private String logoUrl;
    private String coverImageUrl;
    private Boolean isVerified;
    private OrganizerProfile.VerificationStatus verificationStatus;
    private String verificationNotes;
    private BigDecimal rating;
    private Integer totalReviews;
    private Integer totalTripsConducted;
    private Boolean isActive;
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdAt;
}
