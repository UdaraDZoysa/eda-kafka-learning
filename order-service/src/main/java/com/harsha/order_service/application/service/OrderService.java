package com.harsha.order_service.application.service;

import com.harsha.common.events.OrderPlacedEvent;
import com.harsha.common.events.PaymentProcessedEvent;
import com.harsha.order_service.application.events.DomainEventPublisher;
import com.harsha.order_service.domain.model.Order;
import com.harsha.order_service.domain.model.OrderStatus;
import com.harsha.order_service.domain.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final DomainEventPublisher publisher;

    public OrderService(
            OrderRepository orderRepository,
            DomainEventPublisher publisher) {
        this.orderRepository = orderRepository;
        this.publisher = publisher;
    }

    @Transactional
    public String placeOrder(String product, String userId, int quantity) {
        String orderId = "ORD-" + UUID.randomUUID();
        Order order =
                new Order(
                        orderId,
                        userId,
                        product,
                        quantity
                );
        order.markPaymentPending();
        orderRepository.save(order);
        OrderPlacedEvent event =
                new OrderPlacedEvent(
                        orderId,
                        userId,
                        order.getProduct(),
                        order.getQuantity()
                );

       publisher.publish(
               orderId,
               event
       );
        return orderId;
    }

    @Transactional
    public void handlePaymentResult(PaymentProcessedEvent event) {
        Order order = orderRepository
                .findById(event.orderId())
                .orElseThrow(null);
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
