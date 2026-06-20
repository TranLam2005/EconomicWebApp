package com.example.demo.controllers;

import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductImageEntity;
import com.example.demo.entities.ProductVariantEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {
  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("menProducts", buildMenProducts());
    model.addAttribute("womenProducts", buildWomenProducts());
    model.addAttribute("unisexProducts", buildUnisexProducts());
    model.addAttribute("articles", buildArticles());
    return "pages/home"; // -> templates/home.html
  }

  // ===== Mock data: Nước hoa Nam =====
  private List<ProductEntity> buildMenProducts() {
    List<ProductEntity> list = new ArrayList<>();
    list.add(mockProduct("Jean Paul Gaultier Le Male Le Parfum", "Jean Paul Gaultier",
            "https://placehold.co/400x400/111111/ffffff?text=JPG+Le+Male",
            325000, 3550000));
    list.add(mockProduct("Creed Aventus For Men EDP", "Creed",
            "https://placehold.co/400x400/222222/ffffff?text=Creed+Aventus",
            795000, 7800000));
    list.add(mockProduct("Creed Wild Vetiver EDP (2026)", "Creed",
            "https://placehold.co/400x400/2e7d32/ffffff?text=Wild+Vetiver",
            785000, 7300000));
    list.add(mockProduct("Chanel Bleu De Chanel EDP", "Chanel",
            "https://placehold.co/400x400/1a1a2e/ffffff?text=Bleu+De+Chanel",
            455000, 4380000));
    list.add(mockProduct("Versace Pour Homme EDT", "Versace",
            "https://placehold.co/400x400/cfd8dc/333333?text=Versace+Homme",
            265000, 2180000));
    return list;
  }

  // ===== Mock data: Nước hoa Nữ =====
  private List<ProductEntity> buildWomenProducts() {
    List<ProductEntity> list = new ArrayList<>();
    list.add(mockProduct("Parfums De Marly Valaya EDP", "Parfums De Marly",
            "https://placehold.co/400x400/f5f5f5/333333?text=Valaya",
            955000, 6800000));
    list.add(mockProduct("Le Labo Another 13 EDP", "Le Labo",
            "https://placehold.co/400x400/f0ead6/333333?text=Another+13",
            795000, 8500000));
    list.add(mockProduct("Parfums De Marly Delina EDP", "Parfums De Marly",
            "https://placehold.co/400x400/f8d7e3/333333?text=Delina",
            875000, 6450000));
    list.add(mockProduct("Chanel Coco Mademoiselle EDP", "Chanel",
            "https://placehold.co/400x400/f4e1c1/333333?text=Coco+Mademoiselle",
            485000, 4950000));
    list.add(mockProduct("Jean Paul Gaultier Scandal EDP", "Jean Paul Gaultier",
            "https://placehold.co/400x400/eec9d2/333333?text=Scandal",
            395000, 3200000));
    return list;
  }

  // ===== Mock data: Nước hoa Unisex =====
  private List<ProductEntity> buildUnisexProducts() {
    List<ProductEntity> list = new ArrayList<>();
    list.add(mockProduct("Louis Vuitton Imagination EDP", "Louis Vuitton",
            "https://placehold.co/400x400/cce5e0/333333?text=Imagination",
            1150000, 10800000));
    list.add(mockProduct("Diptyque Orphéon EDP", "Diptyque",
            "https://placehold.co/400x400/f5f0dc/333333?text=Orpheon",
            725000, 4950000));
    list.add(mockProduct("Initio Musk Therapy Extrait De Parfum", "Initio",
            "https://placehold.co/400x400/ffffff/333333?text=Musk+Therapy",
            955000, 6900000));
    list.add(mockProduct("Tom Ford Ombré Leather EDP", "Tom Ford",
            "https://placehold.co/400x400/1a1a1a/ffffff?text=Ombre+Leather",
            575000, 5900000));
    list.add(mockProduct("Tom Ford Oud Wood EDP", "Tom Ford",
            "https://placehold.co/400x400/3e2c1c/ffffff?text=Oud+Wood",
            775000, 7900000));
    return list;
  }

  private ProductEntity mockProduct(String name, String brand, String imageUrl,
                                    long minPrice, long maxPrice) {
    ProductImageEntity image = ProductImageEntity.builder()
            .secureUrl(imageUrl)
            .altText(name)
            .isMain(true)
            .sortOrder(1)
            .build();

    ProductVariantEntity variantMin = ProductVariantEntity.builder()
            .sku(name.substring(0, Math.min(3, name.length())).toUpperCase() + "-MIN")
            .variantName("Chiết nhỏ")
            .volumeMl(10)
            .price(BigDecimal.valueOf(minPrice))
            .stockQuantity(20)
            .isActive(true)
            .build();

    ProductVariantEntity variantMax = ProductVariantEntity.builder()
            .sku(name.substring(0, Math.min(3, name.length())).toUpperCase() + "-MAX")
            .variantName("Fullbox")
            .volumeMl(100)
            .price(BigDecimal.valueOf(maxPrice))
            .stockQuantity(5)
            .isActive(true)
            .build();

    return ProductEntity.builder()
            .productName(name)
            .brand(brand)
            .gender("Unisex")
            .normalizedKey(name.toLowerCase().replaceAll("[^a-z0-9]+", "-"))
            .description(name + " - " + brand)
            .images(List.of(image))
            .variants(List.of(variantMin, variantMax))
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
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