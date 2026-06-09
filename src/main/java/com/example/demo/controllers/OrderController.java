package com.example.demo.controllers;

import com.example.demo.dtos.request.OrderRequest;
import com.example.demo.entities.DiscountEntity;
import com.example.demo.entities.OrderEntity;
import com.example.demo.entities.PaymentEntity;
import com.example.demo.entities.UserEntity;
import com.example.demo.enums.OrderStatus;
import com.example.demo.services.DiscountService;
import com.example.demo.services.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequestMapping("/private/api/order")
@RestController
public class OrderController {
    private final UserService userService;
    private final DiscountService discountService;

    public OrderController(UserService userService,  DiscountService discountService) {
        this.userService = userService;
        this.discountService = discountService;
    }

    @GetMapping
    public String payment() {
        return "";
    }

    @PostMapping("/create")
    public String create(@RequestBody OrderRequest order, Authentication authentication) {
        // get user from security context
        String email = authentication.getName();
        UserEntity user = userService.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // get discount entity
        DiscountEntity discount = discountService.findByDiscountCodeIgnoreCase(order.getDiscountCode())
                .orElseThrow(() -> new RuntimeException("Discount code not found"));

        // initialize

        PaymentEntity payment = new PaymentEntity();

        return "";
    }
}
