package com.inventory.controller;

import com.inventory.entity.Customer;
import com.inventory.entity.SampleOrder;
import com.inventory.entity.User;
import com.inventory.service.CustomerService;
import com.inventory.service.SampleOrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/samples")
public class SampleOrderController {
    private final SampleOrderService sampleOrderService;
    private final CustomerService customerService;

    public SampleOrderController(SampleOrderService sampleOrderService, CustomerService customerService) {
        this.sampleOrderService = sampleOrderService;
        this.customerService = customerService;
    }

    private Integer getUserId(HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        return user != null ? user.getId() : 0;
    }

    @GetMapping
    public String list(HttpSession session, Model model) {
        Integer userId = getUserId(session);
        model.addAttribute("samples", sampleOrderService.findAll(userId));
        model.addAttribute("customers", customerService.findAll(userId));
        return "samples";
    }

    @PostMapping("/save")
    public String save(@RequestParam(required = false) Integer customerId,
                       @RequestParam String productName,
                       @RequestParam(required = false) String requirements,
                       @RequestParam(required = false) String material,
                       @RequestParam(required = false) String craft,
                       @RequestParam(required = false) BigDecimal sampleFee,
                       @RequestParam(required = false) LocalDate sampleDate,
                       @RequestParam(required = false) String remark,
                       HttpSession session) {
        Integer userId = getUserId(session);
        SampleOrder order = new SampleOrder();
        
        if (customerId != null) {
            order.setCustomer(customerService.findById(customerId, userId).orElse(null));
        }
        order.setProductName(productName);
        order.setRequirements(requirements != null ? requirements : "");
        order.setMaterial(material != null ? material : "");
        order.setCraft(craft != null ? craft : "");
        order.setSampleFee(sampleFee != null ? sampleFee : BigDecimal.ZERO);
        order.setSampleDate(sampleDate != null ? sampleDate : LocalDate.now());
        order.setRemark(remark != null ? remark : "");
        order.setUserId(userId);
        
        sampleOrderService.save(order);
        return "redirect:/samples";
    }

    @PostMapping("/status/{id}")
    public String updateStatus(@PathVariable Integer id, @RequestParam String status, HttpSession session) {
        sampleOrderService.updateStatus(id, status, getUserId(session));
        return "redirect:/samples";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, HttpSession session) {
        sampleOrderService.delete(id, getUserId(session));
        return "redirect:/samples";
    }
}
