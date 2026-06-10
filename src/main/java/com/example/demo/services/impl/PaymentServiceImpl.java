package com.example.demo.services.impl;
import com.example.demo.entities.OrderEntity;
import com.example.demo.entities.PaymentEntity;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repositories.PaymentRepository;
import com.example.demo.services.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
  private final PaymentRepository paymentRepository;

  @Override
  public Optional<PaymentEntity> findPaymentByIdempotencyKey(String idempotencyKey) {
    return  paymentRepository.findPaymentByIdempotencyKey(idempotencyKey);
  }

  @Override
  public Optional<PaymentEntity> findPaymentByPaymentCode(String paymentCode) {
    return paymentRepository.findPaymentByPaymentCode(paymentCode);
  }

  @Transactional
  @Override
  public void handleVnpayReturn (Map<String, String> params) {
    String paymentCode = params.get("vnp_TxnRef");
    String responseCode = params.get("vnp_ResponseCode");
    String transactionId = params.get("vnp_TransactionNo");

    PaymentEntity payment = paymentRepository.findPaymentByPaymentCode(paymentCode)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

    if ("00".equals(responseCode)) {
      payment.setStatus(PaymentStatus.SUCCESS);
      payment.setProviderTransactionId(transactionId);
      payment.setPaidAt(LocalDateTime.now());

      OrderEntity order = payment.getOrder();
      order.setOrderStatus(OrderStatus.PAID);
    }   else {
      payment.setStatus(PaymentStatus.FAILED);
      payment.setFailureCode(responseCode);
      payment.setFailureMessage("Payment VNPAY failed");

      OrderEntity order = payment.getOrder();
      order.setOrderStatus(OrderStatus.FAILED);
    }
  }
}
