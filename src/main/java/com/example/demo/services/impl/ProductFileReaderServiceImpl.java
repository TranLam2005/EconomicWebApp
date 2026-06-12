package com.example.demo.services.impl;
import com.example.demo.dtos.request.ProductImportFileRequest;
import com.example.demo.dtos.request.ProductImageRequest;
import com.example.demo.dtos.request.ProductVariantRequest;
import com.example.demo.services.ProductFileReaderService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductFileReaderServiceImpl implements ProductFileReaderService {

  @Override
  public Boolean supports(String fileName) {
    if (fileName == null) {
      return false;
    }

    String lowerFileName = fileName.toLowerCase();

    return lowerFileName.endsWith(".csv")
            || lowerFileName.endsWith(".xlsx")
            || lowerFileName.endsWith(".xls");
  }

  @Override
  public List<ProductImportFileRequest> readFileCSV(MultipartFile file) {
    List<ProductImportFileRequest> flatRows = new ArrayList<>();
    try (
            Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8);
            CSVParser csvParser = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .setTrim(true)
                    .build()
                    .parse(reader)
    )   {
      for (CSVRecord record : csvParser) {
        if (isEmptyCSV(record)) {
          continue;
        }
        ProductImportFileRequest dto = ProductImportFileRequest.builder()
                .productName(getValue(record, "name"))
                .brand(getValue(record, "brand"))
                .price(parseBigDecimal(getValue(record, "price")))
                .concentration(getValue(record, "concentration"))
                .description(getValue(record, "description"))
                .gender(getValue(record, "gender"))
                .normalizedKey(getValue(record, "normalizedKey"))
                .releaseYear(parseInteger(getValue(record, "releaseYear")))
                .secureUrl(getValue(record, "secureUrl"))
                .stockQuantity(parseInteger(getValue(record, "stockQuantity")))
                .volumeMl(parseInteger(getValue(record, "volume")))
                .sortOrder(parseInteger(getValue(record, "sortOrder")))
                .isMain(Objects.equals(getValue(record, "isMain"), "true"))
                .altText(getValue(record, "altText"))
                .sku(getValue(record, "sku"))
                .variantName(getValue(record, "variantName"))
                .variantPrice(parseBigDecimal(getValue(record, "variantPrice")))
                .isActive(Objects.equals(getValue(record, "isActive"), "true"))
                .build();
        flatRows.add(dto);
      }
    }   catch (Exception e) {
      throw new RuntimeException("Không thể đọc được file CSV", e);
    }
    return groupToNestedStructure(flatRows);
  }

  @Override
  public List<ProductImportFileRequest> readFileExcel(MultipartFile file) {
    List<ProductImportFileRequest> flatRows = new ArrayList<>();

    try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
      Sheet sheet = workbook.getSheetAt(0);
      if (sheet == null) {
        return flatRows;
      }

      Row headerRow = sheet.getRow(0);
      Map<String, Integer> headerMap = buildHeaderMap(headerRow);

      for (int i = 1; i <= sheet.getLastRowNum(); i++) {
        Row row = sheet.getRow(i);
        if (row == null || isEmptyExcel(row)) {
          continue;
        }

        ProductImportFileRequest dto = ProductImportFileRequest.builder()
                .productName(getCellString(row, getHeaderIndex(headerMap, "name")))
                .brand(getCellString(row, getHeaderIndex(headerMap, "brand")))
                .price(parseBigDecimal(getCellString(row, getHeaderIndex(headerMap, "price"))))
                .concentration(getCellString(row, getHeaderIndex(headerMap, "concentration")))
                .description(getCellString(row, getHeaderIndex(headerMap, "description")))
                .gender(getCellString(row, getHeaderIndex(headerMap, "gender")))
                .normalizedKey(getCellString(row, getHeaderIndex(headerMap, "normalizedKey")))
                .releaseYear(parseInteger(getCellString(row, getHeaderIndex(headerMap, "releaseYear"))))
                .secureUrl(getCellString(row, getHeaderIndex(headerMap, "secureUrl")))
                .stockQuantity(parseInteger(getCellString(row, getHeaderIndex(headerMap, "stockQuantity"))))
                .volumeMl(parseInteger(getCellString(row, getHeaderIndex(headerMap, "volume"))))
                .isMain(Objects.equals(getCellString(row, getHeaderIndex(headerMap, "isMain")), "true"))
                .altText(getCellString(row, getHeaderIndex(headerMap, "altText")))
                .sku(getCellString(row, getHeaderIndex(headerMap, "sku")))
                .variantName(getCellString(row, getHeaderIndex(headerMap, "variantName")))
                .variantPrice(parseBigDecimal(getCellString(row, getHeaderIndex(headerMap, "variantPrice"))))
                .isActive(Objects.equals(getCellString(row, getHeaderIndex(headerMap, "isActive")), "true"))
                .build();
        flatRows.add(dto);
      }
      return groupToNestedStructure(flatRows);
    }   catch (Exception e) {
      throw new RuntimeException("Have problem during read data", e);
    }
  }

  private List<ProductImportFileRequest> groupToNestedStructure(List<ProductImportFileRequest> flatRows) {
    Map<String, Map<String, List<ProductImportFileRequest>>> groupedByProductAndVariant =
            flatRows.stream()
                    .filter(row -> row.getNormalizedKey() != null && row.getSku() != null)
                    .collect(Collectors.groupingBy(
                            ProductImportFileRequest::getNormalizedKey,
                            Collectors.groupingBy(ProductImportFileRequest::getSku)
                    ));

    List<ProductImportFileRequest> structuredProducts = new ArrayList<>();

    for (Map.Entry<String, Map<String, List<ProductImportFileRequest>>> productEntry : groupedByProductAndVariant.entrySet()) {
      String normalizedKey = productEntry.getKey();
      Map<String, List<ProductImportFileRequest>> variantMap = productEntry.getValue();

      ProductImportFileRequest firstRow = variantMap.values().stream()
              .flatMap(Collection::stream)
              .findFirst()
              .orElse(null);

      if (firstRow == null) {
        continue;
      }

      List<ProductVariantRequest> variants = new ArrayList<>();

      for (Map.Entry<String, List<ProductImportFileRequest>> variantEntry : variantMap.entrySet()) {
        String sku = variantEntry.getKey();
        List<ProductImportFileRequest> imageRows = variantEntry.getValue();

        ProductImportFileRequest variantFirstRow = imageRows.get(0);

        List<ProductImageRequest> images = imageRows.stream()
                .filter(row -> row.getSecureUrl() != null && !row.getSecureUrl().isEmpty())
                .map(row -> ProductImageRequest.builder()
                        .secureUrl(row.getSecureUrl())
                        .altText(row.getAltText())
                        .isMain(row.getIsMain() != null ? row.getIsMain() : false)
                        .sortOrder(row.getSortOrder() != null ? row.getSortOrder() : 0)
                        .build())
                .collect(Collectors.toList());

        ProductVariantRequest variant = ProductVariantRequest.builder()
                .sku(sku)
                .volumeMl(variantFirstRow.getVolumeMl())
                .variantName(variantFirstRow.getVariantName())
                .price(variantFirstRow.getVariantPrice())
                .stockQuantity(variantFirstRow.getStockQuantity())
                .isActive(variantFirstRow.getIsActive())
                .images(images)
                .build();

        variants.add(variant);
      }

      ProductImportFileRequest product = ProductImportFileRequest.builder()
              .productName(firstRow.getProductName())
              .brand(firstRow.getBrand())
              .concentration(firstRow.getConcentration())
              .description(firstRow.getDescription())
              .gender(firstRow.getGender())
              .normalizedKey(normalizedKey)
              .price(firstRow.getPrice())
              .releaseYear(firstRow.getReleaseYear())
              .variants(variants)
              .build();

      structuredProducts.add(product);
    }

    return structuredProducts;
  }

  @Override
  public BigDecimal parseBigDecimal(String value) {
    try {
      if (value == null || value.trim().isEmpty())
        return null;
      return new BigDecimal(value.trim());
    } catch (Exception e) {
      return null;
    }
  }

  @Override
  public Integer parseInteger(String value) {
    try {
      if (value == null || value.trim().isEmpty())
        return null;
      return Integer.parseInt(value.trim());
    } catch (Exception e) {
      return null;
    }
  }

  private boolean isEmptyCSV(CSVRecord record) {
    for (String value : record) {
      if (value != null && !value.trim().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private boolean isEmptyExcel(Row row) {
    for (Cell cell : row) {
      String value = getCellValueAsString(cell);
      if (value != null && !value.trim().isEmpty())
        return false;
    }
    return true;
  }

  private String getValue(CSVRecord record, String column) {
    try {
      return record.get(column);
    } catch (Exception e) {
      return null;
    }
  }

  private String getCellString(Row row, Integer index) {
    if (index == null)
      return null;
    Cell cell = row.getCell(index);
    return getCellValueAsString(cell);
  }

  private String getCellValueAsString(Cell cell) {
    if (cell == null)
      return null;

    return switch (cell.getCellType()) {
      case STRING -> cell.getStringCellValue();
      case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue())
              .stripTrailingZeros()
              .toPlainString();
      case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
      case FORMULA -> cell.getCellFormula();
      default -> "";
    };
  }

  private Map<String, Integer> buildHeaderMap(Row headerRow) {
    Map<String, Integer> headerMap = new HashMap<>();
    if (headerRow == null)
      return headerMap;

    for (Cell cell : headerRow) {
      String value = getCellValueAsString(cell);
      if (value != null && !value.trim().isEmpty()) {
        headerMap.put(value.trim().toLowerCase(), cell.getColumnIndex());
      }
    }
    return headerMap;
  }

  private Integer getHeaderIndex(Map<String, Integer> headerMap, String columnName) {
    if (columnName == null) {
      return null;
    }

    return headerMap.get(columnName.toLowerCase());
  }
}
