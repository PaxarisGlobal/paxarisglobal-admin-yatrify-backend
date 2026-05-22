package com.yatrify.notification.service;

import com.yatrify.booking.model.Booking;
import com.yatrify.notification.model.Notification;
import com.yatrify.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendBookingCreated(Booking booking) {
        createNotification(
                booking.getUserProfile().getId(),
                "Booking Created",
                "Your booking " + booking.getBookingReference() + " for " + booking.getTrip().getTitle() + " has been created. Please complete payment.",
                "BOOKING_CREATED",
                "BOOKING",
                booking.getId()
        );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendBookingConfirmed(Booking booking) {
        createNotification(
                booking.getUserProfile().getId(),
                "Booking Confirmed!",
                "Great news! Your booking " + booking.getBookingReference() + " for " + booking.getTrip().getTitle() + " is confirmed. Get ready for an amazing trip!",
                "BOOKING_CONFIRMED",
                "BOOKING",
                booking.getId()
        );
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void sendBookingCancelled(Booking booking) {
        createNotification(
                booking.getUserProfile().getId(),
                "Booking Cancelled",
                "Your booking " + booking.getBookingReference() + " has been cancelled. Refund will be processed as per cancellation policy.",
                "BOOKING_CANCELLED",
                "BOOKING",
                booking.getId()
        );
    }

    public List<Notification> getUserNotifications(UUID userProfileId) {
        return notificationRepository.findByUserProfileIdOrderByCreatedAtDesc(userProfileId);
    }

    @Transactional
    public void markAsRead(UUID notificationId, UUID userProfileId) {
        notificationRepository.findByIdAndUserProfileId(notificationId, userProfileId)
                .ifPresent(n -> {
                    n.setIsRead(true);
                    notificationRepository.save(n);
                });
    }

    @Transactional
    public void markAllAsRead(UUID userProfileId) {
        notificationRepository.markAllReadByUserId(userProfileId);
    }

    public long getUnreadCount(UUID userProfileId) {
        return notificationRepository.countByUserProfileIdAndIsReadFalse(userProfileId);
    }

    private void createNotification(UUID userProfileId, String title, String body,
                                    String type, String category, UUID referenceId) {
        Notification notification = Notification.builder()
                .userProfileId(userProfileId)
                .title(title)
                .body(body)
                .type(type)
                .category(category)
                .referenceId(referenceId)
                .build();
        notificationRepository.save(notification);
        log.debug("Notification sent to user {}: {}", userProfileId, title);
    }
}
