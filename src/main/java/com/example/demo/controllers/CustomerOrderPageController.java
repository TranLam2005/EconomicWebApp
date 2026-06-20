package com.example.demo.controllers;

import com.example.demo.entities.OrderEntity;
import com.example.demo.entities.OrderItemEntity;
import com.example.demo.entities.PaymentEntity;
import com.example.demo.entities.ProductEntity;
import com.example.demo.enums.OrderStatus;
import com.example.demo.enums.PaymentStatus;
import com.example.demo.repositories.OrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CustomerOrderPageController {

    private final OrderRepository orderRepository;

    @GetMapping({"/don-hang", "/don-hang-cua-toi", "/orders", "/my-orders"})
    public String ordersPage() {
        return "pages/customer-orders";
    }

    @ResponseBody
    @GetMapping("/public/api/orders/my")
    public ResponseEntity<List<CustomerOrderResponse>> myOrders(
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader
    ) {
        String email = resolveEmail(authentication, emailHeader);
        if (email.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        List<CustomerOrderResponse> orders = orderRepository.findCustomerOrdersByEmail(email)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(orders);
    }

    @Transactional
    @ResponseBody
    @PostMapping("/public/api/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            Authentication authentication,
            @RequestHeader(value = "X-User-Email", required = false) String emailHeader
    ) {
        String email = resolveEmail(authentication, emailHeader);
        if (email.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Bạn cần đăng nhập để hủy đơn hàng."));
        }

        OrderEntity order = orderRepository.findById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Không tìm thấy đơn hàng."));
        }

        if (!belongsToEmail(order, email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Bạn không có quyền hủy đơn hàng này."));
        }

        if (!canCancel(order)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Chỉ có thể hủy đơn hàng đang chờ xử lý."));
        }

        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        if (order.getPayments() != null) {
            order.getPayments().forEach(payment -> {
                if (payment.getStatus() == PaymentStatus.PENDING || payment.getStatus() == PaymentStatus.PROCESSING) {
                    payment.setStatus(PaymentStatus.CANCELLED);
                    payment.setFailureMessage("Khách hàng hủy đơn hàng");
                }
            });
        }
        OrderEntity savedOrder = orderRepository.save(order);

        return ResponseEntity.ok(toResponse(savedOrder));
    }

    private String resolveEmail(Authentication authentication, String emailHeader) {
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equals(authentication.getName())) {
            return authentication.getName().trim();
        }
        if (emailHeader != null && !emailHeader.isBlank()) {
            return emailHeader.trim();
        }
        return "";
    }

    private boolean belongsToEmail(OrderEntity order, String email) {
        if (email == null || email.isBlank()) return false;
        String normalizedEmail = email.trim().toLowerCase();

        String customerEmail = order.getCustomerEmail();
        if (customerEmail != null && customerEmail.trim().equalsIgnoreCase(normalizedEmail)) {
            return true;
        }

        if (order.getUser() != null && order.getUser().getEmail() != null) {
            return order.getUser().getEmail().trim().equalsIgnoreCase(normalizedEmail);
        }
        return false;
    }

    private boolean canCancel(OrderEntity order) {
        return order.getOrderStatus() == null || order.getOrderStatus() == OrderStatus.PENDING;
    }

    private CustomerOrderResponse toResponse(OrderEntity order) {
        List<CustomerOrderItemResponse> items = order.getItems() == null
                ? List.of()
                : order.getItems().stream().map(this::toItemResponse).toList();

        String paymentMethod = "";
        String paymentStatus = "";
        if (order.getPayments() != null && !order.getPayments().isEmpty()) {
            PaymentEntity payment = order.getPayments().get(0);
            paymentMethod = payment.getPaymentMethod() == null ? "" : payment.getPaymentMethod().name();
            paymentStatus = payment.getStatus() == null ? "" : payment.getStatus().name();
        }

        String status = order.getOrderStatus() == null ? "PENDING" : order.getOrderStatus().name();
        return new CustomerOrderResponse(
                order.getId(),
                order.getOrderCode(),
                status,
                statusText(status),
                canCancel(order),
                order.getCreatedAt(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getCustomerPhone(),
                order.getShippingAddress(),
                order.getNote(),
                safe(order.getSubtotalAmount()),
                safe(order.getShippingFee()),
                safe(order.getDiscountAmount()),
                safe(order.getTotalAmount()),
                paymentMethod,
                paymentStatus,
                items
        );
    }

    private CustomerOrderItemResponse toItemResponse(OrderItemEntity item) {
        ProductEntity product = item.getProduct();
        String productName = product == null ? "Sản phẩm" : product.getProductName();
        String brand = product == null ? "" : product.getBrand();
        String imageUrl = product == null ? "" : product.getPrimaryImageUrl();
        Long productId = product == null ? null : product.getId();
        Long quantity = item.getQuantity() == null ? 0L : item.getQuantity();
        BigDecimal unitPrice = safe(item.getUnitPrice());
        BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));

        return new CustomerOrderItemResponse(
                item.getId(),
                productId,
                productName,
                brand,
                imageUrl,
                quantity,
                unitPrice,
                lineTotal
        );
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String statusText(String status) {
        return switch (status) {
            case "PAID" -> "Đã thanh toán";
            case "SHIPPED" -> "Đang giao hàng";
            case "CANCELLED" -> "Đã hủy";
            case "FAILED" -> "Thất bại";
            default -> "Chờ xử lý";
        };
    }

    public record CustomerOrderResponse(
            Long id,
            String orderCode,
            String status,
            String statusText,
            boolean cancelable,
            LocalDateTime createdAt,
            String customerName,
            String customerEmail,
            String customerPhone,
            String shippingAddress,
            String note,
            BigDecimal subtotalAmount,
            BigDecimal shippingFee,
            BigDecimal discountAmount,
            BigDecimal totalAmount,
            String paymentMethod,
            String paymentStatus,
            List<CustomerOrderItemResponse> items
    ) {}

    public record CustomerOrderItemResponse(
            Long id,
            Long productId,
            String productName,
            String brand,
            String imageUrl,
            Long quantity,
            BigDecimal unitPrice,
            BigDecimal lineTotal
    ) {}
}
