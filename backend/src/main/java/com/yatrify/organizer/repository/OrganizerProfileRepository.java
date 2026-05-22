package com.yatrify.organizer.repository;

import com.yatrify.organizer.model.OrganizerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrganizerProfileRepository extends JpaRepository<OrganizerProfile, UUID> {

    Optional<OrganizerProfile> findByUserProfileId(UUID userProfileId);

    boolean existsByUserProfileId(UUID userProfileId);

    Page<OrganizerProfile> findByVerificationStatus(
            OrganizerProfile.VerificationStatus status, Pageable pageable);

    @Query("SELECT o FROM OrganizerProfile o WHERE o.isVerified = true AND o.isActive = true ORDER BY o.rating DESC")
    Page<OrganizerProfile> findVerifiedOrganizers(Pageable pageable);

    @Query("SELECT o FROM OrganizerProfile o WHERE o.userProfile.genericUserId = :genericUserId")
    Optional<OrganizerProfile> findByGenericUserId(@Param("genericUserId") String genericUserId);
}
