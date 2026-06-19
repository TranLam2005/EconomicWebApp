package com.example.demo.services;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CloudinaryService {
    Map <String, Object> uploadImage(MultipartFile file, String folder);
}
