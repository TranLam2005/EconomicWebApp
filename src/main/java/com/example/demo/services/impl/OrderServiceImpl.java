package com.example.demo.services.impl;

import com.example.demo.dtos.reponse.OrderResponse;
import com.example.demo.dtos.request.OrderItemRequest;
import com.example.demo.dtos.request.OrderRequest;
import com.example.demo.entities.*;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repositories.OrderRepository;
import com.example.demo.repositories.PaymentRepository;
import com.example.demo.services.DiscountService;
import com.example.demo.services.OrderService;
import com.example.demo.services.ProductService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
  private final OrderRepository orderRepository;
  private final ProductService productService;
  private final DiscountService discountService;
  private final VnpayServiceImpl  vnpayService;
  private final PaymentRepository paymentRepository;

  @Override
  public List<OrderEntity> findAll() {
    return orderRepository.findAll();
  }

  @Override
  public Optional<OrderEntity> findById(Long id) {
    return orderRepository.findById(id);
  }

  @Transactional
  @Override
  public OrderResponse createOrder(OrderRequest orderRequest, UserEntity user, HttpServletRequest httpRequest) {
    // calculate total amount the cost
    BigDecimal totalAmount = calculateTotalAmount(orderRequest);

    // get discount entity
    BigDecimal discountAmount = BigDecimal.ZERO;
    DiscountEntity discount = null;
    if (orderRequest.getDiscountCode() != null && !orderRequest.getDiscountCode().isEmpty()) {
      discountService.findByDiscountCodeIgnoreCase(orderRequest.getDiscountCode());
      discountAmount = discountService.applyDiscount(orderRequest.getDiscountCode(), totalAmount).getDiscountAmount();
    }

    // create order entity
    OrderEntity order = OrderEntity.builder()
            .orderCode(UUID.randomUUID().toString())
            .orderStatus(OrderStatus.PENDING)
            .customerEmail(orderRequest.getCustomerEmail())
            .customerName(orderRequest.getCustomerName())
            .customerPhone(orderRequest.getCustomerPhone())
            .subtotalAmount(orderRequest.getSubtotalAmount())
            .discount(discount)
            .discountAmount(discountAmount)
            .shippingAddress(orderRequest.getShippingAddress())
            .totalAmount(totalAmount)
            .note(orderRequest.getNote())
            .shippingFee(shippingFee(orderRequest.getSubtotalAmount()))
            .user(user)
            .build();

    List<OrderItemEntity> orderItems = orderRequest.getItems()
            .stream()
            .map(item -> {
              ProductEntity product = productService.findProductById(item.getProductId())
                      .orElseThrow(() -> new RuntimeException("Product not found"));
              BigDecimal price = product.getPrice();

              return OrderItemEntity.builder()
                      .order(order)
                      .product(product)
                      .quantity(item.getQuantity())
                      .unitPrice(price)
                      .build();
            })
            .toList();
    order.setItems(orderItems);
    orderRepository.save(order);

    // create payment
    PaymentEntity payment = PaymentEntity.builder()
            .paymentCode(UUID.randomUUID().toString())
            .amount(totalAmount)
            .idempotencyKey("ORDER_" + order.getId() + "_" + orderRequest.getPaymentMethod())
            .order(order)
            .status(PaymentStatus.PENDING)
            .build();
    if (orderRequest.getPaymentMethod() == PaymentMethod.COD) {
      payment.setProvider("COD");
      payment.setPaymentUrl(null);
      payment.setExpiredAt(null);
    } else if (orderRequest.getPaymentMethod() == PaymentMethod.VNPAY) {
      payment.setProvider("VNPAY");
      payment.setExpiredAt(LocalDateTime.now().plusMinutes(15));

      String paymentUrl = vnpayService.createPaymentUrl(httpRequest, payment);
      payment.setPaymentUrl(paymentUrl);
    }
    paymentRepository.save(payment);
    return new OrderResponse(
            order.getOrderStatus(),
            payment.getPaymentUrl()
    );
  }

  // the formula for calculating the total cost = quantity*price + shipping fee - discount
  private BigDecimal calculateTotalAmount(OrderRequest orderRequest) {
    BigDecimal discountAmount = BigDecimal.ZERO;
    BigDecimal totalAmount;
    BigDecimal shippingAmount;
    BigDecimal subtotalAmount = BigDecimal.ZERO;
    List<OrderItemRequest> items = orderRequest.getItems();

    for (OrderItemRequest item : items) {
      ProductEntity product = productService.findProductById(item.getProductId())
              .orElseThrow(() -> new RuntimeException("Product not found"));
      BigDecimal lineTotal = product.getPrice()
              .multiply(BigDecimal.valueOf(item.getQuantity()));
      subtotalAmount = subtotalAmount.add(lineTotal);
    }
    shippingAmount = shippingFee(subtotalAmount);

    if (orderRequest.getDiscountCode() != null && !orderRequest.getDiscountCode().isEmpty()) {
      discountAmount = calculateDiscountAmount(subtotalAmount, orderRequest.getDiscountCode());
    }

    totalAmount = subtotalAmount
            .add(shippingAmount)
            .subtract(discountAmount);
    return totalAmount;
  }

  private BigDecimal shippingFee(BigDecimal subtotalAmount) {
    if (subtotalAmount.compareTo(BigDecimal.valueOf(1000000)) > 0) return BigDecimal.ZERO;
    return BigDecimal.valueOf(35000);
  }

  private BigDecimal calculateDiscountAmount(BigDecimal subtotalAmount, String discountCode) {
    return discountService.applyDiscount(discountCode, subtotalAmount).getDiscountAmount();
  }
}
