package com.inventory.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inventory.config.AiConfig;
import com.inventory.entity.Product;
import com.inventory.entity.ProductSku;
import com.inventory.entity.Purchase;
import com.inventory.entity.Sale;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class AiChatService {

    private final AiConfig aiConfig;
    private final ProductService productService;
    private final PurchaseService purchaseService;
    private final SaleService saleService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public AiChatService(AiConfig aiConfig, ProductService productService,
                         PurchaseService purchaseService, SaleService saleService) {
        this.aiConfig = aiConfig;
        this.productService = productService;
        this.purchaseService = purchaseService;
        this.saleService = saleService;
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    public Map<String, Object> chat(String userMessage, Integer userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String systemPrompt = """
                你是一个进销存管理系统的AI助手。你可以帮助用户：
                1. 添加商品 - 识别用户想添加商品，提取商品信息
                2. 添加进货单 - 识别用户想进货，提取进货信息
                3. 添加销售单 - 识别用户想销售，提取销售信息
                4. 查询信息 - 回答关于库存、销售等问题
                
                当用户想添加商品时，请返回JSON格式：
                {"action": "add_product", "data": {"name": "商品名", "specification": "规格", "unit": "单位", "costPrice": 成本价, "sellingPrice": 售价, "stockQuantity": 库存, "minStock": 最低库存}}
                
                当用户想添加进货单时，请返回JSON格式：
                {"action": "add_purchase", "data": {"productName": "商品名", "quantity": 数量, "unitPrice": 单价, "supplier": "供应商"}}
                
                当用户想添加销售单时，请返回JSON格式：
                {"action": "add_sale", "data": {"productName": "商品名", "quantity": 数量, "unitPrice": 单价, "customer": "客户名"}}
                
                如果只是询问，正常回答即可。请简洁回答，中文。
                """;

            String aiResponse = callAiApi(systemPrompt, userMessage);
            
            Map<String, Object> actionResult = parseAndExecuteAction(aiResponse, userId);
            
            if (actionResult != null) {
                result.put("type", "action");
                result.put("action", actionResult.get("action"));
                result.put("success", actionResult.get("success"));
                result.put("message", actionResult.get("message"));
                result.put("reply", actionResult.get("reply"));
            } else {
                result.put("type", "chat");
                result.put("reply", aiResponse);
            }
            
        } catch (Exception e) {
            result.put("type", "error");
            result.put("reply", "抱歉，处理您的请求时出错了: " + e.getMessage());
        }
        
        return result;
    }

    private String callAiApi(String systemPrompt, String userMessage) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> systemMsg = new HashMap<>();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);
        
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(aiConfig.getKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(aiConfig.getUrl(), entity, String.class);
        
        JsonNode root = objectMapper.readTree(response.getBody());
        return root.path("choices").get(0).path("message").path("content").asText();
    }

    private Map<String, Object> parseAndExecuteAction(String aiResponse, Integer userId) {
        try {
            String jsonStr = aiResponse;
            if (aiResponse.contains("{")) {
                int start = aiResponse.indexOf("{");
                int end = aiResponse.lastIndexOf("}") + 1;
                jsonStr = aiResponse.substring(start, end);
            }

            JsonNode node = objectMapper.readTree(jsonStr);
            
            if (!node.has("action")) return null;
            
            String action = node.path("action").asText();
            JsonNode data = node.path("data");
            
            Map<String, Object> result = new HashMap<>();
            result.put("action", action);
            
            switch (action) {
                case "add_product":
                    return handleAddProduct(data, result, userId);
                case "add_purchase":
                    return handleAddPurchase(data, result, userId);
                case "add_sale":
                    return handleAddSale(data, result, userId);
                default:
                    return null;
            }
            
        } catch (Exception e) {
            return null;
        }
    }

    private Map<String, Object> handleAddProduct(JsonNode data, Map<String, Object> result, Integer userId) {
        try {
            Product product = new Product();
            product.setName(data.path("name").asText(""));
            product.setSpecification(data.path("specification").asText(""));
            product.setUnit(data.path("unit").asText("件"));
            product.setCostPrice(new BigDecimal(data.path("costPrice").asDouble(0)));
            product.setSellingPrice(new BigDecimal(data.path("sellingPrice").asDouble(0)));
            product.setStockQuantity(data.path("stockQuantity").asInt(0));
            product.setMinStock(data.path("minStock").asInt(0));
            product.setUserId(userId);

            productService.save(product);
            
            result.put("success", true);
            result.put("message", "商品添加成功: " + product.getName());
            result.put("reply", "✅ 已成功添加商品「" + product.getName() + "」！");
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败: " + e.getMessage());
            result.put("reply", "❌ 添加商品失败: " + e.getMessage());
            return result;
        }
    }

    private Map<String, Object> handleAddPurchase(JsonNode data, Map<String, Object> result, Integer userId) {
        try {
            String productName = data.path("productName").asText("");
            Product product = findProductByName(productName, userId);
            
            if (product == null) {
                result.put("success", false);
                result.put("message", "未找到商品: " + productName);
                result.put("reply", "❌ 未找到商品「" + productName + "」，请先添加该商品。");
                return result;
            }
            
            Purchase purchase = new Purchase();
            purchase.setProduct(product);
            purchase.setProductSku(product.getSkus().isEmpty() ? null : product.getSkus().get(0));
            purchase.setQuantity(data.path("quantity").asInt(0));
            purchase.setUnitPrice(new BigDecimal(data.path("unitPrice").asDouble(0)));
            purchase.setTotalAmount(purchase.getUnitPrice().multiply(new BigDecimal(purchase.getQuantity())));
            purchase.setSupplier(data.path("supplier").asText(""));
            purchase.setPurchaseDate(LocalDate.now());
            purchase.setRemark("AI助手添加");
            purchase.setUserId(userId);
            
            purchaseService.save(purchase);
            
            result.put("success", true);
            result.put("message", "进货单添加成功");
            result.put("reply", "✅ 已成功添加进货单，商品：「" + productName + "」，数量：" + purchase.getQuantity() + "，单价：" + purchase.getUnitPrice() + "元");
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败: " + e.getMessage());
            result.put("reply", "❌ 添加进货单失败: " + e.getMessage());
            return result;
        }
    }

    private Map<String, Object> handleAddSale(JsonNode data, Map<String, Object> result, Integer userId) {
        try {
            String productName = data.path("productName").asText("");
            Product product = findProductByName(productName, userId);
            
            if (product == null) {
                result.put("success", false);
                result.put("message", "未找到商品: " + productName);
                result.put("reply", "❌ 未找到商品「" + productName + "」，请先添加该商品。");
                return result;
            }
            
            Sale sale = new Sale();
            sale.setProduct(product);
            sale.setProductSku(product.getSkus().isEmpty() ? null : product.getSkus().get(0));
            sale.setQuantity(data.path("quantity").asInt(0));
            sale.setUnitPrice(new BigDecimal(data.path("unitPrice").asDouble(0)));
            sale.setTotalAmount(sale.getUnitPrice().multiply(new BigDecimal(sale.getQuantity())));
            sale.setCustomer(data.path("customer").asText(""));
            sale.setSaleDate(LocalDate.now());
            sale.setRemark("AI助手添加");
            sale.setUserId(userId);
            
            saleService.save(sale);
            
            result.put("success", true);
            result.put("message", "销售单添加成功");
            result.put("reply", "✅ 已成功添加销售单，商品：「" + productName + "」，数量：" + sale.getQuantity() + "，单价：" + sale.getUnitPrice() + "元");
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "添加失败: " + e.getMessage());
            result.put("reply", "❌ 添加销售单失败: " + e.getMessage());
            return result;
        }
    }

    private Product findProductByName(String name, Integer userId) {
        List<Product> products = productService.findAll(userId);
        for (Product p : products) {
            if (p.getName().contains(name) || name.contains(p.getName())) {
                return p;
            }
        }
        return null;
    }

    public Map<String, String> analyzeExcelHeaders(List<String> headers) {
        String prompt = """
            You are an inventory management data assistant. 
            I have a list of headers from an Excel file: %s.
            Please map these headers to the following system fields: 
            "name", "specification", "material", "craft", "sellingPrice", "costPrice", "stock", "minStock", "brand", "season".
            
            Rules:
            1. "sellingPrice" maps to price-related headers.
            2. "costPrice" maps to cost-related headers.
            3. "specification" maps to color, size, model, or generic specs.
            4. Return ONLY a valid JSON object. Do not include markdown formatting or other text.
            5. Keys must be the system fields exactly. Values must be the exact string from the input list.
            
            Example output: {"name": "品名", "specification": "颜色", "sellingPrice": "售价"}
            """;
        
        Map<String, String> mapping = new HashMap<>();
        try {
            String response = callAiApi(prompt, headers.toString());
            
            // Clean response to ensure it's just JSON
            String jsonStr = response;
            if (jsonStr.contains("{")) {
                jsonStr = jsonStr.substring(jsonStr.indexOf("{"), jsonStr.lastIndexOf("}") + 1);
            }
            JsonNode node = objectMapper.readTree(jsonStr);
            node.fields().forEachRemaining(entry -> mapping.put(entry.getKey(), entry.getValue().asText()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mapping;
    }
}