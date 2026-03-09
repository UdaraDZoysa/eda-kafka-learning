package com.harsha.order_service.infrastructure.persistence;

import com.harsha.order_service.domain.model.Order;
import com.harsha.order_service.domain.repository.OrderRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaOrderRepository
        extends JpaRepository<Order, String>, OrderRepository {
}