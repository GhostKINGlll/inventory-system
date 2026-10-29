package com.inventory.service;

import com.inventory.dao.ProductRepository;
import com.inventory.dao.ProductSkuRepository;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;

    public ProductService(ProductRepository productRepository, ProductSkuRepository productSkuRepository) {
        this.productRepository = productRepository;
        this.productSkuRepository = productSkuRepository;
    }

    public List<Product> findAll(Integer userId) {
        return productRepository.findByUserId(userId);
    }

    public List<Product> findLowStockProducts(Integer userId) {
        return productRepository.findLowStockProducts(userId);
    }

    public Optional<Product> findById(Integer id, Integer userId) {
        return productRepository.findByIdAndUserId(id, userId);
    }

    public List<Product> search(String keyword, Integer userId) {
        return productRepository.search(keyword, userId);
    }

    @Transactional
    public Product save(Product product) {
        if (product.getId() == null) {
            String name = product.getName();
            String spec = product.getSpecification() != null ? product.getSpecification().trim() : "";
            
            // Check for duplicate by Name + Spec
            List<Product> existingProducts = productRepository.findByNameAndUserId(name, product.getUserId());
            for (Product existing : existingProducts) {
                String existingSpec = existing.getSpecification() != null ? existing.getSpecification().trim() : "";
                if (existingSpec.equals(spec)) {
                    // Merge SKUs
                    if (product.getSkus() != null) {
                        for (ProductSku newSku : product.getSkus()) {
                            newSku.setProduct(existing);
                            productSkuRepository.save(newSku);
                        }
                    }
                    return existing;
                }
            }
            
            Integer nextId = findNextAvailableId(product.getUserId());
            product.setId(nextId);
            
            // If no SKUs provided, create default SKU from product fields
            if (product.getSkus() == null || product.getSkus().isEmpty()) {
                ProductSku defaultSku = new ProductSku();
                defaultSku.setProduct(product);
                defaultSku.setColor("均码");
                defaultSku.setSize("均码");
                defaultSku.setCostPrice(product.getCostPrice());
                defaultSku.setSellingPrice(product.getSellingPrice());
                defaultSku.setStockQuantity(product.getStockQuantity() != null ? product.getStockQuantity() : 0);
                product.getSkus().add(defaultSku);
            } else {
                for (ProductSku sku : product.getSkus()) {
                    sku.setProduct(product);
                }
            }
        } else {
            // Updating existing product
            if (product.getSkus() != null) {
                for (ProductSku sku : product.getSkus()) {
                    if (sku.getId() == null) {
                        sku.setProduct(product);
                    }
                }
            }
        }
        return productRepository.save(product);
    }

    @Transactional
    public List<Product> saveAll(List<Product> products, Integer userId) {
        List<Product> saved = new ArrayList<>();
        for (Product p : products) {
            p.setUserId(userId);
            saved.add(save(p));
        }
        return saved;
    }

    private Integer findNextAvailableId(Integer userId) {
        Integer minId = productRepository.findMinIdByUserId(userId);
        Integer maxId = productRepository.findMaxIdByUserId(userId);
        
        if (minId == 0) return 1;
        
        for (int i = 1; i <= maxId + 1; i++) {
            if (productRepository.findByIdAndUserId(i, userId).isEmpty()) {
                return i;
            }
        }
        return maxId + 1;
    }

    @Transactional
    public void delete(Integer id, Integer userId) {
        productRepository.findByIdAndUserId(id, userId).ifPresentOrElse(
            product -> productRepository.deleteById(id),
            () -> { throw new RuntimeException("无权操作该商品"); }
        );
    }

    @Transactional
    public void updateStock(Integer productId, int quantity, Integer userId) {
        productRepository.findByIdAndUserId(productId, userId).ifPresent(product -> {
            int current = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
            product.setStockQuantity(current + quantity);
            productRepository.save(product);
        });
    }

    public List<Product> parseExcel(MultipartFile file) {
        List<Product> products = new ArrayList<>();
        
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
                
                Product product = new Product();
                Map<String, String> rowData = getRowData(row, headers);
                
                String name = findValue(rowData, "商品名称", "名称", "name", "product", "产品", "商品");
                if (name != null && !name.isEmpty()) {
                    product.setName(name);
                } else {
                    continue;
                }
                
                String spec = findValue(rowData, "规格型号", "规格", "型号", "spec", "specification");
                if (spec != null) product.setSpecification(spec);
                
                String unit = findValue(rowData, "单位", "unit");
                product.setUnit(unit != null ? unit : "件");
                
                String costStr = findValue(rowData, "成本价", "成本", "cost", "costprice");
                product.setCostPrice(parseBigDecimal(costStr));
                
                String priceStr = findValue(rowData, "售价", "price", "sellingprice", "价格");
                product.setSellingPrice(parseBigDecimal(priceStr));
                
                String stockStr = findValue(rowData, "库存", "库存数量", "stock", "stockquantity", "数量");
                product.setStockQuantity(parseInt(stockStr));
                
                String minStockStr = findValue(rowData, "最低库存", "minstock", "最小库存");
                product.setMinStock(parseInt(minStockStr));
                
                products.add(product);
            }
        } catch (Exception e) {
            throw new RuntimeException("Excel解析失败: " + e.getMessage());
        }
        
        return products;
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
}