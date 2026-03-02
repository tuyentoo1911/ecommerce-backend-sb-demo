package com.example.javaspringboot.repository;

import com.example.enums.PaymentStatus;
import com.example.javaspringboot.entity.Order;
import com.example.javaspringboot.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {
    List<Payment> findByOrder(Order order);

    Optional<Payment> findByRequestId(String requestId);
}
