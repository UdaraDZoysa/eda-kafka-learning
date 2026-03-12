package com.harsha.payment_service.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.harsha.common.events.EventEnvelope;
import com.harsha.common.events.EventType;
import com.harsha.payment_service.application.service.PaymentService;
import com.harsha.payment_service.infrastructure.idempotency.ProcessedEvent;
import com.harsha.payment_service.infrastructure.idempotency.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.harsha.common.events.OrderPlacedEvent;

import java.util.UUID;

@Component
public class PaymentEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(PaymentEventConsumer.class);
    private final ObjectMapper objectMapper;
    private final PaymentService paymentService;
    private final ProcessedEventRepository processedEventRepository;

    public PaymentEventConsumer(
            ObjectMapper objectMapper,
            PaymentService paymentService,
            ProcessedEventRepository processedEventRepository
    ) {
        this.objectMapper = objectMapper;
        this.paymentService = paymentService;
        this.processedEventRepository = processedEventRepository;
    }

    @KafkaListener(topics = "${topic.order}", groupId = "payment-group")
    public void handle(EventEnvelope envelope) {

        UUID eventId = envelope.eventId();

        if (processedEventRepository.existsById(eventId)) {
            return;
        }

        if (envelope.eventType() == EventType.ORDER_PLACED) {
            OrderPlacedEvent event = objectMapper.convertValue(
                    envelope.payload(),
                    OrderPlacedEvent.class
            );
            log.info("Received ORDER_PLACED event for orderId={}", event.orderId());

            paymentService.handleOrderPlaced(event);

            processedEventRepository.save(
                    new ProcessedEvent(eventId)
            );
        }
    }
}
