package com.harsha.order_service.application.service;

import com.harsha.common.events.OrderPlacedEvent;
import com.harsha.common.events.PaymentProcessedEvent;
import com.harsha.order_service.domain.model.Order;
import com.harsha.order_service.domain.model.OrderStatus;
import com.harsha.order_service.domain.repository.OrderRepository;
import com.harsha.order_service.infrastructure.messaging.OrderEventProducer;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderRepository orderRepository, OrderEventProducer orderEventProducer) {
        this.orderRepository = orderRepository;
        this.orderEventProducer = orderEventProducer;
    }

    @Transactional
    public String placeOrder(String product, int quantity) {
        String orderId = "ORD-" + UUID.randomUUID();
        Order order =
                new Order(
                        orderId,
                        product,
                        quantity
                );
        order.markPaymentPending();
        orderRepository.save(order);
        OrderPlacedEvent event =
                new OrderPlacedEvent(
                        orderId,
                        order.getProduct(),
                        order.getQuantity()
                );
        orderEventProducer.publishOrderPlaced(event);
        return orderId;
    }

    @Transactional
    public void handlePaymentResult(PaymentProcessedEvent event) {
        Order order = orderRepository
                .findById(event.orderId())
                .orElseThrow();
        if (event.success()) {
            if (order.getStatus() == OrderStatus.PAYMENT_PENDING) {
                order.complete();
                orderRepository.save(order);
            }
        } else{
            if (order.getStatus() == OrderStatus.PAYMENT_PENDING) {
                order.fail();
                orderRepository.save(order);
            }
        }
        orderRepository.save(order);
    }
}
