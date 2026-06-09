package com.example.demo.services.impl;
import com.example.demo.dtos.reponse.DiscountResponse;
import com.example.demo.entities.DiscountEntity;
import com.example.demo.enums.DiscountType;
import com.example.demo.repositories.DiscountRepository;
import com.example.demo.services.DiscountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class DiscountServiceImpl implements DiscountService {
  @Autowired
  private DiscountRepository discountRepository;

  @Override
  public Optional<DiscountEntity> findByDiscountCodeIgnoreCase(String discountCode) {
    return discountRepository.findByDiscountCodeIgnoreCase(discountCode);
  }

  @Override
  public DiscountResponse applyDiscount (String discountCode, BigDecimal subtotalAmount) {
    DiscountEntity discount = findByDiscountCodeIgnoreCase(discountCode)
            .orElseThrow(() -> new RuntimeException("Invalid discount code"));
    if (!Boolean.TRUE.equals(discount.getActive())) {
      throw new RuntimeException("Discount is not active.");
    }

    LocalDateTime now = LocalDateTime.now();

    if (discount.getStartAt() != null && now.isBefore(discount.getStartAt())) {
      throw new RuntimeException("Discount code is not available yet");
    }

    if (discount.getEndAt() != null && now.isAfter(discount.getEndAt())) {
      throw new RuntimeException("Discount code has expired");
    }

    if (discount.getMinOrderAmount() != null &&
            subtotalAmount.compareTo(discount.getMinOrderAmount()) < 0
    ) {
      throw new RuntimeException("Order amount is not enough to use this discount code");
    }

    BigDecimal discountAmount = calculateDiscountAmount(discount, subtotalAmount);

    BigDecimal finalAmount = subtotalAmount.subtract(discountAmount);

    if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
      finalAmount = BigDecimal.ZERO;
    }

    DiscountResponse response = new DiscountResponse();
    response.setDiscountCode(discount.getDiscountCode());
    response.setSubtotalAmount(subtotalAmount);
    response.setDiscountAmount(discountAmount);
    response.setFinalAmount(finalAmount);
    response.setMessage("Discount applied successfully");

    return response;
  }

  private BigDecimal calculateDiscountAmount(DiscountEntity discount, BigDecimal subtotalAmount) {
    BigDecimal discountAmount;
    if (discount.getDiscountType() == DiscountType.PERCENTAGE) {
      discountAmount = subtotalAmount
              .multiply(discount.getDiscountValue())
              .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
      if (discount.getMaxDiscountAmount() != null &&
              discountAmount.compareTo(discount.getMaxDiscountAmount()) > 0
      ) {
        discountAmount = discount.getMaxDiscountAmount();
      }
    } else if (discount.getDiscountType() == DiscountType.FIX_AMOUNT) {
      discountAmount = discount.getDiscountValue();
      if (discountAmount.compareTo(subtotalAmount) > 0) {
        discountAmount = subtotalAmount;
      }
    } else {
      throw new RuntimeException("Unsupported discount type");
    }
    return discountAmount;
  }
}
