package com.example.demo.services.impl;

import com.example.demo.entities.BrandEntity;
import com.example.demo.repositories.BrandRepository;
import com.example.demo.services.BrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    private final BrandRepository brandRepository;

    @Override
    public List<BrandEntity> findAll() {
        return brandRepository.findAll();
    }

    @Override
    public Optional<BrandEntity> findById(Long id) {
        return brandRepository.findById(id);
    }

    @Override
    public BrandEntity create(BrandEntity brand) {
        validateName(brand.getName());
        brandRepository.findByNameIgnoreCase(brand.getName().trim()).ifPresent(existing -> {
            throw new RuntimeException("Thương hiệu đã tồn tại");
        });
        prepareBrandBeforeSave(brand);
        return brandRepository.save(brand);
    }

    @Override
    public BrandEntity save(BrandEntity brand) {
        if (brand.getId() == null) {
            return create(brand);
        }
        prepareBrandBeforeSave(brand);
        return brandRepository.save(brand);
    }

    @Override
    public BrandEntity update(Long id, BrandEntity brandDetails) {
        BrandEntity brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));

        String newName = brandDetails.getName();
        if (newName != null && !newName.isBlank() && !newName.equalsIgnoreCase(brand.getName())) {
            brandRepository.findByNameIgnoreCase(newName.trim()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Thương hiệu đã tồn tại");
                }
            });
            brand.setName(newName.trim());
        }

        if (brandDetails.getDescription() != null) {
            brand.setDescription(brandDetails.getDescription().trim());
        }
        if (brandDetails.getSlug() != null && !brandDetails.getSlug().isBlank()) {
            brand.setSlug(toSlug(brandDetails.getSlug()));
        } else if (brand.getSlug() == null || brand.getSlug().isBlank()) {
            brand.setSlug(toSlug(brand.getName()));
        }
        if (brandDetails.getActive() != null) {
            brand.setActive(brandDetails.getActive());
        }
        if (brand.getActive() == null) {
            brand.setActive(true);
        }
        return brandRepository.save(brand);
    }


    private void prepareBrandBeforeSave(BrandEntity brand) {
        brand.setName(brand.getName().trim());
        if (brand.getDescription() != null) {
            brand.setDescription(brand.getDescription().trim());
        }
        if (brand.getSlug() == null || brand.getSlug().isBlank()) {
            brand.setSlug(toSlug(brand.getName()));
        } else {
            brand.setSlug(toSlug(brand.getSlug()));
        }
        if (brand.getActive() == null) {
            brand.setActive(true);
        }
    }

    private void validateName(String name) {
        if (name == null || name.trim().isBlank()) {
            throw new RuntimeException("Tên thương hiệu không được để trống");
        }
    }

    private String toSlug(String input) {
        String normalized = Normalizer.normalize(input == null ? "" : input, Normalizer.Form.NFD);
        String withoutMarks = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        return withoutMarks.toLowerCase(Locale.ROOT)
                .replace("đ", "d")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }

    @Override
    public void delete(Long id) {
        if (!brandRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy thương hiệu");
        }
        brandRepository.deleteById(id);
    }
}
