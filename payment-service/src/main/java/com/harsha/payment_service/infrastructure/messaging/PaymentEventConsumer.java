package com.harsha.payment_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import com.harsha.payment_service.infrastructure.inbox.InboxEvent;
import com.harsha.payment_service.infrastructure.inbox.InboxRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PaymentEventConsumer {
    private final ObjectMapper objectMapper;
    private final InboxRepository inboxRepository;

    public PaymentEventConsumer(
            ObjectMapper objectMapper,
            InboxRepository inboxRepository
    ) {
        this.objectMapper = objectMapper;
        this.inboxRepository = inboxRepository;
    }

    @KafkaListener(topics = "${topic.order}", groupId = "payment-group")
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
