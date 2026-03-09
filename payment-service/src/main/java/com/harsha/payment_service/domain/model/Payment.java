package com.harsha.payment_service.domain.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    private String id;
    private String orderId;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private Instant createdAt;

    protected Payment() {}
    public Payment(String id, String orderId) {
        this.id = id;
        this.orderId = orderId;
        this.createdAt = Instant.now();
        this.status = PaymentStatus.INITIATED;
    }
    public String getId() { return id; }
    public String getOrderId() { return orderId; }
    public PaymentStatus getStatus() { return status; }

    public void succeed() {
        if (this.status == PaymentStatus.SUCCESS) return;
        if (this.status != PaymentStatus.INITIATED)
            throw new IllegalStateException("Invalid transition");
        this.status = PaymentStatus.SUCCESS;
    }

    public void fail() {
        if (this.status == PaymentStatus.FAILED) return;
        if (this.status != PaymentStatus.INITIATED)
            throw new IllegalStateException("Invalid transition");
        this.status = PaymentStatus.FAILED;
    }

}
