package com.example.demo.services.impl;

import com.example.demo.entities.PaymentEntity;
import com.example.demo.repositories.PaymentRepository;
import com.example.demo.services.PaymentService;

import java.util.Optional;

public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Optional<PaymentEntity> findPaymentByIdempotencyKey(String idempotencyKey) {
        return  paymentRepository.findPaymentByIdempotencyKey(idempotencyKey);
    }
}
