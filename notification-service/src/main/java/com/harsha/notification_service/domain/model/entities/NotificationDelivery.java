package com.harsha.notification_service.domain.model.entities;

import com.harsha.notification_service.domain.model.enums.DeliveryStatus;
import com.harsha.notification_service.domain.model.enums.NotificationChannel;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification_deliveries",
        uniqueConstraints = {
                @UniqueConstraint(
                        name="uk_notification_channel",
                        columnNames={"notificationId","channel"}
                )
            }
        )
public class NotificationDelivery {
    @Id
    private UUID id;

    private UUID notificationId;

    @Enumerated(EnumType.STRING)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    private DeliveryStatus status;

    private Instant createdAt;

    private Instant nextRetryAt;

    private Instant lastAttemptAt;

    private int retryCount;

    protected NotificationDelivery() {}

    public NotificationDelivery(UUID notificationId, NotificationChannel channel) {
        this.id = UUID.randomUUID();
        this.notificationId = notificationId;
        this.channel = channel;
        this.status = DeliveryStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = Instant.now();
        this.nextRetryAt = Instant.now();
    }

    public UUID getId(){ return id; }

    public UUID getNotificationId(){ return notificationId; }

    public int getRetryCount(){ return retryCount; }

    public Instant getLastAttemptAt(){ return lastAttemptAt; }

    public void markSuccess(){
        this.status = DeliveryStatus.SUCCESS;
    }

    public void markFailed(){
        this.status = DeliveryStatus.FAILED;
    }

    public void markFailureAttempt(long backOff){
        this.retryCount++;
        this.lastAttemptAt = Instant.now();
        this.nextRetryAt = Instant.now().plusMillis(backOff);
    }

    public boolean shouldRetry() {
        return retryCount < 50 &&
                createdAt.plusSeconds(3600).isAfter(Instant.now());
    }
}
