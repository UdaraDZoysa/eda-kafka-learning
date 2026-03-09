package com.harsha.order_service.domain.repository;

import com.harsha.order_service.domain.model.Order;

import java.util.Optional;

public interface OrderRepository {
    Order save(Order order);

    Optional<Order> findById(String id);
}
