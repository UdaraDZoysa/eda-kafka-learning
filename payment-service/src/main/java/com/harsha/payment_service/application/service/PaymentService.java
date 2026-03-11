package com.harsha.payment_service.application.service;

import com.harsha.common.events.OrderPlacedEvent;
import com.harsha.common.events.PaymentProcessedEvent;
import com.harsha.payment_service.application.events.DomainEventPublisher;
import com.harsha.payment_service.domain.model.Payment;
import com.harsha.payment_service.domain.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {
    private static final Logger log =
            LoggerFactory.getLogger(PaymentService.class);
    private final PaymentRepository repository;
    private final DomainEventPublisher publisher;

    public PaymentService(PaymentRepository repository,
                          DomainEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    @Transactional
    public void handleOrderPlaced(OrderPlacedEvent event) {

        if(repository.existsByOrderId(event.orderId())){
            log.info("Payment already exists for orderId={}", event.orderId());
            return;
        }

        String paymentId = "PAY-" + UUID.randomUUID();

        log.info("Creating payment for orderId={}", event.orderId());

        Payment payment = new Payment(paymentId, event.orderId());
        //simulate success for now
        payment.succeed();
        repository.save(payment);
        PaymentProcessedEvent paymentEvent =
                new PaymentProcessedEvent(
                        event.orderId(),
                        true
                );
            publisher.publish(
                    event.orderId(),
                    paymentEvent
            );
        log.info("Payment processed for orderId={}", event.orderId());
    }
}
