package com.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_sku")
public class ProductSku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Product product;

    @Column(length = 50)
    private String color;

    @Column(length = 50)
    private String size;

    @Column(length = 100)
    private String barcode;

    @Column(name = "cost_price", precision = 10, scale = 2)
    private BigDecimal costPrice = BigDecimal.ZERO;

    @Column(name = "selling_price", precision = 10, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(name = "stock_quantity")
    private Integer stockQuantity = 0;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public BigDecimal getCostPrice() { return costPrice; }
    public void setCostPrice(BigDecimal costPrice) { this.costPrice = costPrice; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }
}
