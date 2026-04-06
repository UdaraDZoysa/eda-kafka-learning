package com.harsha.payment_service.infrastructure.inbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import com.harsha.common.events.EventType;
import com.harsha.common.events.OrderPlacedEvent;
import com.harsha.payment_service.application.service.PaymentService;
import jakarta.transaction.Transactional;
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
    private final PaymentService paymentService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;
    private static final Logger log = LoggerFactory.getLogger(InboxProcessor.class);

    @Value("${topic.order.dlt}")
    private String dltTopic;

    public InboxProcessor(
            InboxRepository repository,
            PaymentService paymentService,
            ObjectMapper objectMapper,
            KafkaTemplate<String, EventEnvelope> kafkaTemplate) {
        this.repository = repository;
        this.paymentService = paymentService;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void process() throws Exception {
        List<InboxEvent> events = repository.lockNextBatch();

        for (InboxEvent event : events) {
            try {
                if (event.getEventType() == EventType.ORDER_PLACED) {
                    OrderPlacedEvent e =  objectMapper.readValue(
                            event.getPayload(),
                            OrderPlacedEvent.class
                    );
                    log.info("Received ORDER_PLACED event for orderId={}", e.orderId());
                    paymentService.handleOrderPlaced(e);
                }
                event.markProcessed();
            } catch (JsonProcessingException ex) {
                sendToDLT(event, ex);

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

    private void sendToDLT(InboxEvent event, Exception ex) {
        try {
            EventEnvelope envelope = new EventEnvelope(
                    event.getId(),
                    event.getAggregateId(),
                    event.getEventType(),
                    1,
                    Instant.now(),
                    objectMapper.readTree(event.getPayload()),
                    "payment-service"
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

        } catch (Exception sendEx) {
            log.error("Failed to publish inbox event to DLT → id={}, reason={}",
                    event.getId(),
                    sendEx.getMessage());
            event.markAttempt();
        }
    }

    private void handleRetry(InboxEvent event, Exception ex) {

        long backoff = calculateBackoff(event.getRetryCount());

        if (event.getLastAttemptAt() != null &&
                Instant.now().isBefore(event.getLastAttemptAt().plusMillis(backoff))) {
            return;
        }
        log.error("handleRetry: Failed to publish inbox event to DLT → id={}, reason={}",
                event.getId(),
                ex.getMessage());
        event.markAttempt();

        if (!event.shouldRetry()) {
            sendToDLT(event, ex);
        }
    }
}
