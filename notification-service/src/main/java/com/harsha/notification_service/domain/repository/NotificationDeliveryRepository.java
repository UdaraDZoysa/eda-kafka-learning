package com.harsha.notification_service.domain.repository;

import com.harsha.notification_service.domain.model.entities.NotificationDelivery;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationDeliveryRepository {
    NotificationDelivery save(NotificationDelivery delivery);

    List<NotificationDelivery> lockNextBatch(
            @Param("status") String status,
            @Param("limit") int batchSize);

    Optional<NotificationDelivery> findById(UUID deliveryId);
}
