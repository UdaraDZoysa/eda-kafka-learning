package com.harsha.payment_service.infrastructure.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.DomainEvent;
import com.harsha.payment_service.application.events.DomainEventPublisher;
import com.harsha.payment_service.infrastructure.inbox.InboxProcessor;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

@Component
public class OutboxEventPublisher implements DomainEventPublisher {
    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;private static final Logger log = LoggerFactory.getLogger(OutboxEventPublisher.class);


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
            log.info("#####Test1");
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
            log.info("#####Test2");
            throw new RuntimeException(
                    "Failed to serialize domain event", e
            );
        }
    }
}
