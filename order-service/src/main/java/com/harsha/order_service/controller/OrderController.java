package com.harsha.order_service.controller;

import com.harsha.order_service.kafka.OrderEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.harsha.common.events.OrderPlacedEvent;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderEventProducer producer;

    public OrderController(OrderEventProducer producer) {
        this.producer = producer;
    }

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderPlacedEvent event) {
        producer.publish(event);
        return ResponseEntity.ok("OrderPlacedEvent sent: " + event.orderId());
    }
}
