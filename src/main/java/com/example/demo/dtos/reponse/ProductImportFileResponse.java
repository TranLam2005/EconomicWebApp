package com.example.demo.dtos.reponse;
import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductImportFileResponse {
    private Integer totalRows;
    private Integer successRows;
    private Integer failedRows;
    private List<ProductImportFileRowErrorResponse> errors;
}
