package com.harsha.order_service.infrastructure.inbox;

import com.harsha.common.events.EventType;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inbox_events")
public class InboxEvent {

    @Id
    private UUID id;

    private String aggregateId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload;

    private boolean processed;

    private int retryCount;

    private Instant createdAt;

    private Instant lastAttemptAt;

    protected InboxEvent() {}

    public InboxEvent(UUID id, String aggregateId, EventType eventType, String payload) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.processed = false;
        this.retryCount = 0;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public EventType getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public boolean isProcessed() { return processed; }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public void markProcessed() {
        this.processed = true;
    }

    public void markAttempt() {
        this.retryCount++;
        this.lastAttemptAt = Instant.now();
    }

    public boolean shouldRetry() {
        return retryCount < 50 &&
                createdAt.plusSeconds(3600).isAfter(Instant.now());
    }
}
