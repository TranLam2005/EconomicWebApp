package com.example.demo.services;

import com.example.demo.entities.PaymentEntity;

import java.util.Optional;

public interface PaymentService {
    Optional<PaymentEntity> findPaymentByIdempotencyKey(String idempotencyKey);
}
