package com.example.demo.entities;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Table(name = "order_item")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private OrderEntity order;

    @Column()
    private Long quantity;

    @Column(name = "unit_price")
    private BigDecimal unitPrice;
}
