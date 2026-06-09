package com.example.demo.dtos.request;

import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class OrderRequest {

  @NotEmpty(message = "Order must contain at least one item")
  @Valid
  private List<OrderItemRequest> items;

  @NotBlank(message = "Customer name is required")
  @Size(max = 100, message = "Customer name must be less than 100 characters")
  private String customerName;

  @Email(message = "Invalid email format")
  @Size(max = 150, message = "Customer email must be less than 150 characters")
  private String customerEmail;

  @NotBlank(message = "Customer phone is required")
  @Size(max = 30, message = "Customer phone must be less than 30 characters")
  private String customerPhone;

  @NotBlank(message = "Shipping address is required")
  private String shippingAddress;

  @NotBlank(message = "Shipping fee is required")
  private BigDecimal shippingFee;

  private String note;

  @NotBlank(message = "Payment method is required")
  private Long paymentMethod;

  @NotBlank
  private BigDecimal subtotalAmount;

  @Nullable
  private String discountCode;
}