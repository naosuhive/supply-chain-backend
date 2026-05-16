package com.noasuhive.supply_chain_management.dto.product;

import java.math.BigDecimal;
import java.util.UUID;

public class ProductCatalogDto {

    private UUID productId;
    private String productName;
    private String brand;
    private String category;
    private String description;
    private String sellerType;
    private String sellerId;
    private BigDecimal price;
    private Long stock;
    private Boolean isActive;

    // Getters and setters
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSellerType() { return sellerType; }
    public void setSellerType(String sellerType) { this.sellerType = sellerType; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Long getStock() { return stock; }
    public void setStock(Long stock) { this.stock = stock; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
