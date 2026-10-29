package com.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sale")
public class Sale {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id")
    private ProductSku productSku;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(length = 100)
    private String customer;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_entity_id")
    private Customer customerEntity;
    
    @Column(length = 20)
    private String status = "已完成";
    
    @Column(name = "sale_date", nullable = false)
    private LocalDate saleDate;
    
    @Column(length = 500)
    private String remark;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "user_id", nullable = false)
    private Integer userId;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    
    public ProductSku getProductSku() { return productSku; }
    public void setProductSku(ProductSku productSku) { this.productSku = productSku; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public String getCustomer() { return customer; }
    public void setCustomer(String customer) { this.customer = customer; }
    
    public Customer getCustomerEntity() { return customerEntity; }
    public void setCustomerEntity(Customer customerEntity) { this.customerEntity = customerEntity; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDate getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDate saleDate) { this.saleDate = saleDate; }
    
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}