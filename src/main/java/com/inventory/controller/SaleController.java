package com.inventory.controller;

import com.inventory.entity.Sale;
import com.inventory.entity.Product;
import com.inventory.entity.Customer;
import com.inventory.entity.User;
import com.inventory.service.SaleService;
import com.inventory.service.ProductService;
import com.inventory.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@Controller
@RequestMapping("/sales")
public class SaleController {
    private final SaleService saleService;
    private final ProductService productService;
    private final CustomerService customerService;

    public SaleController(SaleService saleService, ProductService productService, CustomerService customerService) {
        this.saleService = saleService;
        this.productService = productService;
        this.customerService = customerService;
    }

    private Integer getUserId(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null ? user.getId() : 0;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        Integer userId = getUserId(session);
        model.addAttribute("sales", saleService.findAll(userId));
        model.addAttribute("products", productService.findAll(userId));
        model.addAttribute("customers", customerService.findAll(userId));
        return "sales";
    }

    @GetMapping("/search")
    public String search(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate,
                         HttpSession session, Model model) {
        Integer userId = getUserId(session);
        model.addAttribute("sales", saleService.findByDateRange(startDate, endDate, userId));
        model.addAttribute("products", productService.findAll(userId));
        return "sales";
    }

    @PostMapping("/save")
    public String save(@RequestParam Integer productId, @RequestParam Integer quantity,
                       @RequestParam(required = false) Integer customerId,
                       @RequestParam java.math.BigDecimal unitPrice, @RequestParam String customer,
                       @RequestParam LocalDate saleDate, @RequestParam(required = false) String remark,
                       @RequestParam String status, HttpSession session, Model model) {
        Integer userId = getUserId(session);
        try {
            Product product = productService.findById(productId, userId).orElse(null);
            if (product != null) {
                Sale sale = new Sale();
                sale.setProduct(product);
                sale.setProductSku(product.getSkus().isEmpty() ? null : product.getSkus().get(0));
                sale.setQuantity(quantity);
                sale.setUnitPrice(unitPrice);
                sale.setTotalAmount(unitPrice.multiply(new java.math.BigDecimal(quantity)));
                
                if (customerId != null) {
                    Customer c = customerService.findById(customerId, userId).orElse(null);
                    sale.setCustomerEntity(c);
                    sale.setCustomer(c != null ? c.getName() : customer);
                } else {
                    sale.setCustomer(customer);
                }
                
                sale.setSaleDate(saleDate);
                sale.setRemark(remark);
                sale.setUserId(userId);
                sale.setStatus(status);
                saleService.save(sale);
            }
            return "redirect:/sales";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("sales", saleService.findAll(userId));
            model.addAttribute("products", productService.findAll(userId));
            model.addAttribute("customers", customerService.findAll(userId));
            return "sales";
        }
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        saleService.delete(id, getUserId(session));
        return "redirect:/sales";
    }
}