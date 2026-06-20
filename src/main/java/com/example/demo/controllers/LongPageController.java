package com.example.demo.controllers;

import com.example.demo.entities.ProductEntity;
import com.example.demo.repositories.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Comparator;
import java.util.List;

@Controller
public class LongPageController {
    private final ProductRepository productRepository;

    public LongPageController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping({"/cart", "/gio-hang", "/long/cart", "/long/gio-hang"})
    public String cartPage() {
        return "pages/long-cart";
    }

    @GetMapping({"/checkout", "/thanh-toan", "/long/checkout", "/long/thanh-toan"})
    public String checkoutPage() {
        return "pages/long-checkout";
    }

    @GetMapping({"/wishlist", "/favorites", "/yeu-thich", "/long/wishlist", "/long/yeu-thich"})
    public String wishlistPage() {
        return "pages/long-wishlist";
    }

    @GetMapping({"/search", "/tim-kiem", "/long/search", "/long/tim-kiem"})
    @Transactional(readOnly = true)
    public String searchPage(Model model) {
        List<ProductEntity> products = productRepository.findAll().stream()
                .sorted(Comparator.comparing(ProductEntity::getId, Comparator.nullsLast(Long::compareTo)).reversed())
                .limit(80)
                .toList();
        model.addAttribute("initialProducts", products);
        model.addAttribute("initialProductCount", products.size());
        return "pages/long-search";
    }
}
