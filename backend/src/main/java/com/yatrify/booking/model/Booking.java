package com.yatrify.booking.model;

import com.yatrify.common.BaseEntity;
import com.yatrify.organizer.model.OrganizerProfile;
import com.yatrify.trip.model.Trip;
import com.yatrify.user.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    @Column(name = "booking_reference", nullable = false, unique = true)
    private String bookingReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private OrganizerProfile organizer;

    @Enumerated(EnumType.STRING)
    @Column(name = "booking_type")
    @Builder.Default
    private BookingType bookingType = BookingType.ONLINE;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    @Builder.Default
    private BookingStatus status = BookingStatus.PENDING;

    @Column(name = "num_adults", nullable = false)
    @Builder.Default
    private Integer numAdults = 1;

    @Column(name = "num_children")
    @Builder.Default
    private Integer numChildren = 0;

    @Column(name = "num_infants")
    @Builder.Default
    private Integer numInfants = 0;

    @Column(name = "total_travelers", nullable = false)
    private Integer totalTravelers;

    @Column(name = "base_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "gst_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal gstAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "amount_paid", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "refund_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BookingTraveler> travelers;

    public enum BookingType {
        ONLINE, OFFLINE
    }

    public enum BookingStatus {
        PENDING, CONFIRMED, CANCELLED, COMPLETED, REFUNDED
    }
}
