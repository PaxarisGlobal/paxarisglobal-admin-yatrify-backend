package com.yatrify.trip.repository;

import com.yatrify.trip.model.Trip;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TripRepository extends JpaRepository<Trip, UUID>, JpaSpecificationExecutor<Trip> {

    Optional<Trip> findBySlug(String slug);

    Page<Trip> findByStatusAndTripType(Trip.TripStatus status, Trip.TripType tripType, Pageable pageable);

    Page<Trip> findByStatus(Trip.TripStatus status, Pageable pageable);

    List<Trip> findByIsFeaturedTrueAndStatus(Trip.TripStatus status);

    List<Trip> findByIsTrendingTrueAndStatus(Trip.TripStatus status);

    @Query("""
            SELECT t FROM Trip t
            WHERE t.status = 'PUBLISHED'
            AND t.startDate >= :fromDate
            AND (:tripType IS NULL OR t.tripType = :tripType)
            AND (:departureCity IS NULL OR :departureCity = '' OR LOWER(t.departureCity) LIKE LOWER(CONCAT('%', :departureCity, '%')))
            AND (:minPrice IS NULL OR t.pricePerPerson >= :minPrice)
            AND (:maxPrice IS NULL OR t.pricePerPerson <= :maxPrice)
            AND t.availableSeats >= :requiredSeats
            """)
    Page<Trip> searchTrips(
            @Param("fromDate") LocalDate fromDate,
            @Param("tripType") Trip.TripType tripType,
            @Param("departureCity") String departureCity,
            @Param("minPrice") java.math.BigDecimal minPrice,
            @Param("maxPrice") java.math.BigDecimal maxPrice,
            @Param("requiredSeats") int requiredSeats,
            Pageable pageable);

    @Query("""
            SELECT t FROM Trip t
            WHERE t.status = 'PUBLISHED'
            AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(t.departureCity) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Trip> fullTextSearch(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT t FROM Trip t WHERE t.organizer.id = :organizerId ORDER BY t.createdAt DESC")
    Page<Trip> findByOrganizerId(@Param("organizerId") UUID organizerId, Pageable pageable);

    @Modifying
    @Query("UPDATE Trip t SET t.availableSeats = t.availableSeats - :seats WHERE t.id = :tripId AND t.availableSeats >= :seats")
    int decrementAvailableSeats(@Param("tripId") UUID tripId, @Param("seats") int seats);

    @Modifying
    @Query("UPDATE Trip t SET t.availableSeats = t.availableSeats + :seats WHERE t.id = :tripId")
    int incrementAvailableSeats(@Param("tripId") UUID tripId, @Param("seats") int seats);

    @Modifying
    @Query("UPDATE Trip t SET t.viewCount = t.viewCount + 1 WHERE t.id = :tripId")
    void incrementViewCount(@Param("tripId") UUID tripId);
}
