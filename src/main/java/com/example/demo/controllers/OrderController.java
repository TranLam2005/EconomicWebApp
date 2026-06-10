package com.example.demo.controllers;

import com.example.demo.dtos.reponse.OrderResponse;
import com.example.demo.dtos.request.OrderRequest;
import com.example.demo.entities.UserEntity;
import com.example.demo.services.OrderService;
import com.example.demo.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RequiredArgsConstructor
@RequestMapping("/public/api/order")
@RestController
public class OrderController {
    private final UserService userService;
    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> create(@RequestBody OrderRequest order, Authentication authentication, HttpServletRequest request) {
        // get user from security context
        String email = authentication.getName();
        UserEntity user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        OrderResponse response = orderService.createOrder(order, user, request);
        return ResponseEntity.ok(response);
    }
}
