package com.example.demo.repositories;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entities.ProductEntity;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByNormalizedKey(String normalizedKey);
    Optional<ProductEntity> findProductById(Long id);
    Long countByBrandIgnoreCase(String brand);

    Page<ProductEntity> findByGenderIgnoreCase(String gender, Pageable pageable);
}