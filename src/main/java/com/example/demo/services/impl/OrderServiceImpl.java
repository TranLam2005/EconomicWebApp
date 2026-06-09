package com.example.demo.services.impl;

import com.example.demo.dtos.reponse.OrderResponse;
import com.example.demo.dtos.request.OrderItemRequest;
import com.example.demo.dtos.request.OrderRequest;
import com.example.demo.entities.*;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentMethod;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repositories.OrderRepository;
import com.example.demo.services.DiscountService;
import com.example.demo.services.OrderService;
import com.example.demo.services.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {
  private final OrderRepository orderRepository;
  private final ProductService productService;
  private final DiscountService discountService;

  public  OrderServiceImpl(OrderRepository orderRepository,  ProductService productService,  DiscountService discountService) {
    this.orderRepository = orderRepository;
    this.productService = productService;
    this.discountService = discountService;
  }

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
  public OrderResponse createOrder(OrderRequest orderRequest, UserEntity user) {
    // calculate total amount the cost
    BigDecimal totalAmount = calculateTotalAmount(orderRequest);

    // get discount entity
    DiscountEntity discount = discountService.findByDiscountCodeIgnoreCase(orderRequest.getDiscountCode())
            .orElseThrow(() -> new RuntimeException("Can't find discount with your code"));

    // create order entity
    OrderEntity order = OrderEntity.builder()
            .orderCode(UUID.randomUUID().toString())
            .orderStatus(OrderStatus.PENDING)
            .customerEmail(orderRequest.getCustomerEmail())
            .customerName(orderRequest.getCustomerName())
            .customerPhone(orderRequest.getCustomerPhone())
            .discount(discount)
            .subtotalAmount(orderRequest.getSubtotalAmount())
            .discountAmount(discountService.applyDiscount(orderRequest.getDiscountCode(), orderRequest.getSubtotalAmount()).getDiscountAmount())
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
    if (orderRequest.getPaymentMethod() === PaymentMethod.COD) {
      payment.setProvider("COD");
      payment.setPaymentUrl(null);
      payment.setExpiredAt(null);
    } else if (orderRequest.getPaymentMethod() === PaymentMethod.VNPAY) {
      payment.setProvider("VNPAY");
      payment.setExpiredAt(LocalDateTime.now().plusMinutes(15));
    }
    return null;
  }

  // the formula for calculating the total cost = quantity*price + shipping fee - discount
  private BigDecimal calculateTotalAmount(OrderRequest orderRequest) {
    BigDecimal discountAmount;
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
    discountAmount = discountService.applyDiscount(orderRequest.getDiscountCode(), orderRequest.getSubtotalAmount()).getDiscountAmount();
    totalAmount = subtotalAmount
            .add(shippingAmount)
            .subtract(discountAmount);
    return totalAmount;
  }

  private BigDecimal shippingFee(BigDecimal subtotalAmount) {
    if (subtotalAmount.compareTo(BigDecimal.valueOf(1000000)) > 0) return BigDecimal.ZERO;
    return BigDecimal.valueOf(35000);
  }
}
