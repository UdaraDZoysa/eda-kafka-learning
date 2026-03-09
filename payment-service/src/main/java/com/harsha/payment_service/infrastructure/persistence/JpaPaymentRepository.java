package com.harsha.payment_service.infrastructure.persistence;

import com.harsha.payment_service.domain.model.Payment;
import com.harsha.payment_service.domain.repository.PaymentRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, String>, PaymentRepository {
}
