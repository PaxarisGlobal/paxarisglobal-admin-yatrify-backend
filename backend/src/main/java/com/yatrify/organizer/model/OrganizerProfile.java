package com.yatrify.organizer.model;

import com.yatrify.common.BaseEntity;
import com.yatrify.user.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "organizer_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizerProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @Column(name = "organization_name", nullable = false)
    private String organizationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "organization_type", nullable = false)
    private OrganizationType organizationType;

    @Column(name = "gstin")
    private String gstin;

    @Column(name = "pan_number")
    private String panNumber;

    @Column(name = "website")
    private String website;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Column(name = "bank_account_number")
    private String bankAccountNumber;

    @Column(name = "bank_ifsc_code")
    private String bankIfscCode;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_holder")
    private String bankAccountHolder;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status")
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Column(name = "verification_notes", columnDefinition = "TEXT")
    private String verificationNotes;

    @Column(name = "rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "total_trips_conducted")
    @Builder.Default
    private Integer totalTripsConducted = 0;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    public enum OrganizationType {
        INDIVIDUAL, AGENCY, NGO, TEMPLE_TRUST, CORPORATE
    }

    public enum VerificationStatus {
        PENDING, UNDER_REVIEW, VERIFIED, REJECTED
    }
}
