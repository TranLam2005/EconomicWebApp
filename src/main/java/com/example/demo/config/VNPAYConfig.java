package com.example.demo.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "vnpay")
public class VNPAYConfig {
  private String tmnCode;
  private String hashSecret;
  private String payUrl;
  private String returnUrl;
  private String ipnUrl;
}
