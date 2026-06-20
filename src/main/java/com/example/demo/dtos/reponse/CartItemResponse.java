package com.example.demo.dtos.reponse;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {
    private Long itemId;

    private Long variantId;

    private Long productId;

    private String productName;

    private String brand;

    private String variantName;

    private Integer volumeMl;

    private Integer quantity;

    private Integer stockQuantity;

    private BigDecimal unitPrice;

    private BigDecimal lineTotal;

    private String imageUrl;
}