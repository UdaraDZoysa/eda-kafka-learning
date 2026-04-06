package com.harsha.notification_service.domain.model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "delivery_attempts")
public class DeliveryAttempt {
    @Id
    private UUID id;

    private UUID deliveryId;

    private int attemptNumber;

    private boolean success;

    private String errorMessage;

    private Instant createdAt;

    protected DeliveryAttempt() {}

    public DeliveryAttempt(UUID deliveryId, int attemptNumber, boolean success, String errorMessage){
        this.id = UUID.randomUUID();
        this.deliveryId = deliveryId;
        this.attemptNumber = attemptNumber;
        this.success = success;
        this.errorMessage = errorMessage;
        this.createdAt = Instant.now();
    }
}
