package com.example.demo.dtos.reponse;

import com.example.demo.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    @NotBlank
    private String status;
    @NotBlank
    private String amount;
    @NotBlank
    private PaymentMethod paymentMethod;
    private String paymentUrl;
}
