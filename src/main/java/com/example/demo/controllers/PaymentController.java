package com.example.demo.controllers;
import com.example.demo.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/public/api/payment")
@RequiredArgsConstructor
public class PaymentController {
  private final PaymentService paymentService;

  @GetMapping("/vnpay-return")
  public ResponseEntity<String> vnpayReturn(@RequestParam Map<String, String> params) {
    paymentService.handleVnpayReturn(params);
    return ResponseEntity.ok("The payment has been processed");
  }
}
