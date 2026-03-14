package com.noasuhive.supply_chain_management.models;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "manufacturer_products")
public class ManufacturerProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID manufacturerId;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    private Long stock;

    private BigDecimal basePrice; // Wholesale price for retailers
    
    @Column(name = "direct_sale_price")
    private BigDecimal directSalePrice; // Price for direct customer sales

    private boolean isActive = true;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(UUID manufacturerId) { this.manufacturerId = manufacturerId; }

    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }

    public Long getStock() { return stock; }
    public void setStock(Long stock) { this.stock = stock; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getDirectSalePrice() { return directSalePrice; }
    public void setDirectSalePrice(BigDecimal directSalePrice) { this.directSalePrice = directSalePrice; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}

