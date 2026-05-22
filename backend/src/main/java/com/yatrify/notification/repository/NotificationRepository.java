package com.yatrify.notification.repository;

import com.yatrify.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserProfileIdOrderByCreatedAtDesc(UUID userProfileId);

    Optional<Notification> findByIdAndUserProfileId(UUID id, UUID userProfileId);

    long countByUserProfileIdAndIsReadFalse(UUID userProfileId);

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userProfileId = :userId")
    void markAllReadByUserId(@Param("userId") UUID userId);
}
