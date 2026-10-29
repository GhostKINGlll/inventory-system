package com.inventory.controller;

import com.inventory.entity.Product;
import com.inventory.entity.User;
import com.inventory.service.ProductService;
import com.inventory.service.AiChatService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Controller
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    private final AiChatService aiChatService;

    public ProductController(ProductService productService, AiChatService aiChatService) {
        this.productService = productService;
        this.aiChatService = aiChatService;
    }

    private Integer getUserId(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null ? user.getId() : 0;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        model.addAttribute("products", productService.findAll(getUserId(session)));
        return "products";
    }

    @GetMapping("/search")
    public String search(@RequestParam String keyword, HttpSession session, Model model) {
        model.addAttribute("products", productService.search(keyword, getUserId(session)));
        return "products";
    }

    @PostMapping("/save")
    public String save(Product product, HttpSession session) {
        product.setUserId(getUserId(session));
        productService.save(product);
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        productService.delete(id, getUserId(session));
        return "redirect:/products";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, HttpSession session, Model model) {
        model.addAttribute("product", productService.findById(id, getUserId(session)).orElse(null));
        model.addAttribute("products", productService.findAll(getUserId(session)));
        return "products";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, HttpSession session, RedirectAttributes redirect) {
        if (file.isEmpty()) {
            redirect.addFlashAttribute("error", "请选择文件");
            return "redirect:/products";
        }
        
        try {
            var products = productService.parseExcel(file);
            int count = 0;
            for (Product p : products) {
                p.setUserId(getUserId(session));
                productService.save(p);
                count++;
            }
            redirect.addFlashAttribute("message", "成功导入 " + count + " 条商品");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "导入失败: " + e.getMessage());
        }
        return "redirect:/products";
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    public Product getDetail(@PathVariable Integer id, HttpSession session) {
        return productService.findById(id, getUserId(session)).orElse(null);
    }

    @PostMapping("/batch-save")
    @ResponseBody
    public Map<String, Object> batchSave(@RequestBody List<Product> products, HttpSession session) {
        Integer userId = getUserId(session);
        try {
            productService.saveAll(products, userId);
            return Map.of("success", true, "count", products.size());
        } catch (Exception e) {
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    @PostMapping("/api/import/analyze")
    @ResponseBody
    public Map<String, String> analyzeHeaders(@RequestBody List<String> headers) {
        return aiChatService.analyzeExcelHeaders(headers);
    }

    @PostMapping("/api/import/save")
    @ResponseBody
    public Map<String, Object> saveSmartImport(@RequestBody SmartImportRequest request, HttpSession session) {
        Integer userId = getUserId(session);
        List<Product> products = new ArrayList<>();

        try {
            Map<String, Integer> mapping = request.getMapping();

            Function<String, String> getVal = (field) -> {
                Integer idx = mapping.get(field);
                if (idx != null && idx >= 0 && request.getRows().get(0).size() > idx) {
                    // This lambda is stateless regarding rows, but we need rows here. 
                    // Actually, lambda captures request effectively.
                    // Wait, the rows list in request.getRows() is a list of lists.
                    // Let's fix the logic below.
                    return null;
                }
                return null;
            };

            // Correct logic for rows processing
            for (List<String> row : request.getRows()) {
                if (row.isEmpty()) continue;

                Product p = new Product();
                p.setUserId(userId);

                String name = extract(row, mapping, "name");
                if (name == null || name.isEmpty()) continue;

                p.setName(name);
                p.setSpecification(extract(row, mapping, "specification"));
                p.setMaterial(extract(row, mapping, "material"));
                p.setCraft(extract(row, mapping, "craft"));
                p.setBrand(extract(row, mapping, "brand"));
                p.setSeason(extract(row, mapping, "season"));

                String cost = extract(row, mapping, "costPrice");
                if (cost != null) p.setCostPrice(new BigDecimal(cost.replaceAll("[^\\d.]", "")));

                String price = extract(row, mapping, "sellingPrice");
                if (price != null) p.setSellingPrice(new BigDecimal(price.replaceAll("[^\\d.]", "")));

                String stock = extract(row, mapping, "stock");
                if (stock != null) p.setStockQuantity(Integer.parseInt(stock.replaceAll("[^\\d]", "")));

                String minStock = extract(row, mapping, "minStock");
                if (minStock != null) p.setMinStock(Integer.parseInt(minStock.replaceAll("[^\\d]", "")));

                products.add(p);
            }

            if (!products.isEmpty()) {
                productService.saveAll(products, userId);
            }

            return Map.of("success", true, "count", products.size());

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "error", e.getMessage());
        }
    }

    private String extract(List<String> row, Map<String, Integer> mapping, String field) {
        Integer idx = mapping.get(field);
        if (idx != null && idx >= 0 && idx < row.size()) {
            return row.get(idx).trim();
        }
        return null;
    }

    public static class SmartImportRequest {
        private List<List<String>> rows;
        private Map<String, Integer> mapping;

        public List<List<String>> getRows() { return rows; }
        public void setRows(List<List<String>> rows) { this.rows = rows; }
        public Map<String, Integer> getMapping() { return mapping; }
        public void setMapping(Map<String, Integer> mapping) { this.mapping = mapping; }
    }
}
