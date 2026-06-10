package com.example.demo.services.impl;

import com.example.demo.config.VNPAYConfig;
import com.example.demo.entities.PaymentEntity;
import com.example.demo.services.VnpayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

    List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
    Collections.sort(fieldNames);

    StringBuilder hashData = new StringBuilder();
    StringBuilder query = new StringBuilder();

    for (String fieldName : fieldNames) {
      // get candidate in vnpParams
      String fieldValue = vnpParams.get(fieldName);
      if (fieldValue != null && !fieldValue.isEmpty()) {
        // add data to hashData, the line data = fieldName(vnp_Version)=urlEncode(fieldValue)
        hashData.append(fieldName)
                .append("=")
                .append(urlEncode(fieldValue));
        // add data to query
        query.append(urlEncode(fieldName))
                .append("=")
                .append(urlEncode(fieldValue));

        // link candidates in vnpParams by the character &
        if (!fieldName.equals(fieldNames.get(fieldNames.size() - 1))) {
          hashData.append("&");
          query.append("&");
        }
      }
    }

    String secureHash = hmacSHA512(vnpayConfig.getHashSecret(), hashData.toString());
    query.append("&vnp_SecureHash=").append(secureHash);
    return vnpayConfig.getPayUrl() + "?" + query;
  }

  private String hmacSHA512(String key, String data) {
    try {
      Mac hmac512 = Mac.getInstance("HmacSHA512");
      SecretKeySpec secretKey = new SecretKeySpec(
              key.getBytes(StandardCharsets.UTF_8),
              "HmacSHA512"
      );
      hmac512.init(secretKey);
      byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));

      StringBuilder hash = new StringBuilder();
      for (byte b : bytes) {
        hash.append(String.format("%02x", b));
      }
      return hash.toString();
    } catch (Exception e) {
      throw new RuntimeException("Cannot generate VNPAY secure hash", e);
    }
  }

  private String urlEncode(String value) {
    try {
      return URLEncoder.encode(value, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String getIpAddress(HttpServletRequest request) {
    String ip = request.getHeader("x-forwarded-for").split(",")[0].trim();
    if (ip.isEmpty()) {
      ip = request.getRemoteAddr();
    }
    return ip;
  }
}
