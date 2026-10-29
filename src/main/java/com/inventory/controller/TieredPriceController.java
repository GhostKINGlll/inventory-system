package com.inventory.controller;

import com.inventory.entity.TieredPrice;
import com.inventory.entity.User;
import com.inventory.service.TieredPriceService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tiered-prices")
public class TieredPriceController {
    private final TieredPriceService tieredPriceService;

    public TieredPriceController(TieredPriceService tieredPriceService) {
        this.tieredPriceService = tieredPriceService;
    }

    @GetMapping
    public ResponseEntity<List<TieredPrice>> getPrices(@RequestParam Integer productId) {
        return ResponseEntity.ok(tieredPriceService.findPricesByProduct(productId));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> savePrices(@RequestParam Integer productId, 
                                                          @RequestBody List<TieredPriceInput> inputs,
                                                          HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            result.put("success", false);
            return ResponseEntity.badRequest().body(result);
        }

        List<TieredPrice> prices = inputs.stream().map(input -> {
            TieredPrice tp = new TieredPrice();
            tp.setMinQuantity(input.getMinQuantity());
            tp.setPrice(input.getPrice());
            com.inventory.entity.Product p = new com.inventory.entity.Product();
            p.setId(productId);
            tp.setProduct(p);
            return tp;
        }).toList();

        tieredPriceService.saveAll(productId, prices);
        result.put("success", true);
        return ResponseEntity.ok(result);
    }

    public static class TieredPriceInput {
        private Integer minQuantity;
        private java.math.BigDecimal price;

        public Integer getMinQuantity() { return minQuantity; }
        public void setMinQuantity(Integer minQuantity) { this.minQuantity = minQuantity; }
        public java.math.BigDecimal getPrice() { return price; }
        public void setPrice(java.math.BigDecimal price) { this.price = price; }
    }
}
