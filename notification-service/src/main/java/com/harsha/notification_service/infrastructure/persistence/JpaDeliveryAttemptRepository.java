package com.harsha.notification_service.infrastructure.persistence;

import com.harsha.notification_service.domain.model.entities.DeliveryAttempt;
import com.harsha.notification_service.domain.repository.DeliveryAttemptRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JpaDeliveryAttemptRepository extends JpaRepository<DeliveryAttempt, UUID>, DeliveryAttemptRepository {
}
