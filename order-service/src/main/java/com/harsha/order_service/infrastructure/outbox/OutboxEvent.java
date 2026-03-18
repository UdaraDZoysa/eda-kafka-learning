package com.harsha.order_service.infrastructure.outbox;

import com.harsha.common.events.EventType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    private String id;

    private String aggregateId;

    @Enumerated(EnumType.STRING)
    private EventType eventType;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String payload;

    private Instant createdAt;

    private boolean published;

    private int retryCount;

    private Instant lastAttemptAt;

    protected OutboxEvent() {}

    public OutboxEvent(
            String id,
            String aggregateId,
            EventType eventType,
            String payload
    ) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = Instant.now();
        this.published = false;
        this.retryCount = 0;
    }

    public String getId() {
        return id;
    }

    public String getAggregateId() {
        return aggregateId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getPayload() {
        return payload;
    }

    public boolean isPublished() {
        return published;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void markPublished() {
        published = true;
    }

    public void markAttempt() {
        this.retryCount++;
        this.lastAttemptAt = Instant.now();
    }
}
