package com.harsha.notification_service.domain.model.entities;

import com.harsha.notification_service.domain.model.enums.NotificationStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notifications",
        uniqueConstraints = {
            @UniqueConstraint(name="uk_notification_event", columnNames="eventId")
        }
)
public class Notification {
    @Id
    private UUID id;

    private String userId;

    private String orderId;

    private UUID eventId;

    private String message;

    private boolean paymentSuccess;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private Instant createdAt;

    protected Notification() {}

    public Notification(
            String userId,
            String orderId,
            UUID eventId,
            String message,
            boolean paymentSuccess
    ) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.orderId = orderId;
        this.eventId = eventId;
        this.message = message;
        this.paymentSuccess = paymentSuccess;
        this.status = NotificationStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }

    public String getUserId() { return userId; }

    public String getOrderId() { return orderId; }

    public String getMessage() { return message; }

    public NotificationStatus getStatus() { return status; }

    public void markSent() {
        this.status = NotificationStatus.SENT;
    }

    public void markFailed() {
        this.status = NotificationStatus.FAILED;
    }
}
