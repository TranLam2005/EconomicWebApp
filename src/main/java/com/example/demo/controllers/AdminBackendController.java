package com.example.demo.controllers;

import com.example.demo.entities.*;
import com.example.demo.enums.DiscountType;
import com.example.demo.enums.OrderStatus;
import com.example.demo.repositories.*;
import com.example.demo.services.BrandService;
import com.example.demo.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Locale;

@Controller
@RequiredArgsConstructor
@RequestMapping("/local-admin")
public class AdminBackendController {
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final DiscountRepository discountRepository;
    private final BlogRepository blogRepository;
    private final CartItemRepository cartItemRepository;
    private final FavoriteRepository favoriteRepository;
    private final CategoryService categoryService;
    private final BrandService brandService;

    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("productCount", productRepository.count());
        model.addAttribute("categoryCount", categoryService.findAll().size());
        model.addAttribute("brandCount", brandService.findAll().size());
        model.addAttribute("userCount", userRepository.count());
        model.addAttribute("orderCount", orderRepository.count());
        model.addAttribute("paymentCount", paymentRepository.count());
        model.addAttribute("discountCount", discountRepository.count());
        model.addAttribute("blogCount", blogRepository.count());
        model.addAttribute("cartCount", cartItemRepository.count());
        model.addAttribute("favoriteCount", favoriteRepository.count());
        model.addAttribute("recentOrders", orderRepository.findAll().stream().limit(5).toList());
        model.addAttribute("recentProducts", productRepository.findAll().stream().limit(5).toList());
        return "pages/admin/backend-dashboard";
    }

    @GetMapping("/catalog")
    public String catalog(@RequestParam(value = "categoryId", required = false) Long categoryId,
                          @RequestParam(value = "brandId", required = false) Long brandId,
                          Model model) {
        CategoryEntity categoryForm = new CategoryEntity();
        if (categoryId != null) {
            categoryForm = categoryService.findById(categoryId).orElse(new CategoryEntity());
        }

        BrandEntity brandForm = new BrandEntity();
        if (brandId != null) {
            brandForm = brandService.findById(brandId).orElse(new BrandEntity());
        }

        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("brands", brandService.findAll());
        model.addAttribute("categoryForm", categoryForm);
        model.addAttribute("brandForm", brandForm);
        return "pages/admin/catalog-management";
    }

    @PostMapping("/catalog/categories/save")
    public String saveCategory(@ModelAttribute("categoryForm") CategoryEntity category,
                               RedirectAttributes redirectAttributes) {
        try {
            if (category.getSlug() == null || category.getSlug().isBlank()) {
                category.setSlug(toSlug(category.getName()));
            }
            if (category.getId() == null) {
                categoryService.create(category);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm danh mục thành công");
            } else {
                categoryService.update(category.getId(), category);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật danh mục thành công");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi danh mục: " + e.getMessage());
        }
        return "redirect:/local-admin/catalog";
    }

    @PostMapping("/catalog/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa danh mục: " + e.getMessage());
        }
        return "redirect:/local-admin/catalog";
    }

    @PostMapping("/catalog/brands/save")
    public String saveBrand(@ModelAttribute("brandForm") BrandEntity brand,
                            RedirectAttributes redirectAttributes) {
        try {
            if (brand.getSlug() == null || brand.getSlug().isBlank()) {
                brand.setSlug(toSlug(brand.getName()));
            }
            if (brand.getId() == null) {
                brandService.save(brand);
                redirectAttributes.addFlashAttribute("successMessage", "Thêm thương hiệu thành công");
            } else {
                brandService.update(brand.getId(), brand);
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật thương hiệu thành công");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi thương hiệu: " + e.getMessage());
        }
        return "redirect:/local-admin/catalog";
    }

    @PostMapping("/catalog/brands/{id}/delete")
    public String deleteBrand(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            brandService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa thương hiệu thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa thương hiệu: " + e.getMessage());
        }
        return "redirect:/local-admin/catalog";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "pages/admin/user-management";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "pages/admin/order-management";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam OrderStatus orderStatus,
                                    RedirectAttributes redirectAttributes) {
        try {
            OrderEntity order = orderRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng"));
            order.setOrderStatus(orderStatus);
            orderRepository.save(order);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái đơn hàng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật đơn hàng: " + e.getMessage());
        }
        return "redirect:/local-admin/orders";
    }

    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("payments", paymentRepository.findAll());
        return "pages/admin/payment-management";
    }

    @GetMapping("/discounts")
    public String discounts(@RequestParam(value = "id", required = false) Long id, Model model) {
        DiscountEntity discountForm = new DiscountEntity();
        if (id != null) {
            discountForm = discountRepository.findById(id).orElse(new DiscountEntity());
        }
        model.addAttribute("discounts", discountRepository.findAll());
        model.addAttribute("discountForm", discountForm);
        model.addAttribute("discountTypes", DiscountType.values());
        return "pages/admin/discount-management";
    }

    @PostMapping("/discounts/save")
    public String saveDiscount(@RequestParam(value = "id", required = false) String id,
                               @RequestParam String discountCode,
                               @RequestParam String discountName,
                               @RequestParam DiscountType discountType,
                               @RequestParam BigDecimal discountValue,
                               @RequestParam(value = "minOrderAmount", required = false) BigDecimal minOrderAmount,
                               @RequestParam(value = "maxDiscountAmount", required = false) BigDecimal maxDiscountAmount,
                               @RequestParam(value = "active", required = false) String active,
                               @RequestParam(value = "startAt", required = false) String startAt,
                               @RequestParam(value = "endAt", required = false) String endAt,
                               RedirectAttributes redirectAttributes) {
        try {
            Long discountId = (id == null || id.isBlank()) ? null : Long.parseLong(id);
            DiscountEntity discount = discountId == null
                    ? new DiscountEntity()
                    : discountRepository.findById(discountId).orElse(new DiscountEntity());
            discount.setDiscountCode(discountCode);
            discount.setDiscountName(discountName);
            discount.setDiscountType(discountType);
            discount.setDiscountValue(discountValue);
            discount.setMinOrderAmount(minOrderAmount);
            discount.setMaxDiscountAmount(maxDiscountAmount);
            discount.setActive("on".equalsIgnoreCase(active) || "true".equalsIgnoreCase(active));
            discount.setStartAt(parseDateTime(startAt));
            discount.setEndAt(parseDateTime(endAt));
            discountRepository.save(discount);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu mã giảm giá thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể lưu mã giảm giá: " + e.getMessage());
        }
        return "redirect:/local-admin/discounts";
    }

    @PostMapping("/discounts/{id}/delete")
    public String deleteDiscount(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            discountRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa mã giảm giá thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa mã giảm giá: " + e.getMessage());
        }
        return "redirect:/local-admin/discounts";
    }

    @GetMapping("/blogs")
    public String blogs(Model model) {
        model.addAttribute("blogs", blogRepository.findAll());
        return "pages/admin/blog-management";
    }

    @PostMapping("/blogs/{id}/delete")
    public String deleteBlog(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            blogRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa bài viết thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa bài viết: " + e.getMessage());
        }
        return "redirect:/local-admin/blogs";
    }

    @GetMapping("/backend-map")
    public String backendMap() {
        return "pages/admin/backend-map";
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isBlank()) return null;
        return LocalDateTime.parse(value);
    }

    private String toSlug(String value) {
        if (value == null) return null;
        return value.trim().toLowerCase(Locale.ROOT)
                .replaceAll("[đ]", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-");
    }
}
