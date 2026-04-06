package com.harsha.notification_service.infrastructure.inbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import com.harsha.common.events.EventType;
import com.harsha.common.events.PaymentProcessedEvent;
import com.harsha.notification_service.application.service.NotificationService;
import jakarta.transaction.Transactional;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class InboxProcessor {
    private final InboxRepository repository;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(InboxProcessor.class);

    @Value("${topic.payment.dlt}")
    private String dltTopic;

    public InboxProcessor(InboxRepository repository,
                          NotificationService notificationService,
                          ObjectMapper objectMapper,
                          KafkaTemplate<String, EventEnvelope> kafkaTemplate
    ) {
        this.repository = repository;
        this.notificationService = notificationService;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }
    @Scheduled(fixedRate = 2000)
    @Transactional
    public void precess() throws Exception {
        List<InboxEvent> events = repository.lockNextBatch();

        for (InboxEvent event : events) {
            try {
                if (event.getEventType() == EventType.PAYMENT_PROCESSED) {
                    PaymentProcessedEvent e =
                            objectMapper.readValue(
                                    event.getPayload(),
                                    PaymentProcessedEvent.class
                            );
                    notificationService.handlePaymentResult(e, event.getId());
                }
                event.markProcessed();
            } catch (JsonProcessingException | SerializationException ex) {
                sendToDlt(event, ex);

            } catch (Exception ex) {
                handleRetry(event, ex);
            }
        }
    }

    private long calculateBackoff(int retryCount) {
        long baseDelay = (long) Math.min(60000, Math.pow(2, retryCount) * 1000);
        double jitter = 0.5 + Math.random();
        return (long) (baseDelay * jitter);
    }

    private void sendToDlt(InboxEvent event, Exception ex) {
        try {
            EventEnvelope envelope = new EventEnvelope(
                    event.getId(),
                    event.getAggregateId(),
                    event.getEventType(),
                    1,
                    Instant.now(),
                    objectMapper.readTree(event.getPayload()),
                    "notification-service"
            );

            log.error("Sending event to DLT. eventId={}, reason={}",
                    event.getId(),
                    ex.getMessage());

            kafkaTemplate.send(
                    dltTopic,
                    event.getAggregateId(),
                    envelope
            ).get();

            event.markProcessed();
        }catch (Exception sendEx) {
            log.error("Failed to publish inbox event to DLT → id={}, reason={}",
                    event.getId(),
                    sendEx.getMessage());
            long backoff = calculateBackoff(event.getRetryCount());

            event.markAttempt(backoff);
        }
    }

    private void handleRetry(InboxEvent event, Exception ex) {
        long backoff = calculateBackoff(event.getRetryCount());

        event.markAttempt(backoff);

        if (!event.shouldRetry()){
            sendToDlt(event, ex);
        }
    }
}
