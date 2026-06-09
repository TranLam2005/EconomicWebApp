package com.example.demo.repositories;

import com.example.demo.entities.DiscountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DiscountRepository extends JpaRepository<DiscountEntity, Long> {
    Optional<DiscountEntity> findByDiscountCodeIgnoreCase(String discountCode);
}
