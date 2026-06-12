package com.example.demo.services;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import com.example.demo.dtos.reponse.OrderResponse;
import com.example.demo.dtos.request.OrderRequest;
import com.example.demo.entities.OrderEntity;
import com.example.demo.entities.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

public interface OrderService {
    List<OrderEntity> findAll();

    Optional<OrderEntity> findById(Long id);

    @Transactional
    OrderResponse createOrder(OrderRequest orderRequest, UserEntity user);
}
