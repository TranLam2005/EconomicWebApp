package com.example.demo.services;
import com.example.demo.dtos.reponse.DiscountResponse;
import com.example.demo.entities.DiscountEntity;
import java.math.BigDecimal;
import java.util.Optional;

public interface DiscountService {
    Optional<DiscountEntity> findByDiscountCodeIgnoreCase(String discountCode);

    DiscountResponse applyDiscount(String discountCode, BigDecimal subtotalAmount);
}
