package com.harsha.order_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import com.harsha.common.events.EventType;
import com.harsha.common.events.PaymentProcessedEvent;
import com.harsha.order_service.application.service.OrderService;
import com.harsha.order_service.infrastructure.idempotency.ProcessedEvent;
import com.harsha.order_service.infrastructure.idempotency.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class OrderEventConsumer {
    private static Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
    private final ObjectMapper objectMapper;
    private final OrderService orderService;
    private final ProcessedEventRepository processedRepository;

    public OrderEventConsumer(
            ObjectMapper objectMapper,
            OrderService orderService,
            ProcessedEventRepository processedRepository
    ) {
        this.objectMapper = objectMapper;
        this.orderService = orderService;
        this.processedRepository = processedRepository;
    }

    @KafkaListener(topics = "${topic.payment}", groupId = "order-group")
    public void handle(EventEnvelope envelope) {

        UUID eventId = envelope.eventId();

        if (processedRepository.existsById(eventId)){
            return;
        }

        if (envelope.eventType() == EventType.PAYMENT_PROCESSED) {
            PaymentProcessedEvent event = objectMapper.convertValue(
                    envelope.payload(),
                    PaymentProcessedEvent.class
            );
            log.info("Received PAYMENT_PROCESSED for orderId={}", event.orderId());

            orderService.handlePaymentResult(event);

            processedRepository.save(
                    new ProcessedEvent(eventId)
            );
        }
    }
}
