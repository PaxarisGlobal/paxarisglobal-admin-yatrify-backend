package com.yatrify.wishlist.repository;

import com.yatrify.wishlist.model.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

    List<Wishlist> findByUserProfileId(UUID userProfileId);

    Optional<Wishlist> findByUserProfileIdAndTripId(UUID userProfileId, UUID tripId);

    boolean existsByUserProfileIdAndTripId(UUID userProfileId, UUID tripId);

    void deleteByUserProfileIdAndTripId(UUID userProfileId, UUID tripId);

    @Query("SELECT w FROM Wishlist w JOIN FETCH w.trip t WHERE w.userProfile.id = :userId AND t.status = 'PUBLISHED'")
    List<Wishlist> findActiveWishlistByUserId(@Param("userId") UUID userId);
}
