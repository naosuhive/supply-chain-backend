package com.noasuhive.supply_chain_management.dto.retailer;

import java.math.BigDecimal;
import java.util.UUID;

public class RetailerProductResponseDto {
    private UUID id;
    private UUID productId;
    private String productName;
    private String category;
    private String brand;
    private BigDecimal retailPrice;
    private BigDecimal discount;
    private Long stock;
    private boolean isActive;

    // Getters/Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public Long getStock() { return stock; }
    public void setStock(Long stock) { this.stock = stock; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}
