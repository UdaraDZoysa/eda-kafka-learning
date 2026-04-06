package com.harsha.order_service.domain.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "orders")
public class Order {
    @Id
    private String id;
    private String userId;
    private String product;
    private int quantity;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private Instant createdAt;

    protected Order() {}

    public Order(String id, String userId, String product, int quantity) {
        this.id = id;
        this.userId = userId;
        this.product = product;
        this.quantity = quantity;
        this.status = OrderStatus.CREATED;
        this.createdAt = Instant.now();
    }
    public String getId() { return id; }
    public String getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public OrderStatus getStatus() { return status; }

    /* =========================
       State Transitions
       ========================= */

    public void markPaymentPending() {
        if (this.status == OrderStatus.PAYMENT_PENDING) {
            return; // idempotent
        }
        if (this.status != OrderStatus.CREATED) {
            throw new IllegalStateException(
                    "Only CREATED orders can move to PAYMENT_PENDING"
            );
        }
        this.status = OrderStatus.PAYMENT_PENDING;
    }

    public void complete() {
        System.out.println("Order Status3: " + this.status);
        if (this.status == OrderStatus.COMPLETED) {
            return; // idempotent
        }
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException(
                    "Only PAYMENT_PENDING orders can be completed"
            );
        }
        this.status = OrderStatus.COMPLETED;
    }

    public void fail() {
        if (this.status == OrderStatus.FAILED) {
            return; // idempotent
        }
        // monotonic protection
        if (this.status == OrderStatus.COMPLETED) {
            return; // ignore out-of-order FAILED event
        }
        if (this.status != OrderStatus.PAYMENT_PENDING) {
            throw new IllegalStateException(
                    "Only PAYMENT_PENDING orders can fail"
            );
        }
        this.status = OrderStatus.FAILED;
    }

    public void cancel() {
        if (this.status == OrderStatus.CANCELLED) {
            return; // idempotent
        }
        if (this.status == OrderStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Completed orders cannot be cancelled"
            );
        }
        this.status = OrderStatus.CANCELLED;
    }
}
