package com.example.demo.repositories;
import com.example.demo.entities.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    @Query("select p from PaymentEntity p where p.idempotencyKey = :idempotencyKey")
    Optional<PaymentEntity> findPaymentByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    Optional<PaymentEntity> findPaymentByPaymentCode(String paymentCode);
}
