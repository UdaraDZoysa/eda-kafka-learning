package com.harsha.notification_service.infrastructure.persistence;

import com.harsha.notification_service.domain.model.entities.NotificationDelivery;
import com.harsha.notification_service.domain.repository.NotificationDeliveryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface JpaNotificationDeliveryRepository
        extends JpaRepository<NotificationDelivery, UUID>, NotificationDeliveryRepository {
    @Override
    @Query(value = """
         SELECT *
            FROM notification_deliveries
            WHERE status = :status
                AND next_retry_at <= CURRENT_TIMESTAMP
            ORDER BY next_retry_at
            LIMIT :limit
            FOR UPDATE SKIP LOCKED
        """,
            nativeQuery = true
    )
    List<NotificationDelivery> lockNextBatch(
            @Param("status") String status,
            @Param("limit") int batchSize);
}
