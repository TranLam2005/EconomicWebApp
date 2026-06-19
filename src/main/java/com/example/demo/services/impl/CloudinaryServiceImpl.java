package com.example.demo.services.impl;

import com.cloudinary.Cloudinary;
import com.example.demo.services.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
  @Autowired
  private final Cloudinary cloudinary;

  public Map<String, Object> uploadImage(MultipartFile file, String folder) {
    try {
      Map<String, Object> options = new HashMap<>();
      options.put("folder", folder);
      options.put("resource_type", "image");

      return cloudinary.uploader().upload(file.getBytes(), options);
    }
    catch (Exception e) {
      throw new RuntimeException("Upload image failed", e);
    }
  }
}
