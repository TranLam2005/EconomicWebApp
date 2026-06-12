package com.example.demo.controllers;

import com.example.demo.dtos.reponse.ProductImportFileResponse;
import com.example.demo.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/pub/api/file")
@RequiredArgsConstructor
public class FileController {
    private final ProductService productService;

    @PostMapping(value = "/product/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImportFileResponse> importProducts(
            @RequestParam("files") MultipartFile file
    ) {
        ProductImportFileResponse response = productService.importProduct(file);
        return ResponseEntity.ok(response);
    }
}

