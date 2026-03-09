package com.harsha.payment_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import com.harsha.common.events.EventType;
import com.harsha.common.events.PaymentProcessedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentEventProducer {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventProducer.class);
    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public PaymentEventProducer(
            KafkaTemplate<String, EventEnvelope> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${topic.payment}") String topic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }
    public void publishPaymentProcessed(PaymentProcessedEvent event) {
        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID(),
                EventType.PAYMENT_PROCESSED,
                1,
                Instant.now(),
                objectMapper.valueToTree(event)
        );
        kafkaTemplate.send(topic, event.orderId(), envelope);

        log.info("Published PAYMENT_PROCESSED event for orderId={}", event.orderId());
    }
}
