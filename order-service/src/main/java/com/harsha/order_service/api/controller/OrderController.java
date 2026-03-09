package com.harsha.order_service.api.controller;

import com.harsha.order_service.api.dto.PlaceOrderRequest;
import com.harsha.order_service.application.service.OrderService;
import com.harsha.order_service.infrastructure.messaging.OrderEventProducer;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.harsha.common.events.OrderPlacedEvent;

@RestController
@RequestMapping("/orders")
public class        OrderController {
    private final OrderService orderService;

    public OrderController(final OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody PlaceOrderRequest request) {
        String orderId =
                orderService.placeOrder(request.product(), request.quantity());
        return ResponseEntity.ok("Order created with id: " + orderId);
    }
}
