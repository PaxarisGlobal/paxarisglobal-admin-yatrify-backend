package com.yatrify.booking.repository;

import com.yatrify.booking.model.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    Optional<Booking> findByBookingReference(String bookingReference);

    Page<Booking> findByUserProfileId(UUID userProfileId, Pageable pageable);

    Page<Booking> findByOrganizerId(UUID organizerId, Pageable pageable);

    Page<Booking> findByTripId(UUID tripId, Pageable pageable);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.trip.id = :tripId AND b.status NOT IN ('CANCELLED', 'REFUNDED')")
    long countActiveBookingsByTripId(@Param("tripId") UUID tripId);

    @Query("SELECT b FROM Booking b WHERE b.userProfile.id = :userId AND b.status NOT IN ('CANCELLED', 'REFUNDED') ORDER BY b.createdAt DESC")
    Page<Booking> findActiveBookingsByUserId(@Param("userId") UUID userId, Pageable pageable);

    boolean existsByTripIdAndUserProfileIdAndStatusNotIn(
            UUID tripId, UUID userProfileId, java.util.List<Booking.BookingStatus> statuses);
}
