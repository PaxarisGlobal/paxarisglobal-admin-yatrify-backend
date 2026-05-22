package com.yatrify.booking.service;

import com.yatrify.booking.dto.BookingDto;
import com.yatrify.booking.dto.CreateBookingRequest;
import com.yatrify.booking.model.Booking;
import com.yatrify.booking.model.BookingTraveler;
import com.yatrify.booking.repository.BookingRepository;
import com.yatrify.common.PageResponse;
import com.yatrify.common.exception.BusinessException;
import com.yatrify.common.exception.ResourceNotFoundException;
import com.yatrify.notification.service.NotificationService;
import com.yatrify.payment.repository.PaymentRepository;
import com.yatrify.trip.model.Trip;
import com.yatrify.trip.repository.TripRepository;
import com.yatrify.user.model.UserProfile;
import com.yatrify.user.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TripRepository tripRepository;
    private final UserProfileService userProfileService;
    private final NotificationService notificationService;
    private final PaymentRepository paymentRepository;

    @Transactional
    public BookingDto createBooking(String genericUserId, CreateBookingRequest request) {
        UserProfile user = userProfileService.getCurrentUserEntity();

        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip", "id", request.getTripId()));

        if (!trip.isAvailable()) {
            throw new BusinessException("This trip is no longer available for booking", "TRIP_UNAVAILABLE");
        }

        int totalSeats = request.getNumAdults() + request.getNumChildren();
        if (trip.getAvailableSeats() < totalSeats) {
            throw new BusinessException(
                    "Only " + trip.getAvailableSeats() + " seats available", "INSUFFICIENT_SEATS");
        }

        // Calculate amounts
        BigDecimal adultTotal = trip.getPricePerPerson().multiply(BigDecimal.valueOf(request.getNumAdults()));
        BigDecimal childTotal = trip.getChildPrice() != null
                ? trip.getChildPrice().multiply(BigDecimal.valueOf(request.getNumChildren()))
                : BigDecimal.ZERO;
        BigDecimal baseAmount = adultTotal.add(childTotal);

        BigDecimal discountAmount = baseAmount.multiply(trip.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = baseAmount.subtract(discountAmount);
        BigDecimal gstAmount = afterDiscount.multiply(trip.getGstPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = afterDiscount.add(gstAmount);

        Booking booking = Booking.builder()
                .bookingReference(generateBookingReference())
                .trip(trip)
                .userProfile(user)
                .organizer(trip.getOrganizer())
                .numAdults(request.getNumAdults())
                .numChildren(request.getNumChildren())
                .numInfants(request.getNumInfants())
                .totalTravelers(totalSeats)
                .baseAmount(baseAmount)
                .discountAmount(discountAmount)
                .gstAmount(gstAmount)
                .totalAmount(totalAmount)
                .specialRequests(request.getSpecialRequests())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        // Save travelers
        List<BookingTraveler> travelers = request.getTravelers().stream()
                .map(t -> BookingTraveler.builder()
                        .booking(savedBooking)
                        .firstName(t.getFirstName())
                        .lastName(t.getLastName())
                        .dateOfBirth(t.getDateOfBirth())
                        .gender(t.getGender())
                        .travelerType(t.getTravelerType())
                        .idType(t.getIdType())
                        .idNumber(t.getIdNumber())
                        .passportNumber(t.getPassportNumber())
                        .passportExpiry(t.getPassportExpiry())
                        .nationality(t.getNationality())
                        .dietaryPreference(t.getDietaryPreference())
                        .medicalConditions(t.getMedicalConditions())
                        .build())
                .collect(Collectors.toList());
        savedBooking.setTravelers(travelers);

        // Reserve seats
        int updated = tripRepository.decrementAvailableSeats(trip.getId(), totalSeats);
        if (updated == 0) {
            throw new BusinessException("Unable to reserve seats, please try again", "SEAT_RESERVATION_FAILED");
        }

        notificationService.sendBookingCreated(savedBooking);

        return mapToDto(bookingRepository.save(savedBooking));
    }

    @Transactional
    public BookingDto confirmBooking(UUID bookingId) {
        Booking booking = getBookingById(bookingId);
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        booking.setConfirmedAt(LocalDateTime.now());
        notificationService.sendBookingConfirmed(booking);
        return mapToDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto cancelBooking(UUID bookingId, String reason, String genericUserId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", bookingId));

        UserProfile user = userProfileService.getCurrentUserEntity();
        if (!booking.getUserProfile().getId().equals(user.getId())) {
            throw new BusinessException("You don't have permission to cancel this booking", "FORBIDDEN", HttpStatus.FORBIDDEN);
        }

        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BusinessException("Booking is already cancelled", "ALREADY_CANCELLED");
        }

        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        booking.setCancelledAt(LocalDateTime.now());

        tripRepository.incrementAvailableSeats(booking.getTrip().getId(), booking.getTotalTravelers());

        notificationService.sendBookingCancelled(booking);
        return mapToDto(bookingRepository.save(booking));
    }

    public PageResponse<BookingDto> getUserBookings(String genericUserId, int page, int size) {
        UserProfile user = userProfileService.getCurrentUserEntity();
        Page<Booking> bookings = bookingRepository.findByUserProfileId(
                user.getId(), PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PageResponse.from(bookings.map(this::mapToDto));
    }

    public BookingDto getBookingByReference(String reference) {
        return bookingRepository.findByBookingReference(reference)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "reference", reference));
    }

    private Booking getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
    }

    private String generateBookingReference() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int random = new Random().nextInt(90000) + 10000;
        return "YTF" + timestamp + random;
    }

    public BookingDto mapToDto(Booking booking) {
        BookingDto.TripSummary tripSummary = null;
        if (booking.getTrip() != null) {
            Trip trip = booking.getTrip();
            tripSummary = BookingDto.TripSummary.builder()
                    .id(trip.getId())
                    .title(trip.getTitle())
                    .coverImageUrl(trip.getCoverImageUrl())
                    .startDate(trip.getStartDate())
                    .endDate(trip.getEndDate())
                    .departureCity(trip.getDepartureCity())
                    .destinations(trip.getDestinations())
                    .organizerName(trip.getOrganizer() != null ? trip.getOrganizer().getOrganizationName() : null)
                    .build();
        }

        List<BookingDto.TravelerDto> travelerDtos = null;
        if (booking.getTravelers() != null) {
            travelerDtos = booking.getTravelers().stream()
                    .map(t -> BookingDto.TravelerDto.builder()
                            .id(t.getId())
                            .firstName(t.getFirstName())
                            .lastName(t.getLastName())
                            .dateOfBirth(t.getDateOfBirth())
                            .gender(t.getGender())
                            .travelerType(t.getTravelerType())
                            .idType(t.getIdType())
                            .idNumber(t.getIdNumber())
                            .dietaryPreference(t.getDietaryPreference())
                            .build())
                    .collect(Collectors.toList());
        }

        return BookingDto.builder()
                .id(booking.getId())
                .bookingReference(booking.getBookingReference())
                .trip(tripSummary)
                .numAdults(booking.getNumAdults())
                .numChildren(booking.getNumChildren())
                .numInfants(booking.getNumInfants())
                .totalTravelers(booking.getTotalTravelers())
                .baseAmount(booking.getBaseAmount())
                .discountAmount(booking.getDiscountAmount())
                .gstAmount(booking.getGstAmount())
                .totalAmount(booking.getTotalAmount())
                .amountPaid(booking.getAmountPaid())
                .status(booking.getStatus())
                .specialRequests(booking.getSpecialRequests())
                .cancellationReason(booking.getCancellationReason())
                .cancelledAt(booking.getCancelledAt())
                .confirmedAt(booking.getConfirmedAt())
                .travelers(travelerDtos)
                .createdAt(booking.getCreatedAt())
                .build();
    }
}
