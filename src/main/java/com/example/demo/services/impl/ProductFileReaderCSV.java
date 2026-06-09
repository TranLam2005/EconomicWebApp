package com.example.demo.services.impl;

import com.example.demo.dtos.request.ProductImportFileRequest;
import com.example.demo.services.ProductFileReader;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ProductFileReaderCSV implements ProductFileReader {
  @Override
  public Boolean supports(String fileName) {
    return fileName != null && fileName.toLowerCase().endsWith(".csv");
  }

  @Override
  public List<ProductImportFileRequest> readFile(MultipartFile file) {
    List<ProductImportFileRequest> rows = new ArrayList<>();
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
        if (isEmpty(record)) {
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
                .volume(parseInteger(getValue(record, "volume")))
                .stockQuantity(parseInteger(getValue(record, "stockQuantity")))
                .build();
        rows.add(dto);
      }
    }   catch (Exception e) {
      throw new RuntimeException("Không thể đọc được file CSV", e);
    }
    return rows;
  }

  @Override
  public BigDecimal parseBigDecimal(String value) {
    try {
      if (value == null || value.trim().isEmpty()) return null;
      return new BigDecimal(value.trim());
    }   catch (Exception e) {
      return null;
    }
  }

  @Override
  public Integer parseInteger(String value) {
    try {
      if (value == null || value.trim().isEmpty()) return null;
      return Integer.parseInt(value.trim());
    }   catch (Exception e) {
      return null;
    }
  }

  private boolean isEmpty (CSVRecord record) {
    for (String value : record) {
      if (value != null || !value.trim().isEmpty()) {
        return false;
      }
    }
    return true;
  }

  private String getValue(CSVRecord record, String column) {
    try {
      return record.get(column);
    }   catch (Exception e) {
      return null;
    }
  }
}
