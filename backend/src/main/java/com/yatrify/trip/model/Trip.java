package com.yatrify.trip.model;

import com.yatrify.common.BaseEntity;
import com.yatrify.organizer.model.OrganizerProfile;
import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private OrganizerProfile organizer;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "slug", unique = true)
    private String slug;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "short_description")
    private String shortDescription;

    @Enumerated(EnumType.STRING)
    @Column(name = "trip_type", nullable = false)
    private TripType tripType;

    @Column(name = "sub_type")
    private String subType;

    @Type(JsonBinaryType.class)
    @Column(name = "destinations", columnDefinition = "jsonb")
    private List<String> destinations;

    @Column(name = "cover_image_url")
    private String coverImageUrl;

    @Type(JsonBinaryType.class)
    @Column(name = "gallery_images", columnDefinition = "jsonb")
    private List<String> galleryImages;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "duration_nights", nullable = false)
    private Integer durationNights;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "registration_deadline", nullable = false)
    private LocalDateTime registrationDeadline;

    @Column(name = "departure_city")
    private String departureCity;

    @Column(name = "departure_location")
    private String departureLocation;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Column(name = "available_seats", nullable = false)
    private Integer availableSeats;

    @Column(name = "min_seats")
    @Builder.Default
    private Integer minSeats = 1;

    @Column(name = "price_per_person", nullable = false, precision = 12, scale = 2)
    private BigDecimal pricePerPerson;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "child_price", precision = 12, scale = 2)
    private BigDecimal childPrice;

    @Column(name = "infant_price", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal infantPrice = BigDecimal.ZERO;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal discountPercentage = BigDecimal.ZERO;

    @Column(name = "gst_percentage", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal gstPercentage = new BigDecimal("5.00");

    @Column(name = "currency")
    @Builder.Default
    private String currency = "INR";

    @Type(JsonBinaryType.class)
    @Column(name = "inclusions", columnDefinition = "jsonb")
    private List<String> inclusions;

    @Type(JsonBinaryType.class)
    @Column(name = "exclusions", columnDefinition = "jsonb")
    private List<String> exclusions;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "cancellation_policy", columnDefinition = "TEXT")
    private String cancellationPolicy;

    @Type(JsonBinaryType.class)
    @Column(name = "highlights", columnDefinition = "jsonb")
    private List<String> highlights;

    @Type(JsonBinaryType.class)
    @Column(name = "languages_spoken", columnDefinition = "jsonb")
    private List<String> languagesSpoken;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    @Builder.Default
    private DifficultyLevel difficultyLevel = DifficultyLevel.EASY;

    @Column(name = "age_restriction_min")
    @Builder.Default
    private Integer ageRestrictionMin = 0;

    @Column(name = "age_restriction_max")
    @Builder.Default
    private Integer ageRestrictionMax = 100;

    @Column(name = "is_visa_required")
    @Builder.Default
    private Boolean isVisaRequired = false;

    @Column(name = "is_international")
    @Builder.Default
    private Boolean isInternational = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private TripStatus status = TripStatus.DRAFT;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "is_trending")
    @Builder.Default
    private Boolean isTrending = false;

    @Column(name = "rating", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @Column(name = "total_bookings")
    @Builder.Default
    private Integer totalBookings = 0;

    @Column(name = "view_count")
    @Builder.Default
    private Integer viewCount = 0;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("dayNumber ASC")
    private List<TripItinerary> itineraries;

    @OneToMany(mappedBy = "trip", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TripHotel> hotels;

    public boolean isAvailable() {
        return TripStatus.PUBLISHED.equals(status)
                && availableSeats > 0
                && LocalDateTime.now().isBefore(registrationDeadline);
    }

    public enum TripType {
        RELIGIOUS, HONEYMOON, BACHELOR, FAMILY, ADVENTURE, SOLO, CORPORATE, EDUCATIONAL, WELLNESS
    }

    public enum TripStatus {
        DRAFT, PUBLISHED, SOLD_OUT, CANCELLED, COMPLETED
    }

    public enum DifficultyLevel {
        EASY, MODERATE, CHALLENGING, EXTREME
    }
}
