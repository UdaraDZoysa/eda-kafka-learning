package com.harsha.payment_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import com.harsha.common.events.OrderPlacedEvent;

@Service
public class PaymentEventConsumer {
    @KafkaListener(topics = "order-events", groupId = "payment-group")
    public void consume(OrderPlacedEvent event) {
        System.out.println("Payment Service received order: "
                + event.orderId()
                + " | product=" + event.product());
    }
}
