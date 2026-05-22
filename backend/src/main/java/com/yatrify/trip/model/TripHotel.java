package com.yatrify.trip.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "trip_hotels")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripHotel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trip_id", nullable = false)
    private Trip trip;

    @Column(name = "hotel_name", nullable = false)
    private String hotelName;

    @Column(name = "city")
    private String city;

    @Column(name = "star_rating")
    private Integer starRating;

    @Column(name = "check_in_day")
    private Integer checkInDay;

    @Column(name = "check_out_day")
    private Integer checkOutDay;

    @Column(name = "room_type")
    private String roomType;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
