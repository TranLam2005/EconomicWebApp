package com.example.demo.services.impl;
import com.example.demo.dtos.request.ProductImportFileRequest;
import com.example.demo.services.ProductFileReader;
import org.apache.poi.ss.usermodel.*;
import org.springframework.web.multipart.MultipartFile;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductFileReaderExcel implements ProductFileReader {
    @Override
    public Boolean supports(String fileName) {
        return null;
    }

    @Override
    public List<ProductImportFileRequest> readFile(MultipartFile file) {
        List<ProductImportFileRequest> rows = new ArrayList<>();

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return rows;
            }

            Row headerRow = sheet.getRow(0);
            Map<String, Integer> headerMap = buildHeaderMap(headerRow);

            for (int i = 1; i < sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmpty(row)) {
                    continue;
                }

                ProductImportFileRequest dto = ProductImportFileRequest.builder()
                        .productName(getCellString(row, headerMap.get("name")))
                        .brand(getCellString(row, headerMap.get("brand")))
                        .price(parseBigDecimal(getCellString(row, headerMap.get("price"))))
                        .concentration(getCellString(row, headerMap.get("concentration")))
                        .description(getCellString(row, headerMap.get("description")))
                        .gender(getCellString(row, headerMap.get("gender")))
                        .normalizedKey(getCellString(row, headerMap.get("normalizedKey")))
                        .releaseYear(parseInteger(getCellString(row, headerMap.get("releaseYear"))))
                        .secureUrl(getCellString(row, headerMap.get("secureUrl")))
                        .stockQuantity(parseInteger(getCellString(row, headerMap.get("stockQuantity"))))
                        .volume(parseInteger(getCellString(row, headerMap.get("volume"))))
                        .stockQuantity(parseInteger(getCellString(row, headerMap.get("stockQuantity"))))
                        .build();
                rows.add(dto);
            }
            return rows;
        }   catch (Exception e) {
            throw new RuntimeException("Have problem during read data", e);
        }
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

    private boolean isEmpty(Row row) {
        for (Cell cell : row) {
            String value = getCellValueAsString(cell);
            if (value != null || !value.trim().isEmpty())
                return false;
        }
        return true;
    }

    private String getCellString(Row row, Integer index) {
        if (index == null)
            return null;
        Cell cell = row.getCell(index);
        return getCellValueAsString(cell);
    }
}
