package com.example.demo.repositories;

import com.example.demo.entities.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<BrandEntity, Long> {
    Optional<BrandEntity> findByNameIgnoreCase(String name);
    Optional<BrandEntity> findBySlug(String slug);
}
