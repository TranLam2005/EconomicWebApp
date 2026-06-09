package com.example.demo.controllers;

import com.example.demo.dtos.request.DiscountRequest;
import com.example.demo.services.DiscountService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.demo.dtos.reponse.DiscountResponse;

@RestController
@RequestMapping("/private/api/discount/")
public class DiscountController {
  private final DiscountService discountService;
  public DiscountController(DiscountService discountService) {
    this.discountService = discountService;
  }

  @GetMapping("apply")
  public DiscountResponse apply(@RequestBody DiscountRequest request) {
    return discountService.applyDiscount(request.getDiscountCode(), request.getSubtotalAmount());
  }
}
