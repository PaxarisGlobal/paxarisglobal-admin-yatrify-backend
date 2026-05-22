package com.yatrify.user.repository;

import com.yatrify.user.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByGenericUserId(String genericUserId);

    Optional<UserProfile> findByEmail(String email);

    boolean existsByGenericUserId(String genericUserId);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserProfile u WHERE u.genericUserId = :genericUserId AND u.isActive = true")
    Optional<UserProfile> findActiveByGenericUserId(String genericUserId);
}
