package com.example.demo.config;

import com.example.demo.controllers.CartController;
import com.example.demo.controllers.FavoriteController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(assignableTypes = {CartController.class, FavoriteController.class})
public class ShopApiExceptionHandler {
    @ExceptionHandler({RuntimeException.class, IllegalArgumentException.class, MethodArgumentNotValidException.class})
    public ResponseEntity<Map<String, String>> handleShopApiError(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = "Không thể xử lý yêu cầu. Vui lòng thử lại.";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }
}
