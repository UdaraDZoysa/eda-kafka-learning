package com.harsha.notification_service.domain.repository;

import com.harsha.notification_service.domain.model.entities.DeliveryAttempt;

public interface DeliveryAttemptRepository {
    DeliveryAttempt save(DeliveryAttempt deliveryAttempt);
}
