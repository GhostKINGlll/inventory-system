package com.inventory.controller;

import com.inventory.dao.SaleRepository;
import com.inventory.entity.Customer;
import com.inventory.entity.Sale;
import com.inventory.entity.User;
import com.inventory.service.CustomerService;
import com.inventory.service.SaleService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.math.BigDecimal;

@Controller
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService customerService;
    private final SaleService saleService;
    private final SaleRepository saleRepository;

    public CustomerController(CustomerService customerService, SaleService saleService, SaleRepository saleRepository) {
        this.customerService = customerService;
        this.saleService = saleService;
        this.saleRepository = saleRepository;
    }

    private Integer getUserId(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null ? user.getId() : 0;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        model.addAttribute("customers", customerService.findAll(getUserId(session)));
        return "customers";
    }

    @PostMapping("/save")
    public String save(Customer customer, HttpSession session) {
        customer.setUserId(getUserId(session));
        customerService.save(customer);
        return "redirect:/customers";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        customerService.delete(id, getUserId(session));
        return "redirect:/customers";
    }

    @GetMapping("/statement/{id}")
    public String statement(@PathVariable Integer id, HttpSession session, Model model) {
        Integer userId = getUserId(session);
        Customer customer = customerService.findById(id, userId).orElse(null);
        if (customer == null) return "redirect:/customers";
        
        List<Sale> sales = saleRepository.findByCustomerEntityIdAndUserId(id, userId);
        BigDecimal totalSales = sales.stream().map(Sale::getTotalAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        
        model.addAttribute("customer", customer);
        model.addAttribute("sales", sales);
        model.addAttribute("totalSales", totalSales);
        return "customer-statement";
    }
}
