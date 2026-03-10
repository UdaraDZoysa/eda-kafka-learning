package com.harsha.order_service.infrastructure.outbox;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxPublisher {
    private final OutboxRepository repository;
    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${topic.order}")
    private String topic;

    @Value("${topic.order.dlt}")
    private String dltTopic;

    public OutboxPublisher(
            OutboxRepository repository,
            KafkaTemplate<String, EventEnvelope> kafkaTemplate,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void publishEvents() throws Exception {
        List<OutboxEvent> events =
                repository.lockNextBatch();

        for (OutboxEvent event : events) {
            try {

                JsonNode payload =
                        objectMapper.readTree(event.getPayload());

                EventEnvelope envelope =
                        new EventEnvelope(
                                UUID.fromString(event.getId()),
                                event.getEventType(),
                                1,
                                Instant.now(),
                                payload
                        );

                kafkaTemplate.send(
                        topic,
                        event.getAggregateId(),
                        envelope
                );

                event.markPublished();

            } catch (Exception ex) {
                event.markAttempt();
                if (event.getRetryCount() > 5){
                    JsonNode payload = objectMapper.readTree(event.getPayload());

                    EventEnvelope envelope = new EventEnvelope(
                            UUID.fromString(event.getId()),
                            event.getEventType(),
                            1,
                            Instant.now(),
                            payload
                    );
                    kafkaTemplate.send(
                            dltTopic,
                            event.getAggregateId(),
                            envelope
                    );
                    event.markPublished();
                }
            }
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    @Modifying
    public void cleanup() {
        repository.deletePublishedOlderThan(
                Instant.now().minus(7, ChronoUnit.DAYS)
        );
    }
}
