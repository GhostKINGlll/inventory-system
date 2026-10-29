package com.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @Column(name = "id")
    private Integer id;
    
    @Column(nullable = false, length = 100)
    private String name;
    
    @Column(length = 100)
    private String specification;
    
    @Column(length = 100)
    private String brand;
    
    @Column(length = 50)
    private String category;
    
    @Column(length = 100)
    private String material;
    
    @Column(length = 100)
    private String craft;
    
    @Column(name = "min_order_qty")
    private Integer minOrderQty = 0;
    
    @Column(length = 20)
    private String season;
    
    @Column(length = 10)
    private String year;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<ProductSku> skus = new java.util.ArrayList<>();
    
    @Column(length = 20)
    private String unit = "件";

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;
    
    @Column(name = "selling_price", precision = 10, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;
    
    @Column(name = "min_stock")
    private Integer minStock = 0;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
    
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit != null ? unit : "件"; }
    
    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }
    
    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }
    
    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
    
    public Integer getMinStock() { return minStock; }
    public void setMinStock(Integer minStock) { this.minStock = minStock; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getCraft() { return craft; }
    public void setCraft(String craft) { this.craft = craft; }

    public Integer getMinOrderQty() { return minOrderQty; }
    public void setMinOrderQty(Integer minOrderQty) { this.minOrderQty = minOrderQty; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public java.util.List<ProductSku> getSkus() { return skus; }
    public void setSkus(java.util.List<ProductSku> skus) { this.skus = skus; }

    public Integer getTotalStock() {
        if (skus == null || skus.isEmpty()) return stockQuantity != null ? stockQuantity : 0;
        return skus.stream().mapToInt(s -> s.getStockQuantity() != null ? s.getStockQuantity() : 0).sum();
    }
}