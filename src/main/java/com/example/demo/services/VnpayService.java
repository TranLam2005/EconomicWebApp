package com.example.demo.services;

import com.example.demo.entities.PaymentEntity;

import javax.servlet.http.HttpServletRequest;

public interface VnpayService {
  String createPaymentUrl(HttpServletRequest request, PaymentEntity payment);
}
