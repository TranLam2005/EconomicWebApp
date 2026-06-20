package com.example.demo.controllers;

import com.example.demo.entities.BrandEntity;
import com.example.demo.entities.CategoryEntity;
import com.example.demo.repositories.BrandRepository;
import com.example.demo.repositories.CategoryRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalNavigationAdvice {
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    @ModelAttribute("navCategories")
    public List<NavigationItem> navCategories() {
        return categoryRepository.findAll().stream()
                .filter(category -> category.getName() != null && !category.getName().isBlank())
                .sorted(Comparator.comparing(category -> category.getName().toLowerCase(Locale.ROOT)))
                .map(category -> new NavigationItem(category.getName(), categoryUrl(category)))
                .toList();
    }

    @ModelAttribute("navBrands")
    public List<NavigationItem> navBrands() {
        return brandRepository.findAll().stream()
                .filter(brand -> brand.getName() != null && !brand.getName().isBlank())
                .filter(brand -> brand.getActive() == null || Boolean.TRUE.equals(brand.getActive()))
                .sorted(Comparator.comparing(brand -> brand.getName().toLowerCase(Locale.ROOT)))
                .map(brand -> new NavigationItem(brand.getName(), "/tim-kiem?brand=" + encode(brand.getName())))
                .toList();
    }

    private String categoryUrl(CategoryEntity category) {
        String name = category.getName();
        String normalized = normalizeVietnamese(name);

        if (normalized.contains("unisex")) {
            return "/tim-kiem?gender=Unisex";
        }
        if (normalized.contains("nuoc hoa nu") || normalized.contains("nu")) {
            return "/tim-kiem?gender=" + encode("Nữ");
        }
        if (normalized.contains("nuoc hoa nam") || normalized.contains("nam")) {
            return "/tim-kiem?gender=Nam";
        }
        return "/tim-kiem?q=" + encode(name);
    }

    private String normalizeVietnamese(String value) {
        if (value == null) return "";
        return value.toLowerCase(Locale.ROOT)
                .replace("đ", "d")
                .replace("á", "a").replace("à", "a").replace("ả", "a").replace("ã", "a").replace("ạ", "a")
                .replace("ă", "a").replace("ắ", "a").replace("ằ", "a").replace("ẳ", "a").replace("ẵ", "a").replace("ặ", "a")
                .replace("â", "a").replace("ấ", "a").replace("ầ", "a").replace("ẩ", "a").replace("ẫ", "a").replace("ậ", "a")
                .replace("é", "e").replace("è", "e").replace("ẻ", "e").replace("ẽ", "e").replace("ẹ", "e")
                .replace("ê", "e").replace("ế", "e").replace("ề", "e").replace("ể", "e").replace("ễ", "e").replace("ệ", "e")
                .replace("í", "i").replace("ì", "i").replace("ỉ", "i").replace("ĩ", "i").replace("ị", "i")
                .replace("ó", "o").replace("ò", "o").replace("ỏ", "o").replace("õ", "o").replace("ọ", "o")
                .replace("ô", "o").replace("ố", "o").replace("ồ", "o").replace("ổ", "o").replace("ỗ", "o").replace("ộ", "o")
                .replace("ơ", "o").replace("ớ", "o").replace("ờ", "o").replace("ở", "o").replace("ỡ", "o").replace("ợ", "o")
                .replace("ú", "u").replace("ù", "u").replace("ủ", "u").replace("ũ", "u").replace("ụ", "u")
                .replace("ư", "u").replace("ứ", "u").replace("ừ", "u").replace("ử", "u").replace("ữ", "u").replace("ự", "u")
                .replace("ý", "y").replace("ỳ", "y").replace("ỷ", "y").replace("ỹ", "y").replace("ỵ", "y");
    }

    private String encode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }

    @Getter
    public static class NavigationItem {
        private final String name;
        private final String url;

        public NavigationItem(String name, String url) {
            this.name = name;
            this.url = url;
        }
    }
}
