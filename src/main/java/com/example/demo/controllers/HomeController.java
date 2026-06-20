package com.example.demo.controllers;
import com.example.demo.entities.ProductEntity;
import com.example.demo.services.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
  private final ProductService productService;

  @GetMapping("/testPageable")
  @ResponseBody
  public Page<ProductEntity> testPageable(
          @RequestParam(defaultValue = "0") int page,
          @RequestParam(defaultValue = "12") int size,
          @RequestParam(defaultValue = "Nam") String gender
  ) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("productName").descending());
    return productService.findByGenderIgnoreCase(gender, pageable);
  }

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("menProducts", getProductByGender(0, 10, "Nam"));
    model.addAttribute("womenProducts", getProductByGender(0, 10, "Nữ"));
    model.addAttribute("unisexProducts", getProductByGender(0, 10, "Unisex"));
    model.addAttribute("articles", buildArticles());
    return "pages/home"; // -> templates/home.html
  }

  // ===== Mock data: Nước hoa Nam =====
  private Page<ProductEntity> getProductByGender(int page, int size, String gender) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("productName").descending());
    return productService.findByGenderIgnoreCase(gender, pageable);
  }

  // ===== Mock data: Bài viết / Thông tin =====
  private List<ArticleView> buildArticles() {
    List<ArticleView> list = new ArrayList<>();
    list.add(new ArticleView(
            "Valentine 2026: Chọn nước hoa sớm để không chọn vội",
            "valentine-2026-chon-nuoc-hoa-som",
            "https://placehold.co/400x300/8c1c2b/ffffff?text=Valentine+2026",
            "Nhân viên Parfumerie",
            LocalDateTime.of(2026, 2, 8, 0, 0),
            "14/2 năm nay lại trùng với thời điểm giao mùa, khi năm cũ sắp khép lại để nhường chỗ cho một khởi..."
    ));
    list.add(new ArticleView(
            "Chuẩn bị mùi hương cho Tết 2026: Vì sao nên mua sớm?",
            "chuan-bi-mui-huong-cho-tet-2026",
            "https://placehold.co/400x300/7a1620/ffffff?text=Tet+2026",
            "Nhân viên Parfumerie",
            LocalDateTime.of(2026, 1, 27, 0, 0),
            "Tết 2026 gần kề, việc chọn mùi hương dùng tết từ sớm dần trở thành một thói quen. Không phải vì v..."
    ));
    list.add(new ArticleView(
            "TOP 3 Mùi Hương Phù Hợp Để Hẹn Hò Khi Đông Về",
            "top-3-mui-huong-hen-ho-mua-dong",
            "https://placehold.co/400x300/084c3c/ffffff?text=Top+3+Huong",
            "Nhân viên Parfumerie",
            LocalDateTime.of(2025, 12, 20, 0, 0),
            "Mỗi mùa lạnh trôi qua đều gắn liền với những kỷ niệm rất riêng: những buổi tối trời se lạnh, ánh ..."
    ));
    list.add(new ArticleView(
            "\"Thư Viện Mùi Hương\" - Món Quà Đặc Biệt Dành Cho 20/11",
            "thu-vien-mui-huong-qua-tang-20-11",
            "https://placehold.co/400x300/5c3a21/ffffff?text=Thu+Vien+Mui+Huong",
            "PARFUMERIEVN",
            LocalDateTime.of(2025, 11, 16, 0, 0),
            "Không chỉ là một chai nước hoa đơn lẻ, \"Thư Viện Mùi Hương\" là bộ quà tặng gồm nhiều mùi hương nh..."
    ));
    return list;
  }

  /**
   * View model đơn giản cho bài viết (article/blog).
   * Có thể thay bằng ArticleEntity thật khi đã có bảng dữ liệu tương ứng.
   */
  public record ArticleView(
          String title,
          String slug,
          String thumbnailUrl,
          String author,
          LocalDateTime publishedAt,
          String excerpt
  ) {}
}