package com.yatrify.trip.service;

import com.yatrify.common.PageResponse;
import com.yatrify.common.exception.BusinessException;
import com.yatrify.common.exception.ResourceNotFoundException;
import com.yatrify.organizer.model.OrganizerProfile;
import com.yatrify.organizer.repository.OrganizerProfileRepository;
import com.yatrify.trip.dto.CreateTripRequest;
import com.yatrify.trip.dto.TripDto;
import com.yatrify.trip.model.Trip;
import com.yatrify.trip.model.TripHotel;
import com.yatrify.trip.model.TripItinerary;
import com.yatrify.trip.repository.TripRepository;
import com.yatrify.user.model.UserProfile;
import com.yatrify.user.service.UserProfileService;
import com.yatrify.wishlist.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TripService {

    private final TripRepository tripRepository;
    private final OrganizerProfileRepository organizerProfileRepository;
    private final UserProfileService userProfileService;
    private final WishlistRepository wishlistRepository;

    @Transactional
    public TripDto createTrip(String genericUserId, CreateTripRequest request) {
        OrganizerProfile organizer = organizerProfileRepository.findByGenericUserId(genericUserId)
                .orElseThrow(() -> new BusinessException(
                        "You need to register as an organizer first", "NOT_ORGANIZER", HttpStatus.FORBIDDEN));

        if (!organizer.getIsVerified()) {
            throw new BusinessException(
                    "Your organizer profile must be verified before creating trips", "ORGANIZER_NOT_VERIFIED", HttpStatus.FORBIDDEN);
        }

        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date must be after start date", "INVALID_DATES");
        }

        Trip trip = Trip.builder()
                .organizer(organizer)
                .title(request.getTitle())
                .slug(generateSlug(request.getTitle()))
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .tripType(request.getTripType())
                .subType(request.getSubType())
                .destinations(request.getDestinations())
                .durationDays(request.getDurationDays())
                .durationNights(request.getDurationNights())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .registrationDeadline(request.getRegistrationDeadline())
                .departureCity(request.getDepartureCity())
                .departureLocation(request.getDepartureLocation())
                .totalSeats(request.getTotalSeats())
                .availableSeats(request.getTotalSeats())
                .pricePerPerson(request.getPricePerPerson())
                .basePrice(request.getBasePrice())
                .childPrice(request.getChildPrice())
                .infantPrice(request.getInfantPrice() != null ? request.getInfantPrice() : BigDecimal.ZERO)
                .discountPercentage(request.getDiscountPercentage() != null ? request.getDiscountPercentage() : BigDecimal.ZERO)
                .gstPercentage(request.getGstPercentage() != null ? request.getGstPercentage() : new BigDecimal("5.00"))
                .inclusions(request.getInclusions())
                .exclusions(request.getExclusions())
                .termsAndConditions(request.getTermsAndConditions())
                .cancellationPolicy(request.getCancellationPolicy())
                .highlights(request.getHighlights())
                .languagesSpoken(request.getLanguagesSpoken())
                .difficultyLevel(request.getDifficultyLevel() != null ? request.getDifficultyLevel() : Trip.DifficultyLevel.EASY)
                .ageRestrictionMin(request.getAgeRestrictionMin() != null ? request.getAgeRestrictionMin() : 0)
                .ageRestrictionMax(request.getAgeRestrictionMax() != null ? request.getAgeRestrictionMax() : 100)
                .isVisaRequired(request.getIsVisaRequired() != null ? request.getIsVisaRequired() : false)
                .isInternational(request.getIsInternational() != null ? request.getIsInternational() : false)
                .status(Trip.TripStatus.DRAFT)
                .build();

        Trip savedTrip = tripRepository.save(trip);

        // Save itineraries
        if (request.getItineraries() != null) {
            List<TripItinerary> itineraries = request.getItineraries().stream()
                    .map(itReq -> TripItinerary.builder()
                            .trip(savedTrip)
                            .dayNumber(itReq.getDayNumber())
                            .title(itReq.getTitle())
                            .description(itReq.getDescription())
                            .activities(itReq.getActivities())
                            .meals(itReq.getMeals())
                            .accommodation(itReq.getAccommodation())
                            .transport(itReq.getTransport())
                            .build())
                    .collect(Collectors.toList());
            savedTrip.setItineraries(itineraries);
        }

        // Save hotels
        if (request.getHotels() != null) {
            List<TripHotel> hotels = request.getHotels().stream()
                    .map(htlReq -> TripHotel.builder()
                            .trip(savedTrip)
                            .hotelName(htlReq.getHotelName())
                            .city(htlReq.getCity())
                            .starRating(htlReq.getStarRating())
                            .checkInDay(htlReq.getCheckInDay())
                            .checkOutDay(htlReq.getCheckOutDay())
                            .roomType(htlReq.getRoomType())
                            .build())
                    .collect(Collectors.toList());
            savedTrip.setHotels(hotels);
        }

        return mapToDto(tripRepository.save(savedTrip), null);
    }

    @Transactional
    public TripDto publishTrip(UUID tripId, String genericUserId) {
        Trip trip = getOrganizerTrip(tripId, genericUserId);

        if (trip.getItineraries() == null || trip.getItineraries().isEmpty()) {
            throw new BusinessException("Trip must have at least one itinerary day", "ITINERARY_REQUIRED");
        }

        trip.setStatus(Trip.TripStatus.PUBLISHED);
        return mapToDto(tripRepository.save(trip), null);
    }

    public PageResponse<TripDto> getPublishedTrips(int page, int size, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Trip> trips = tripRepository.findByStatus(Trip.TripStatus.PUBLISHED, pageable);
        return PageResponse.from(trips.map(t -> mapToDto(t, null)));
    }

    public PageResponse<TripDto> searchTrips(
            String keyword, Trip.TripType tripType, String departureCity,
            BigDecimal minPrice, BigDecimal maxPrice, int requiredSeats,
            int page, int size) {

        if (StringUtils.isNotBlank(keyword)) {
            Pageable pageable = PageRequest.of(page, size);
            Page<Trip> trips = tripRepository.fullTextSearch(keyword, pageable);
            return PageResponse.from(trips.map(t -> mapToDto(t, null)));
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("startDate").ascending());
        Page<Trip> trips = tripRepository.searchTrips(
                LocalDate.now(), tripType, departureCity, minPrice, maxPrice,
                Math.max(1, requiredSeats), pageable);
        return PageResponse.from(trips.map(t -> mapToDto(t, null)));
    }

    public TripDto getTripBySlug(String slug, String genericUserId) {
        Trip trip = tripRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "slug", slug));

        tripRepository.incrementViewCount(trip.getId());

        UUID wishlistUserId = null;
        if (genericUserId != null) {
            try {
                UserProfile user = userProfileService.getCurrentUserEntity();
                wishlistUserId = user.getId();
            } catch (Exception ignored) {}
        }

        boolean wishlisted = wishlistUserId != null &&
                wishlistRepository.existsByUserProfileIdAndTripId(wishlistUserId, trip.getId());

        TripDto dto = mapToDto(trip, null);
        dto.setWishlisted(wishlisted);
        return dto;
    }

    public TripDto getTripById(UUID id) {
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", id));
        return mapToDto(trip, null);
    }

    public List<TripDto> getFeaturedTrips() {
        return tripRepository.findByIsFeaturedTrueAndStatus(Trip.TripStatus.PUBLISHED)
                .stream().map(t -> mapToDto(t, null)).collect(Collectors.toList());
    }

    public List<TripDto> getTrendingTrips() {
        return tripRepository.findByIsTrendingTrueAndStatus(Trip.TripStatus.PUBLISHED)
                .stream().map(t -> mapToDto(t, null)).collect(Collectors.toList());
    }

    public PageResponse<TripDto> getOrganizerTrips(String genericUserId, int page, int size) {
        OrganizerProfile organizer = organizerProfileRepository.findByGenericUserId(genericUserId)
                .orElseThrow(() -> new ResourceNotFoundException("OrganizerProfile", "genericUserId", genericUserId));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Trip> trips = tripRepository.findByOrganizerId(organizer.getId(), pageable);
        return PageResponse.from(trips.map(t -> mapToDto(t, null)));
    }

    @Transactional
    public void updateCoverImage(UUID tripId, String genericUserId, String imageUrl) {
        Trip trip = getOrganizerTrip(tripId, genericUserId);
        trip.setCoverImageUrl(imageUrl);
        tripRepository.save(trip);
    }

    private Trip getOrganizerTrip(UUID tripId, String genericUserId) {
        OrganizerProfile organizer = organizerProfileRepository.findByGenericUserId(genericUserId)
                .orElseThrow(() -> new BusinessException("Not an organizer", "NOT_ORGANIZER", HttpStatus.FORBIDDEN));

        Trip trip = tripRepository.findById(tripId)
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", tripId));

        if (!trip.getOrganizer().getId().equals(organizer.getId())) {
            throw new BusinessException("You don't have permission to modify this trip", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }
        return trip;
    }

    private String generateSlug(String title) {
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        // Ensure uniqueness
        String baseSlug = slug;
        int counter = 1;
        while (tripRepository.findBySlug(slug).isPresent()) {
            slug = baseSlug + "-" + counter++;
        }
        return slug;
    }

    public TripDto mapToDto(Trip trip, UUID currentUserId) {
        TripDto.OrganizerSummaryDto organizerSummary = null;
        if (trip.getOrganizer() != null) {
            OrganizerProfile org = trip.getOrganizer();
            organizerSummary = TripDto.OrganizerSummaryDto.builder()
                    .id(org.getId())
                    .organizationName(org.getOrganizationName())
                    .logoUrl(org.getLogoUrl())
                    .rating(org.getRating())
                    .totalReviews(org.getTotalReviews())
                    .isVerified(org.getIsVerified())
                    .build();
        }

        List<TripDto.TripItineraryDto> itineraryDtos = null;
        if (trip.getItineraries() != null) {
            itineraryDtos = trip.getItineraries().stream()
                    .map(it -> TripDto.TripItineraryDto.builder()
                            .id(it.getId())
                            .dayNumber(it.getDayNumber())
                            .title(it.getTitle())
                            .description(it.getDescription())
                            .activities(it.getActivities())
                            .meals(it.getMeals())
                            .accommodation(it.getAccommodation())
                            .transport(it.getTransport())
                            .imageUrl(it.getImageUrl())
                            .build())
                    .collect(Collectors.toList());
        }

        List<TripDto.TripHotelDto> hotelDtos = null;
        if (trip.getHotels() != null) {
            hotelDtos = trip.getHotels().stream()
                    .map(h -> TripDto.TripHotelDto.builder()
                            .id(h.getId())
                            .hotelName(h.getHotelName())
                            .city(h.getCity())
                            .starRating(h.getStarRating())
                            .checkInDay(h.getCheckInDay())
                            .checkOutDay(h.getCheckOutDay())
                            .roomType(h.getRoomType())
                            .imageUrl(h.getImageUrl())
                            .build())
                    .collect(Collectors.toList());
        }

        return TripDto.builder()
                .id(trip.getId())
                .title(trip.getTitle())
                .slug(trip.getSlug())
                .description(trip.getDescription())
                .shortDescription(trip.getShortDescription())
                .tripType(trip.getTripType())
                .subType(trip.getSubType())
                .destinations(trip.getDestinations())
                .coverImageUrl(trip.getCoverImageUrl())
                .galleryImages(trip.getGalleryImages())
                .durationDays(trip.getDurationDays())
                .durationNights(trip.getDurationNights())
                .startDate(trip.getStartDate())
                .endDate(trip.getEndDate())
                .registrationDeadline(trip.getRegistrationDeadline())
                .departureCity(trip.getDepartureCity())
                .departureLocation(trip.getDepartureLocation())
                .totalSeats(trip.getTotalSeats())
                .availableSeats(trip.getAvailableSeats())
                .pricePerPerson(trip.getPricePerPerson())
                .basePrice(trip.getBasePrice())
                .childPrice(trip.getChildPrice())
                .infantPrice(trip.getInfantPrice())
                .discountPercentage(trip.getDiscountPercentage())
                .gstPercentage(trip.getGstPercentage())
                .currency(trip.getCurrency())
                .inclusions(trip.getInclusions())
                .exclusions(trip.getExclusions())
                .termsAndConditions(trip.getTermsAndConditions())
                .cancellationPolicy(trip.getCancellationPolicy())
                .highlights(trip.getHighlights())
                .languagesSpoken(trip.getLanguagesSpoken())
                .difficultyLevel(trip.getDifficultyLevel())
                .ageRestrictionMin(trip.getAgeRestrictionMin())
                .ageRestrictionMax(trip.getAgeRestrictionMax())
                .isVisaRequired(trip.getIsVisaRequired())
                .isInternational(trip.getIsInternational())
                .status(trip.getStatus())
                .isFeatured(trip.getIsFeatured())
                .isTrending(trip.getIsTrending())
                .rating(trip.getRating())
                .totalReviews(trip.getTotalReviews())
                .totalBookings(trip.getTotalBookings())
                .viewCount(trip.getViewCount())
                .organizer(organizerSummary)
                .itineraries(itineraryDtos)
                .hotels(hotelDtos)
                .isAvailable(trip.isAvailable())
                .createdAt(trip.getCreatedAt())
                .build();
    }
}
