package com.noasuhive.supply_chain_management.dto.product;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductResponseDto {
    private UUID id;
    private String productName;
    private String category;
    private String brand;
    private BigDecimal basePrice;

    // Getters/Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
}

