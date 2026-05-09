package com.noasuhive.supply_chain_management.dto.inventory;

import java.math.BigDecimal;
import java.util.UUID;

public class ManufacturerInventoryResponseDto {
    private UUID manufacturerId;
    private String manufacturerName;
    private UUID productId;
    private String productName;
    private String productDescription;
    private String category;
    private String subCategory;
    private String brand;
    private String unitOfMeasure;
    private Long currentStock;
    private BigDecimal basePrice;
    private BigDecimal directSalePrice;
    private Boolean active;

    public UUID getManufacturerId() { return manufacturerId; }
    public void setManufacturerId(UUID manufacturerId) { this.manufacturerId = manufacturerId; }

    public String getManufacturerName() { return manufacturerName; }
    public void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public Long getCurrentStock() { return currentStock; }
    public void setCurrentStock(Long currentStock) { this.currentStock = currentStock; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getDirectSalePrice() { return directSalePrice; }
    public void setDirectSalePrice(BigDecimal directSalePrice) { this.directSalePrice = directSalePrice; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
