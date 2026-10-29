package com.inventory.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "sample_order")
public class SampleOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(length = 200)
    private String productName;

    @Column(length = 200)
    private String requirements;

    @Column(length = 100)
    private String material;

    @Column(length = 100)
    private String craft;

    @Column(name = "sample_fee", precision = 10, scale = 2)
    private BigDecimal sampleFee = BigDecimal.ZERO;

    @Column(name = "status", length = 20)
    private String status = "待打样";

    @Column(name = "sample_date")
    private LocalDate sampleDate;

    @Column(length = 500)
    private String remark;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (sampleDate == null) sampleDate = LocalDate.now();
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getCraft() { return craft; }
    public void setCraft(String craft) { this.craft = craft; }

    public BigDecimal getSampleFee() { return sampleFee; }
    public void setSampleFee(BigDecimal sampleFee) { this.sampleFee = sampleFee; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getSampleDate() { return sampleDate; }
    public void setSampleDate(LocalDate sampleDate) { this.sampleDate = sampleDate; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
