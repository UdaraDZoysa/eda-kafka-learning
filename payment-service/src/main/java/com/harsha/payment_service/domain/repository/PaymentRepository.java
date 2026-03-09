package com.harsha.payment_service.domain.repository;

import com.harsha.payment_service.domain.model.Payment;

import java.util.Optional;

public interface PaymentRepository {
    Payment save(Payment payment);

    Optional<Payment> findById(String id);

    boolean existsByOrderId(String s);
}
