package com.example.demo.services;

import com.example.demo.entities.PaymentEntity;
import java.util.Map;
import java.util.Optional;

public interface PaymentService {
    Optional<PaymentEntity> findPaymentByIdempotencyKey(String idempotencyKey);
    Optional<PaymentEntity> findPaymentByPaymentCode(String paymentCode);
    void handleVnpayReturn(Map<String, String> params);
}
