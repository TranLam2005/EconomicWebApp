package com.example.demo.dtos.reponse;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImportFileRowErrorResponse {
    private Integer rowNumber;
    private String message;
}
