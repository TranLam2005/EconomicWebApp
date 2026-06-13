package com.example.demo.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.CartItemEntity;
import com.example.demo.entities.ProductVariantEntity;
import com.example.demo.entities.UserEntity;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {
    List<CartItemEntity> findByUserOrderByCreatedAtAsc(UserEntity user);

    Optional<CartItemEntity> findByUserAndVariant(UserEntity user, ProductVariantEntity variant);
}