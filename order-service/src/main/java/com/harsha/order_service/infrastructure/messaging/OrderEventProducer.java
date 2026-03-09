package com.harsha.order_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import com.harsha.common.events.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.harsha.common.events.OrderPlacedEvent;

import java.time.Instant;
import java.util.UUID;

@Component
public class OrderEventProducer {
    private static final Logger log =
            LoggerFactory.getLogger(OrderEventProducer.class);
    private final KafkaTemplate<String, EventEnvelope> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String orderTopic;

    public OrderEventProducer(
            KafkaTemplate<String, EventEnvelope> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${topic.order}") String orderTopic
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.orderTopic = orderTopic;
    }
    public void publishOrderPlaced(OrderPlacedEvent event) {

        EventEnvelope envelope = new EventEnvelope(
                UUID.randomUUID(),
                EventType.ORDER_PLACED,
                1,
                Instant.now(),
                objectMapper.valueToTree(event)
        );

        kafkaTemplate.send(orderTopic, event.orderId(), envelope);
        log.info("Published ORDER_PLACED event → orderId={}", event.orderId());
    }
}
