package com.example.demo.controllers;

import com.example.demo.dtos.reponse.OrderResponse;
import com.example.demo.dtos.request.OrderRequest;
import com.example.demo.entities.UserEntity;
import com.example.demo.services.OrderService;
import com.example.demo.services.ShopUserResolverService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/public/api/order")
@RestController
public class OrderController {
    private final ShopUserResolverService shopUserResolverService;
    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<OrderResponse> create(
            @RequestBody OrderRequest order,
            Authentication authentication,
            HttpServletRequest request,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader
    ) {
        String email = resolveEmail(authentication, emailHeader, order.getCustomerEmail());
        UserEntity user = shopUserResolverService.getOrCreateByEmail(email);

        if (order.getCustomerEmail() == null || order.getCustomerEmail().isBlank()) {
            order.setCustomerEmail(user.getEmail());
        }

        OrderResponse response = orderService.createOrder(order, user, request);
        return ResponseEntity.ok(response);
    }

    private String resolveEmail(Authentication authentication, String emailHeader, String orderEmail) {
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName();
        }
        if (emailHeader != null && !emailHeader.isBlank()) {
            return emailHeader.trim();
        }
        if (orderEmail != null && !orderEmail.isBlank()) {
            return orderEmail.trim();
        }
        return "dang@test.com";
    }
}
