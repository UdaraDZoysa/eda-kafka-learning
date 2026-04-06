package com.harsha.notification_service.domain.repository;

import com.harsha.notification_service.domain.model.entities.Notification;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {
    Notification save(Notification notification);

    Optional<Notification> findById(UUID notificationId);
}
