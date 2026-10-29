package com.inventory.service;

import com.inventory.dao.PurchaseRepository;
import com.inventory.dao.ProductRepository;
import com.inventory.dao.ProductSkuRepository;
import com.inventory.entity.Purchase;
import com.inventory.entity.Product;
import com.inventory.entity.ProductSku;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;

    public PurchaseService(PurchaseRepository purchaseRepository, ProductRepository productRepository, ProductSkuRepository productSkuRepository) {
        this.purchaseRepository = purchaseRepository;
        this.productRepository = productRepository;
        this.productSkuRepository = productSkuRepository;
    }

    public List<Purchase> findAll(Integer userId) {
        return purchaseRepository.findByUserId(userId);
    }

    public List<Purchase> findByDateRange(LocalDate startDate, LocalDate endDate, Integer userId) {
        return purchaseRepository.findByPurchaseDateBetweenAndUserId(startDate, endDate, userId);
    }

    @Transactional
    public Purchase save(Purchase purchase) {
        ProductSku sku = purchase.getProductSku();
        if (sku == null) {
            sku = purchase.getProduct().getSkus().stream().findFirst().orElse(null);
            purchase.setProductSku(sku);
        }
        if (sku != null) {
            int currentStock = sku.getStockQuantity() != null ? sku.getStockQuantity() : 0;
            sku.setStockQuantity(currentStock + purchase.getQuantity());
            productSkuRepository.save(sku);
        }
        return purchaseRepository.save(purchase);
    }

    @Transactional
    public void delete(Integer id, Integer userId) {
        Purchase purchase = purchaseRepository.findByIdAndUserId(id, userId).orElse(null);
        if (purchase != null) {
            ProductSku sku = purchase.getProductSku();
            if (sku != null) {
                int currentStock = sku.getStockQuantity() != null ? sku.getStockQuantity() : 0;
                sku.setStockQuantity(currentStock - purchase.getQuantity());
                productSkuRepository.save(sku);
            }
            purchaseRepository.deleteById(id);
        }
    }

    public List<Purchase> parseExcel(MultipartFile file, Integer userId) {
        List<Purchase> purchases = new ArrayList<>();

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new RuntimeException("Excel文件为空");
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValue(cell).trim().toLowerCase().replace(" ", ""));
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row)) continue;

                Map<String, String> rowData = getRowData(row, headers);

                String productName = findValue(rowData, "商品名称", "名称", "name", "product", "商品", "产品");
                String quantityStr = findValue(rowData, "数量", "quantity", "qty", "进货数量");
                String priceStr = findValue(rowData, "单价", "price", "unitprice", "进货单价");
                String supplier = findValue(rowData, "供应商", "supplier", "supply");
                String dateStr = findValue(rowData, "日期", "date", "purchaseDate", "进货日期");

                if (productName == null || productName.isEmpty()) continue;

                Product product = findProductByName(productName, userId);
                if (product == null) continue;

                Purchase purchase = new Purchase();
                purchase.setProduct(product);
                purchase.setQuantity(parseInt(quantityStr));
                purchase.setUnitPrice(parseBigDecimal(priceStr));
                purchase.setTotalAmount(purchase.getUnitPrice().multiply(new BigDecimal(purchase.getQuantity())));
                purchase.setSupplier(supplier != null ? supplier : "");
                purchase.setPurchaseDate(parseDate(dateStr));
                purchase.setRemark("");

                purchases.add(purchase);
            }
        } catch (Exception e) {
            throw new RuntimeException("Excel解析失败: " + e.getMessage());
        }

        return purchases;
    }

    private Product findProductByName(String name, Integer userId) {
        List<Product> products = productRepository.findByUserId(userId);
        for (Product p : products) {
            if (p.getName().equals(name)) {
                return p;
            }
        }
        return null;
    }
    
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try { return String.valueOf(cell.getNumericCellValue()); }
                catch (Exception e) { return cell.getStringCellValue(); }
            default: return "";
        }
    }
    
    private boolean isEmptyRow(Row row) {
        for (Cell cell : row) {
            if (cell != null && !getCellValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }
    
    private Map<String, String> getRowData(Row row, List<String> headers) {
        Map<String, String> data = new HashMap<>();
        for (int j = 0; j < headers.size() && j < row.getLastCellNum(); j++) {
            Cell cell = row.getCell(j);
            if (cell != null && headers.get(j) != null) {
                data.put(headers.get(j), getCellValue(cell));
            }
        }
        return data;
    }
    
    private String findValue(Map<String, String> data, String... keys) {
        for (String key : keys) {
            String value = data.get(key.replace(" ", ""));
            if (value != null && !value.isEmpty()) {
                return value;
            }
        }
        return null;
    }
    
    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) return BigDecimal.ZERO;
        try {
            String num = value.replaceAll("[^\\d.]", "");
            return new BigDecimal(num.isEmpty() ? "0" : num);
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
    
    private int parseInt(String value) {
        if (value == null || value.isEmpty()) return 0;
        try {
            Matcher m = Pattern.compile("\\d+").matcher(value);
            if (m.find()) return Integer.parseInt(m.group());
        } catch (Exception e) { }
        return 0;
    }
    
    private LocalDate parseDate(String value) {
        if (value == null || value.isEmpty()) return LocalDate.now();
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }
}