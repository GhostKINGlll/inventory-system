package com.inventory.controller;

import com.inventory.entity.Purchase;
import com.inventory.entity.Product;
import com.inventory.entity.User;
import com.inventory.service.PurchaseService;
import com.inventory.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/purchases")
public class PurchaseController {
    private final PurchaseService purchaseService;
    private final ProductService productService;

    public PurchaseController(PurchaseService purchaseService, ProductService productService) {
        this.purchaseService = purchaseService;
        this.productService = productService;
    }

    private Integer getUserId(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null ? user.getId() : 0;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        Integer userId = getUserId(session);
        model.addAttribute("purchases", purchaseService.findAll(userId));
        model.addAttribute("products", productService.findAll(userId));
        return "purchases";
    }

    @GetMapping("/search")
    public String search(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
                         HttpSession session, Model model) {
        Integer userId = getUserId(session);
        model.addAttribute("purchases", purchaseService.findByDateRange(startDate, endDate, userId));
        model.addAttribute("products", productService.findAll(userId));
        return "purchases";
    }

    @PostMapping("/save")
    public String save(@RequestParam Integer productId, @RequestParam Integer quantity,
                      @RequestParam BigDecimal unitPrice, @RequestParam String supplier,
                      @RequestParam LocalDate purchaseDate, @RequestParam(required = false) String remark,
                      HttpSession session) {
        Integer userId = getUserId(session);
        Product product = productService.findById(productId, userId).orElse(null);
        if (product != null) {
            Purchase purchase = new Purchase();
            purchase.setProduct(product);
            purchase.setProductSku(product.getSkus().isEmpty() ? null : product.getSkus().get(0));
            purchase.setQuantity(quantity);
            purchase.setUnitPrice(unitPrice);
            purchase.setTotalAmount(unitPrice.multiply(BigDecimal.valueOf(quantity)));
            purchase.setSupplier(supplier);
            purchase.setPurchaseDate(purchaseDate);
            purchase.setRemark(remark != null ? remark : "");
            purchase.setUserId(userId);
            purchaseService.save(purchase);
        }
        return "redirect:/purchases";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        purchaseService.delete(id, getUserId(session));
        return "redirect:/purchases";
    }

    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file, HttpSession session, RedirectAttributes redirect) {
        if (file.isEmpty()) {
            redirect.addFlashAttribute("error", "请选择文件");
            return "redirect:/purchases";
        }
        
        try {
            var purchases = purchaseService.parseExcel(file, getUserId(session));
            for (Purchase p : purchases) {
                p.setUserId(getUserId(session));
                purchaseService.save(p);
            }
            redirect.addFlashAttribute("message", "成功导入 " + purchases.size() + " 条进货记录");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "导入失败: " + e.getMessage());
        }
        return "redirect:/purchases";
    }
}