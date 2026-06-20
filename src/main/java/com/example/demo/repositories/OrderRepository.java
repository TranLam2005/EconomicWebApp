package com.example.demo.repositories;
import com.example.demo.entities.OrderEntity;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity,Long> {
    @NullMarked
    List<OrderEntity> findAll();

    @NullMarked
    Optional<OrderEntity> findById(Long id);

    @Query("""
            select distinct o
            from OrderEntity o
            left join fetch o.items item
            left join fetch item.product product
            left join o.user user
            where lower(coalesce(o.customerEmail, '')) = lower(:email)
               or lower(coalesce(user.email, '')) = lower(:email)
            order by o.createdAt desc
            """)
    List<OrderEntity> findCustomerOrdersByEmail(@Param("email") String email);

}
