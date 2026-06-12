package com.example.demo.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entities.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
}