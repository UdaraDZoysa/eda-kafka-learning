package com.harsha.order_service.infrastructure.inbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import com.harsha.common.events.EventType;
import com.harsha.common.events.PaymentProcessedEvent;
import com.harsha.order_service.application.service.OrderService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@Component
public class InboxProcessor {
    private final InboxRepository repository;
    private final OrderService orderService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;

    @Value("${topic.payment.dlt}")
    private String dltTopic;

    public InboxProcessor(
            InboxRepository repository,
            OrderService orderService,
            ObjectMapper objectMapper,
            KafkaTemplate<String, EventEnvelope> kafkaTemplate) {
        this.repository = repository;
        this.orderService = orderService;
        this.objectMapper = objectMapper;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void process() throws Exception {

        List<InboxEvent> events = repository.lockNextBatch();

        for (InboxEvent event : events) {
            try {
                if (event.getEventType() == EventType.PAYMENT_PROCESSED) {
                    PaymentProcessedEvent e =
                            objectMapper.readValue(
                                    event.getPayload(),
                                    PaymentProcessedEvent.class
                            );
                    orderService.handlePaymentResult(e);
                }
                event.markProcessed();
            } catch (Exception ex) {

                long backoff = calculateBackoff(event.getRetryCount());

                if (event.getLastAttemptAt() != null &&
                        Instant.now().isBefore(event.getLastAttemptAt().plusMillis(backoff))) {
                    continue;
                }

                event.markAttempt();
                if (!event.shouldRetry()) {
                    EventEnvelope envelope = new EventEnvelope(
                            event.getId(),
                            event.getAggregateId(),
                            event.getEventType(),
                            1,
                            Instant.now(),
                            objectMapper.readTree(event.getPayload())
                    );
                    log.error("Inbox event failed permanently → id={}", event.getId());
                    kafkaTemplate.send(
                            dltTopic,
                            event.getAggregateId(),
                            envelope
                    );
                    event.markProcessed();
                }
            }
        }
    }

    private long calculateBackoff(int retryCount) {
        long baseDelay = (long) Math.min(60000, Math.pow(2, retryCount) * 1000);
        double jitter = 0.5 + Math.random();
        return (long) (baseDelay * jitter);
    }
}
