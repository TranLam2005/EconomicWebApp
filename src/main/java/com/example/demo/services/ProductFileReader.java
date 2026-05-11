package com.example.demo.services;

import com.example.demo.dtos.request.ProductImportFileRequest;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

public interface ProductFileReader {
    Boolean supports(String fileName);
    List<ProductImportFileRequest> readFile(MultipartFile file);
    BigDecimal parseBigDecimal(String value);
    Integer parseInteger(String value);
}
