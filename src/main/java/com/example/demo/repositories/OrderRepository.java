package com.example.demo.repositories;
import com.example.demo.entities.OrderEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity,Long> {
    @NullMarked
    List<OrderEntity> findAll();

    @NullMarked
    Optional<OrderEntity> findById(Long id);
}
