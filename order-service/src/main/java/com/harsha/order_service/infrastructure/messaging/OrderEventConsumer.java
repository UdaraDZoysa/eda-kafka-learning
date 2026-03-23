package com.harsha.order_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import com.harsha.common.events.EventType;
import com.harsha.common.events.PaymentProcessedEvent;
import com.harsha.order_service.application.service.OrderService;
import com.harsha.order_service.infrastructure.idempotency.ProcessedEvent;
import com.harsha.order_service.infrastructure.idempotency.ProcessedEventRepository;
import com.harsha.order_service.infrastructure.inbox.InboxEvent;
import com.harsha.order_service.infrastructure.inbox.InboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderEventConsumer {
    private final ObjectMapper objectMapper;
    private final InboxRepository inboxRepository;

    public OrderEventConsumer(
            ObjectMapper objectMapper,
            InboxRepository inboxRepository
    ) {
        this.objectMapper = objectMapper;
        this.inboxRepository = inboxRepository;
    }

    @KafkaListener(topics = "${topic.payment}", groupId = "order-group")
    public void handle(EventEnvelope envelope) {

        UUID eventId = envelope.eventId();

        if (inboxRepository.existsById(eventId)) {
            return;
        }

        String payload;
        try {
            payload = objectMapper.writeValueAsString(envelope.payload());
        }catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        InboxEvent inboxEvent = new InboxEvent(
                eventId,
                envelope.aggregateId(),
                envelope.eventType(),
                payload
        );
        inboxRepository.save(inboxEvent);
    }
}
