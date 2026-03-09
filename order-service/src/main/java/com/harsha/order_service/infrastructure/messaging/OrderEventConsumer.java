package com.harsha.order_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import com.harsha.common.events.EventType;
import com.harsha.common.events.PaymentProcessedEvent;
import com.harsha.order_service.application.service.OrderService;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventConsumer {
    private static Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    public OrderEventConsumer(
            ObjectMapper objectMapper,
            OrderService orderService
    ) {
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    @KafkaListener(topics = "${topic.payment}", groupId = "order-group")
    public void handle(EventEnvelope envelope) {
        if (envelope.eventType() == EventType.PAYMENT_PROCESSED) {
            PaymentProcessedEvent event = objectMapper.convertValue(
                    envelope.payload(),
                    PaymentProcessedEvent.class
            );
            log.info("Received PAYMENT_PROCESSED for orderId={}", event.orderId());

            orderService.handlePaymentResult(event);
        }
    }
}
