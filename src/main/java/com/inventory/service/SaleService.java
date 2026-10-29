package com.inventory.service;

import com.inventory.dao.SaleRepository;
import com.inventory.dao.ProductRepository;
import com.inventory.dao.ProductSkuRepository;
import com.inventory.dao.CustomerRepository;
import com.inventory.entity.Sale;
import com.inventory.entity.Product;
import com.inventory.entity.ProductSku;
import com.inventory.entity.Customer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class SaleService {
    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final ProductSkuRepository productSkuRepository;
    private final CustomerRepository customerRepository;

    public SaleService(SaleRepository saleRepository, ProductRepository productRepository, 
                       ProductSkuRepository productSkuRepository, CustomerRepository customerRepository) {
        this.saleRepository = saleRepository;
        this.productRepository = productRepository;
        this.productSkuRepository = productSkuRepository;
        this.customerRepository = customerRepository;
    }

    public List<Sale> findAll(Integer userId) {
        return saleRepository.findByUserId(userId);
    }

    public List<Sale> findByDateRange(LocalDate startDate, LocalDate endDate, Integer userId) {
        return saleRepository.findBySaleDateBetweenAndUserId(startDate, endDate, userId);
    }

    @Transactional
    public Sale save(Sale sale) {
        ProductSku sku = sale.getProductSku();
        if (sku == null) {
            sku = sale.getProduct().getSkus().stream().findFirst().orElse(null);
            sale.setProductSku(sku);
        }
        if (sku != null) {
            int currentStock = sku.getStockQuantity() != null ? sku.getStockQuantity() : 0;
            if (sale.getQuantity() > currentStock) {
                throw new RuntimeException("库存不足！当前库存: " + currentStock + " (" + sku.getColor() + "/" + sku.getSize() + ")");
            }
            sku.setStockQuantity(currentStock - sale.getQuantity());
            productSkuRepository.save(sku);
            
            Customer customer = sale.getCustomerEntity();
            if (customer != null && "月结".equals(customer.getSettlementMode())) {
                customer.setCurrentBalance(customer.getCurrentBalance().add(sale.getTotalAmount()));
                customerRepository.save(customer);
            }
        }
        return saleRepository.save(sale);
    }

    @Transactional
    public void delete(Integer id, Integer userId) {
        Sale sale = saleRepository.findByIdAndUserId(id, userId).orElse(null);
        if (sale != null) {
            ProductSku sku = sale.getProductSku();
            if (sku != null) {
                int currentStock = sku.getStockQuantity() != null ? sku.getStockQuantity() : 0;
                sku.setStockQuantity(currentStock + sale.getQuantity());
                productSkuRepository.save(sku);
            }
            Customer customer = sale.getCustomerEntity();
            if (customer != null && "月结".equals(customer.getSettlementMode())) {
                customer.setCurrentBalance(customer.getCurrentBalance().subtract(sale.getTotalAmount()));
                customerRepository.save(customer);
            }
            saleRepository.deleteById(id);
        }
    }
}