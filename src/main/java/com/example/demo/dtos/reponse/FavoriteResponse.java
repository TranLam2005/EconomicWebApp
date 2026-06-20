package com.example.demo.dtos.reponse;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FavoriteResponse {
    private Long favoriteId;

    private Long productId;

    private String productName;

    private String brand;

    private String gender;

    private String concentration;

    private BigDecimal price;

    private String imageUrl;

    private LocalDateTime createdAt;
}