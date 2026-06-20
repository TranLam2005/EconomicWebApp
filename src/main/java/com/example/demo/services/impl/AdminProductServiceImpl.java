package com.example.demo.services.impl;

import com.example.demo.dtos.reponse.AdminProductImageResponse;
import com.example.demo.dtos.reponse.AdminProductResponse;
import com.example.demo.dtos.reponse.AdminProductVariantResponse;
import com.example.demo.dtos.reponse.ProductImportFileResponse;
import com.example.demo.dtos.reponse.ProductImportFileRowErrorResponse;
import com.example.demo.dtos.request.AdminProductCreateRequest;
import com.example.demo.dtos.request.AdminProductImageRequest;
import com.example.demo.dtos.request.AdminProductUpdateRequest;
import com.example.demo.dtos.request.AdminProductVariantRequest;
import com.example.demo.dtos.request.ProductImportFileRequest;
import com.example.demo.entities.ProductEntity;
import com.example.demo.entities.ProductImage;
import com.example.demo.entities.ProductVariant;
import com.example.demo.repositories.ProductRepository;
import com.example.demo.services.AdminProductService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AdminProductServiceImpl implements AdminProductService {
    private final ProductRepository productRepository;

    public AdminProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AdminProductResponse getProductById(Long id) {
        ProductEntity product = findProductOrThrow(id);
        return toResponse(product);
    }

    @Override
    @Transactional
    public AdminProductResponse createProduct(AdminProductCreateRequest request) {
        String normalizedKey = cleanText(request.getNormalizedKey());
        if (isBlank(normalizedKey)) {
            normalizedKey = makeNormalizedKey(request.getBrand(), request.getProductName());
        }
        if (productRepository.findByNormalizedKey(normalizedKey).isPresent()) {
            throw new IllegalArgumentException("normalizedKey đã tồn tại: " + normalizedKey);
        }

        ProductEntity product = new ProductEntity();
        product.setProductName(request.getProductName());
        product.setPrice(request.getPrice());
        product.setBrand(request.getBrand());
        product.setGender(request.getGender());
        product.setConcentration(request.getConcentration());
        product.setReleaseYear(request.getReleaseYear());
        product.setDescription(request.getDescription());
        product.setNormalizedKey(normalizedKey);
        product.setVariants(buildVariants(request.getVariants(), product, normalizedKey, request.getPrice()));
        product.setImages(buildImages(request.getImages(), product));

        ProductEntity saved = productRepository.saveAndFlush(product);
        restoreVariantValuesAfterPrePersist(saved, request.getVariants());
        restoreImageValuesAfterPrePersist(saved, request.getImages());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AdminProductResponse updateProduct(Long id, AdminProductUpdateRequest request) {
        ProductEntity product = findProductOrThrow(id);

        if (!isBlank(request.getNormalizedKey()) && !request.getNormalizedKey().equals(product.getNormalizedKey())) {
            Optional<ProductEntity> existed = productRepository.findByNormalizedKey(cleanText(request.getNormalizedKey()));
            if (existed.isPresent() && !existed.get().getId().equals(id)) {
                throw new IllegalArgumentException("normalizedKey đã tồn tại: " + request.getNormalizedKey());
            }
            product.setNormalizedKey(cleanText(request.getNormalizedKey()));
        }

        if (!isBlank(request.getProductName())) product.setProductName(request.getProductName());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (!isBlank(request.getBrand())) product.setBrand(request.getBrand());
        if (!isBlank(request.getGender())) product.setGender(request.getGender());
        if (request.getConcentration() != null) product.setConcentration(request.getConcentration());
        if (request.getReleaseYear() != null) product.setReleaseYear(request.getReleaseYear());
        if (!isBlank(request.getDescription())) product.setDescription(request.getDescription());
        product.setUpdatedAt(LocalDateTime.now());

        if (request.getVariants() != null) {
            if (product.getVariants() == null) {
                product.setVariants(new ArrayList<>());
            } else {
                product.getVariants().clear();
            }
            product.getVariants().addAll(buildVariants(request.getVariants(), product, product.getNormalizedKey(), product.getPrice()));
        }

        if (request.getImages() != null) {
            if (product.getImages() == null) {
                product.setImages(new ArrayList<>());
            } else {
                product.getImages().clear();
            }
            product.getImages().addAll(buildImages(request.getImages(), product));
        }

        ProductEntity saved = productRepository.saveAndFlush(product);
        restoreVariantValuesAfterPrePersist(saved, request.getVariants());
        restoreImageValuesAfterPrePersist(saved, request.getImages());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        ProductEntity product = findProductOrThrow(id);
        productRepository.delete(product);
    }

    @Override
    @Transactional
    public void deleteProducts(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("Danh sách id không được rỗng");
        }
        ids.forEach(this::deleteProduct);
    }

    @Override
    @Transactional
    public ProductImportFileResponse importProducts(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File import không được để trống");
        }

        List<ProductImportFileRequest> rows = readImportFile(file);
        List<ProductImportFileRowErrorResponse> errors = new ArrayList<>();
        int success = 0;

        for (int i = 0; i < rows.size(); i++) {
            int rowNumber = i + 2;
            ProductImportFileRequest row = rows.get(i);
            try {
                importOneRow(row);
                success++;
            } catch (Exception e) {
                errors.add(ProductImportFileRowErrorResponse.builder()
                        .rowNumber(rowNumber)
                        .message(e.getMessage())
                        .build());
            }
        }

        return ProductImportFileResponse.builder()
                .totalRows(rows.size())
                .successRows(success)
                .failedRows(errors.size())
                .errors(errors)
                .build();
    }

    private void importOneRow(ProductImportFileRequest row) {
        validateImportRow(row);

        String normalizedKey = cleanText(row.getNormalizedKey());
        if (isBlank(normalizedKey)) {
            normalizedKey = makeNormalizedKey(row.getBrand(), row.getProductName());
        }

        ProductEntity product = productRepository.findByNormalizedKey(normalizedKey)
                .orElseGet(ProductEntity::new);

        product.setProductName(row.getProductName());
        product.setBrand(row.getBrand());
        product.setGender(row.getGender());
        product.setConcentration(row.getConcentration());
        product.setReleaseYear(row.getReleaseYear());
        product.setDescription(row.getDescription());
        product.setNormalizedKey(normalizedKey);
        product.setPrice(row.getPrice());
        product.setUpdatedAt(LocalDateTime.now());

        if (product.getVariants() == null) {
            product.setVariants(new ArrayList<>());
        } else {
            product.getVariants().clear();
        }

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(makeSku(normalizedKey, row.getVolume()));
        variant.setVolumeMl(row.getVolume());
        variant.setVariantName(row.getVolume() == null ? "Mặc định" : row.getVolume() + "ml");
        variant.setPrice(row.getPrice());
        variant.setStockQuantity(row.getStockQuantity() == null ? 0 : row.getStockQuantity());
        variant.setIsActive(true);
        variant.setUpdatedAt(LocalDateTime.now());
        product.getVariants().add(variant);

        if (!isBlank(row.getSecureUrl())) {
            if (product.getImages() == null) {
                product.setImages(new ArrayList<>());
            } else {
                product.getImages().clear();
            }
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setSecureUrl(row.getSecureUrl());
            image.setAltText(row.getProductName());
            image.setIsMain(true);
            image.setSortOrder(1);
            image.setResourceType("image");
            image.setUpdatedAt(LocalDateTime.now());
            product.getImages().add(image);
        }

        ProductEntity saved = productRepository.saveAndFlush(product);
        restoreImportedValuesAfterPrePersist(saved, row);
    }

    private void validateImportRow(ProductImportFileRequest row) {
        if (isBlank(row.getProductName())) throw new IllegalArgumentException("Thiếu name/productName");
        if (isBlank(row.getBrand())) throw new IllegalArgumentException("Thiếu brand");
        if (isBlank(row.getGender())) throw new IllegalArgumentException("Thiếu gender");
        if (isBlank(row.getDescription())) throw new IllegalArgumentException("Thiếu description");
        if (row.getPrice() == null) throw new IllegalArgumentException("Thiếu hoặc sai price");
        if (row.getPrice().compareTo(BigDecimal.ZERO) < 0) throw new IllegalArgumentException("price không được âm");
    }

    private ProductEntity findProductOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm id = " + id));
    }

    private List<ProductVariant> buildVariants(List<AdminProductVariantRequest> requests,
                                               ProductEntity product,
                                               String normalizedKey,
                                               BigDecimal defaultPrice) {
        List<ProductVariant> variants = new ArrayList<>();
        if (requests == null || requests.isEmpty()) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(makeSku(normalizedKey, null));
            variant.setVariantName("Mặc định");
            variant.setPrice(defaultPrice == null ? BigDecimal.ZERO : defaultPrice);
            variant.setStockQuantity(0);
            variant.setIsActive(true);
            variant.setUpdatedAt(LocalDateTime.now());
            variants.add(variant);
            return variants;
        }

        for (AdminProductVariantRequest request : requests) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(isBlank(request.getSku()) ? makeSku(normalizedKey, request.getVolumeMl()) : request.getSku());
            variant.setVolumeMl(request.getVolumeMl());
            variant.setVariantName(isBlank(request.getVariantName()) ? buildVariantName(request.getVolumeMl()) : request.getVariantName());
            variant.setPrice(request.getPrice() == null ? defaultPrice : request.getPrice());
            variant.setStockQuantity(request.getStockQuantity() == null ? 0 : request.getStockQuantity());
            variant.setIsActive(request.getIsActive() == null || request.getIsActive());
            variant.setUpdatedAt(LocalDateTime.now());
            variants.add(variant);
        }
        return variants;
    }

    private List<ProductImage> buildImages(List<AdminProductImageRequest> requests, ProductEntity product) {
        List<ProductImage> images = new ArrayList<>();
        if (requests == null) {
            return images;
        }
        for (int i = 0; i < requests.size(); i++) {
            AdminProductImageRequest request = requests.get(i);
            if (isBlank(request.getSecureUrl())) {
                continue;
            }
            ProductImage image = new ProductImage();
            image.setProduct(product);
            image.setCloudinaryAssetId(request.getCloudinaryAssetId());
            image.setPublicId(request.getPublicId());
            image.setVersionNo(request.getVersionNo());
            image.setResourceType(isBlank(request.getResourceType()) ? "image" : request.getResourceType());
            image.setFormat(request.getFormat());
            image.setSecureUrl(request.getSecureUrl());
            image.setWidth(request.getWidth());
            image.setHeight(request.getHeight());
            image.setBytesSize(request.getBytesSize());
            image.setAltText(request.getAltText());
            image.setIsMain(request.getIsMain() != null ? request.getIsMain() : i == 0);
            image.setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : i + 1);
            image.setUpdatedAt(LocalDateTime.now());
            images.add(image);
        }
        return images;
    }

    private AdminProductResponse toResponse(ProductEntity product) {
        AdminProductResponse response = new AdminProductResponse();
        response.setId(product.getId());
        response.setProductName(product.getProductName());
        response.setPrice(product.getPrice());
        response.setBrand(product.getBrand());
        response.setGender(product.getGender());
        response.setConcentration(product.getConcentration());
        response.setReleaseYear(product.getReleaseYear());
        response.setDescription(product.getDescription());
        response.setNormalizedKey(product.getNormalizedKey());
        response.setCreatedAt(product.getCreatedAt());
        response.setUpdatedAt(product.getUpdatedAt());

        if (product.getVariants() == null) {
            response.setVariants(List.of());
        } else {
            response.setVariants(product.getVariants().stream().map(this::toVariantResponse).toList());
        }

        if (product.getImages() == null) {
            response.setImages(List.of());
        } else {
            response.setImages(product.getImages().stream().map(this::toImageResponse).toList());
        }

        return response;
    }

    private AdminProductVariantResponse toVariantResponse(ProductVariant variant) {
        AdminProductVariantResponse response = new AdminProductVariantResponse();
        response.setId(variant.getId());
        response.setSku(variant.getSku());
        response.setVolumeMl(variant.getVolumeMl());
        response.setVariantName(variant.getVariantName());
        response.setPrice(variant.getPrice());
        response.setStockQuantity(variant.getStockQuantity());
        response.setIsActive(variant.getIsActive());
        return response;
    }

    private AdminProductImageResponse toImageResponse(ProductImage image) {
        AdminProductImageResponse response = new AdminProductImageResponse();
        response.setId(image.getId());
        response.setCloudinaryAssetId(image.getCloudinaryAssetId());
        response.setPublicId(image.getPublicId());
        response.setVersionNo(image.getVersionNo());
        response.setResourceType(image.getResourceType());
        response.setFormat(image.getFormat());
        response.setSecureUrl(image.getSecureUrl());
        response.setWidth(image.getWidth());
        response.setHeight(image.getHeight());
        response.setBytesSize(image.getBytesSize());
        response.setAltText(image.getAltText());
        response.setIsMain(image.getIsMain());
        response.setSortOrder(image.getSortOrder());
        return response;
    }


    private void restoreVariantValuesAfterPrePersist(ProductEntity product, List<AdminProductVariantRequest> requests) {
        if (product.getVariants() == null || product.getVariants().isEmpty() || requests == null || requests.isEmpty()) {
            return;
        }
        int size = Math.min(product.getVariants().size(), requests.size());
        for (int i = 0; i < size; i++) {
            ProductVariant variant = product.getVariants().get(i);
            AdminProductVariantRequest request = requests.get(i);
            if (request.getStockQuantity() != null) variant.setStockQuantity(request.getStockQuantity());
            if (request.getIsActive() != null) variant.setIsActive(request.getIsActive());
            variant.setUpdatedAt(LocalDateTime.now());
        }
    }

    private void restoreImageValuesAfterPrePersist(ProductEntity product, List<AdminProductImageRequest> requests) {
        if (product.getImages() == null || product.getImages().isEmpty() || requests == null || requests.isEmpty()) {
            return;
        }
        int size = Math.min(product.getImages().size(), requests.size());
        for (int i = 0; i < size; i++) {
            ProductImage image = product.getImages().get(i);
            AdminProductImageRequest request = requests.get(i);
            if (request.getIsMain() != null) image.setIsMain(request.getIsMain());
            if (request.getSortOrder() != null) image.setSortOrder(request.getSortOrder());
            if (!isBlank(request.getResourceType())) image.setResourceType(request.getResourceType());
            image.setUpdatedAt(LocalDateTime.now());
        }
    }

    private void restoreImportedValuesAfterPrePersist(ProductEntity product, ProductImportFileRequest row) {
        if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            ProductVariant variant = product.getVariants().get(0);
            variant.setStockQuantity(row.getStockQuantity() == null ? 0 : row.getStockQuantity());
            variant.setIsActive(true);
            variant.setUpdatedAt(LocalDateTime.now());
        }
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            ProductImage image = product.getImages().get(0);
            image.setIsMain(true);
            image.setSortOrder(1);
            image.setResourceType("image");
            image.setUpdatedAt(LocalDateTime.now());
        }
    }

    private List<ProductImportFileRequest> readImportFile(MultipartFile file) {
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new IllegalArgumentException("Không đọc được tên file");
        }
        String lowerName = fileName.toLowerCase(Locale.ROOT);
        if (lowerName.endsWith(".csv")) {
            return readCsv(file);
        }
        if (lowerName.endsWith(".xlsx") || lowerName.endsWith(".xls")) {
            return readExcel(file);
        }
        throw new IllegalArgumentException("Chỉ hỗ trợ file .csv, .xlsx, .xls");
    }

    private List<ProductImportFileRequest> readCsv(MultipartFile file) {
        List<ProductImportFileRequest> rows = new ArrayList<>();
        try (Reader reader = new InputStreamReader(file.getInputStream());
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            for (CSVRecord record : parser) {
                Map<String, String> map = new HashMap<>();
                record.toMap().forEach((key, value) -> map.put(normalizeHeader(key), value));
                if (!isImportMapEmpty(map)) {
                    rows.add(toImportRequest(map));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể đọc file CSV: " + e.getMessage(), e);
        }
        return rows;
    }

    private List<ProductImportFileRequest> readExcel(MultipartFile file) {
        List<ProductImportFileRequest> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) return rows;

            Row headerRow = sheet.getRow(0);
            Map<Integer, String> headerMap = new HashMap<>();
            if (headerRow != null) {
                for (Cell cell : headerRow) {
                    headerMap.put(cell.getColumnIndex(), normalizeHeader(cellToString(cell)));
                }
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                Map<String, String> map = new HashMap<>();
                for (Map.Entry<Integer, String> entry : headerMap.entrySet()) {
                    map.put(entry.getValue(), cellToString(row.getCell(entry.getKey())));
                }
                if (!isImportMapEmpty(map)) {
                    rows.add(toImportRequest(map));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Không thể đọc file Excel: " + e.getMessage(), e);
        }
        return rows;
    }

    private ProductImportFileRequest toImportRequest(Map<String, String> map) {
        return ProductImportFileRequest.builder()
                .productName(first(map, "name", "productname", "product_name", "ten", "tensanpham"))
                .brand(first(map, "brand", "thuonghieu"))
                .gender(first(map, "gender", "gioitinh"))
                .concentration(first(map, "concentration", "nongdo"))
                .releaseYear(parseInteger(first(map, "releaseyear", "release_year", "namphathanh")))
                .description(first(map, "description", "mota"))
                .normalizedKey(first(map, "normalizedkey", "normalized_key", "key"))
                .secureUrl(first(map, "secureurl", "secure_url", "image", "imageurl", "image_url"))
                .volume(parseInteger(first(map, "volume", "volumeml", "volume_ml", "dungtich")))
                .price(parseBigDecimal(first(map, "price", "gia")))
                .stockQuantity(parseInteger(first(map, "stockquantity", "stock_quantity", "soluong", "tonkho")))
                .build();
    }

    private String cellToString(Cell cell) {
        if (cell == null) return null;
        CellType type = cell.getCellType();
        if (type == CellType.STRING) return cell.getStringCellValue();
        if (type == CellType.NUMERIC) return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
        if (type == CellType.BOOLEAN) return String.valueOf(cell.getBooleanCellValue());
        if (type == CellType.FORMULA) return cell.getCellFormula();
        return null;
    }

    private boolean isImportMapEmpty(Map<String, String> map) {
        return map.values().stream().allMatch(this::isBlank);
    }

    private String first(Map<String, String> map, String... keys) {
        for (String key : keys) {
            String value = map.get(normalizeHeader(key));
            if (!isBlank(value)) return value.trim();
        }
        return null;
    }

    private BigDecimal parseBigDecimal(String value) {
        try {
            if (isBlank(value)) return null;
            return new BigDecimal(value.trim().replace(",", ""));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInteger(String value) {
        try {
            if (isBlank(value)) return null;
            return new BigDecimal(value.trim()).intValue();
        } catch (Exception e) {
            return null;
        }
    }

    private String makeNormalizedKey(String brand, String productName) {
        String raw = (brand == null ? "" : brand) + "-" + (productName == null ? "" : productName);
        String normalized = Normalizer.normalize(raw, Normalizer.Form.NFD);
        normalized = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        normalized = normalized.toLowerCase(Locale.ROOT)
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return normalized.isBlank() ? "product-" + System.currentTimeMillis() : normalized;
    }

    private String makeSku(String normalizedKey, Integer volumeMl) {
        if (volumeMl == null) return normalizedKey + "-default";
        return normalizedKey + "-" + volumeMl + "ml";
    }

    private String buildVariantName(Integer volumeMl) {
        if (volumeMl == null) return "Mặc định";
        return volumeMl + "ml";
    }

    private String normalizeHeader(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        normalized = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");
        return normalized.toLowerCase(Locale.ROOT)
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9]", "");
    }

    private String cleanText(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
