package com.example.demo.controllers;

import com.example.demo.entities.*;
import com.example.demo.enums.DiscountType;
import com.example.demo.enums.OrderStatus;
import com.example.demo.repositories.*;
import com.example.demo.services.BrandService;
import com.example.demo.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
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
    private final PasswordEncoder passwordEncoder;

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
    public String users(@RequestParam(value = "id", required = false) Long id, Model model) {
        UserEntity userForm = new UserEntity();
        if (id != null) {
            userForm = userRepository.findById(id).orElse(new UserEntity());
        }
        if (userForm.getRole() == null || userForm.getRole().isBlank()) {
            userForm.setRole("USER");
        }

        model.addAttribute("users", userRepository.findAll().stream()
                .sorted(Comparator.comparing(UserEntity::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList());
        model.addAttribute("userForm", userForm);
        model.addAttribute("editMode", userForm.getId() != null);
        return "pages/admin/user-management";
    }

    @PostMapping("/users/save")
    public String saveUser(@RequestParam(value = "id", required = false) Long id,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String email,
                           @RequestParam(value = "role", required = false) String role,
                           @RequestParam(value = "password", required = false) String password,
                           RedirectAttributes redirectAttributes) {
        try {
            String normalizedEmail = email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
            if (normalizedEmail.isBlank()) {
                throw new RuntimeException("Email không được để trống");
            }
            if (firstName == null || firstName.isBlank()) {
                throw new RuntimeException("Họ không được để trống");
            }
            if (lastName == null || lastName.isBlank()) {
                throw new RuntimeException("Tên không được để trống");
            }

            UserEntity duplicatedUser = userRepository.findByEmail(normalizedEmail).orElse(null);
            if (duplicatedUser != null && (id == null || !duplicatedUser.getId().equals(id))) {
                throw new RuntimeException("Email đã tồn tại trong hệ thống");
            }

            UserEntity user = id == null
                    ? new UserEntity()
                    : userRepository.findById(id).orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());
            user.setEmail(normalizedEmail);
            user.setRole((role == null || role.isBlank()) ? "USER" : role.trim().toUpperCase(Locale.ROOT));

            if (password != null && !password.isBlank()) {
                user.setPassword(passwordEncoder.encode(password));
            } else if (id == null) {
                throw new RuntimeException("Mật khẩu không được để trống khi thêm tài khoản mới");
            }

            userRepository.save(user);
            redirectAttributes.addFlashAttribute("successMessage", id == null
                    ? "Thêm khách hàng thành công"
                    : "Cập nhật khách hàng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể lưu khách hàng: " + e.getMessage());
            return id == null ? "redirect:/local-admin/users" : "redirect:/local-admin/users?id=" + id;
        }
        return "redirect:/local-admin/users";
    }

    @PostMapping("/users/{id}/delete")
    @Transactional
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            UserEntity user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

            cartItemRepository.deleteAll(cartItemRepository.findByUserOrderByCreatedAtAsc(user));
            favoriteRepository.deleteAll(favoriteRepository.findByUserOrderByCreatedAtDesc(user));
            userRepository.delete(user);

            redirectAttributes.addFlashAttribute("successMessage", "Xóa khách hàng thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa khách hàng: " + e.getMessage());
        }
        return "redirect:/local-admin/users";
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
    public String blogs(@RequestParam(value = "id", required = false) Long id, Model model) {
        BlogEntity blogForm = new BlogEntity();
        if (id != null) {
            blogForm = blogRepository.findById(id).orElse(new BlogEntity());
        }

        Long selectedCategoryId = blogForm.getCategory() != null ? blogForm.getCategory().getId() : null;
        Long selectedAuthorId = blogForm.getAuthor() != null ? blogForm.getAuthor().getId() : null;

        model.addAttribute("blogs", blogRepository.findAll().stream()
                .sorted(Comparator.comparing(BlogEntity::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList());
        model.addAttribute("blogForm", blogForm);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("selectedCategoryId", selectedCategoryId);
        model.addAttribute("selectedAuthorId", selectedAuthorId);
        return "pages/admin/blog-management";
    }

    @GetMapping("/blogs/detail/{id}")
    public String blogDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            BlogEntity blog = blogRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
            model.addAttribute("blog", blog);
            return "pages/admin/blog-detail";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể mở chi tiết bài viết: " + e.getMessage());
            return "redirect:/local-admin/blogs";
        }
    }

    @PostMapping("/blogs/save")
    public String saveBlog(@RequestParam(value = "id", required = false) Long id,
                           @RequestParam String title,
                           @RequestParam(value = "slug", required = false) String slug,
                           @RequestParam(value = "featuredImage", required = false) String featuredImage,
                           @RequestParam(value = "excerpt", required = false) String excerpt,
                           @RequestParam(value = "content", required = false) String content,
                           @RequestParam(value = "categoryId", required = false) Long categoryId,
                           @RequestParam(value = "authorId", required = false) Long authorId,
                           @RequestParam(value = "isPublished", required = false) String isPublished,
                           RedirectAttributes redirectAttributes) {
        try {
            if (title == null || title.isBlank()) {
                throw new RuntimeException("Tiêu đề bài viết không được để trống");
            }

            if (categoryId == null) {
                throw new RuntimeException("Vui lòng chọn danh mục cho bài viết");
            }
            if (authorId == null) {
                throw new RuntimeException("Vui lòng chọn tác giả cho bài viết");
            }

            CategoryEntity category = categoryService.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("Danh mục không tồn tại"));
            UserEntity author = userRepository.findById(authorId)
                    .orElseThrow(() -> new RuntimeException("Tác giả không tồn tại"));

            BlogEntity blog = id == null
                    ? new BlogEntity()
                    : blogRepository.findById(id).orElse(new BlogEntity());

            blog.setTitle(title.trim());
            blog.setSlug((slug == null || slug.isBlank()) ? toSlug(title) : slug.trim());
            blog.setFeaturedImage(featuredImage == null ? null : featuredImage.trim());
            blog.setContent(content == null ? "" : content);
            blog.setExcerpt((excerpt == null || excerpt.isBlank()) ? makeExcerpt(content) : excerpt.trim());
            blog.setCategory(category);
            blog.setAuthor(author);
            blog.setIsPublished("on".equalsIgnoreCase(isPublished) || "true".equalsIgnoreCase(isPublished));
            if (blog.getViewCount() == null) {
                blog.setViewCount(0);
            }

            blogRepository.save(blog);
            redirectAttributes.addFlashAttribute("successMessage", id == null
                    ? "Thêm bài viết thành công"
                    : "Cập nhật bài viết thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể lưu bài viết: " + e.getMessage());
        }
        return "redirect:/local-admin/blogs";
    }

    @PostMapping("/blogs/{id}/toggle")
    public String toggleBlogPublished(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            BlogEntity blog = blogRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy bài viết"));
            blog.setIsPublished(!Boolean.TRUE.equals(blog.getIsPublished()));
            blogRepository.save(blog);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái bài viết thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật trạng thái: " + e.getMessage());
        }
        return "redirect:/local-admin/blogs";
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

    private String makeExcerpt(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String plainText = content.replaceAll("<[^>]*>", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (plainText.length() <= 180) {
            return plainText;
        }
        return plainText.substring(0, 180) + "...";
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
