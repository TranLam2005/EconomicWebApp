package com.example.demo.services.impl;

import com.example.demo.config.VNPAYConfig;
import com.example.demo.entities.PaymentEntity;
import com.example.demo.services.VnpayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VnpayServiceImpl implements VnpayService {
  private final VNPAYConfig vnpayConfig;

  @Override
  public String createPaymentUrl(HttpServletRequest request, PaymentEntity payment) {
    String vnpTxnRef = payment.getPaymentCode();
    String vnpOrderInfo = "Thanh toan don hang" + payment.getOrder().getOrderCode();

    long amount = payment.getAmount()
            .multiply(BigDecimal.valueOf(100))
            .longValue();

    String createDate = LocalDateTime.now()
            .plusMinutes(15)
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String expireDate = LocalDateTime.now()
            .plusMinutes(15)
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    Map<String, String> vnpParams = new HashMap<>();
    vnpParams.put("vnp_Version", "2.1.0");
    vnpParams.put("vnp_Command", "pay");
    vnpParams.put("vnp_TmnCode", vnpayConfig.getTmnCode());
    vnpParams.put("vnp_Amount", String.valueOf(amount));
    vnpParams.put("vnp_CurrCode", "VND");
    vnpParams.put("vnp_TxnRef", vnpTxnRef);
    vnpParams.put("vnp_OrderInfo", vnpOrderInfo);
    vnpParams.put("vnp_OrderType", "other");
    vnpParams.put("vnp_Locale", "vn");
    vnpParams.put("vnp_ReturnUrl", vnpayConfig.getReturnUrl());
    vnpParams.put("vnp_IpAddr", getIpAddress(request));
    vnpParams.put("vnp_CreateDate", createDate);
    vnpParams.put("vnp_ExpireDate", expireDate);
    return "";
  }

  private String getIpAddress(HttpServletRequest request) {
    String ip = request.getHeader("x-forwarded-for");
    if (ip == null || ip.isEmpty()) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }
}
