package com.harsha.notification_service.infrastructure.persistence;

import com.harsha.notification_service.domain.model.entities.Notification;
import com.harsha.notification_service.domain.repository.NotificationRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaNotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepository {
}
