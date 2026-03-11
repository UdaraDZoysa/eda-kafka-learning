package com.harsha.payment_service.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.DomainEvent;
import com.harsha.payment_service.application.events.DomainEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OutboxEventPublisher implements DomainEventPublisher {
    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(
            OutboxRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(
            String aggregateId,
            DomainEvent event
    ) {
        try {
            String payload =
                    objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent =
                    new OutboxEvent(
                            UUID.randomUUID().toString(),
                            aggregateId,
                            event.type(),
                            payload
                    );
            repository.save(outboxEvent);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to serialize domain event", e
            );
        }
    }
}
