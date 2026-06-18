package com.example.demo.controllers;

import com.example.demo.dtos.reponse.ProductImportFileResponse;
import com.example.demo.services.CloudinaryService;
import com.example.demo.services.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pub/api/file")
@RequiredArgsConstructor
public class FileController {
    private final ProductService productService;
    private final CloudinaryService cloudinaryService;

    @PostMapping(value = "/product/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductImportFileResponse> importProducts(
            @RequestParam("files") MultipartFile file
    ) {
        ProductImportFileResponse response = productService.importProduct(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/image/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "products") String folder
    ) {
        try {
            log.info("Uploading image: {}, size: {}", file.getOriginalFilename(), file.getSize());

            if (file.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "File is empty");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            Map<String, Object> uploadResult = cloudinaryService.uploadImage(file, folder);
            log.info("Upload success: {}", uploadResult.get("secure_url"));

            Map<String, Object> response = new HashMap<>();
            response.put("secure_url", uploadResult.get("secure_url"));
            response.put("public_id", uploadResult.get("public_id"));
            response.put("url", uploadResult.get("url"));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Upload failed: ", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("type", e.getClass().getSimpleName());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}


