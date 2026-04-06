package com.harsha.notification_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import com.harsha.notification_service.infrastructure.inbox.InboxEvent;
import com.harsha.notification_service.infrastructure.inbox.InboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NotificationEventConsumer {
    private final ObjectMapper objectMapper;
    private final InboxRepository inboxRepository;
    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    public NotificationEventConsumer(
            ObjectMapper objectMapper,
            InboxRepository inboxRepository) {
        this.objectMapper = objectMapper;
        this.inboxRepository = inboxRepository;
    }

    @KafkaListener(topics = "${topic.payment}", groupId = "notification-group")
    public void handle(EventEnvelope envelope) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(envelope.payload());
        }catch (Exception e) {
            throw new RuntimeException("Failed to serialize payload", e);
        }

        InboxEvent inboxEvent = new InboxEvent(
                envelope.eventId(),
                envelope.aggregateId(),
                envelope.eventType(),
                payload
        );

        try {
            inboxRepository.save(inboxEvent);
        } catch (DataIntegrityViolationException ex) {
            // duplicate event → already inserted → ignore
            log.debug("Duplicate event ignored: {}", envelope.eventId());
        }
    }
}
