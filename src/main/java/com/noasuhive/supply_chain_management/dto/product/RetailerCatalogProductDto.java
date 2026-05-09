package com.noasuhive.supply_chain_management.dto.product;

import java.math.BigDecimal;
import java.util.UUID;

public class RetailerCatalogProductDto {
    private UUID retailerProductId;
    private UUID retailerId;
    private String retailerName;
    private UUID productId;
    private String productName;
    private String productDescription;
    private String productCategory;
    private String productSubCategory;
    private String brand;
    private String unitOfMeasure;
    private BigDecimal retailPrice;
    private BigDecimal discount;
    private Long currentStock;
    private Boolean active;

    public UUID getRetailerProductId() { return retailerProductId; }
    public void setRetailerProductId(UUID retailerProductId) { this.retailerProductId = retailerProductId; }

    public UUID getRetailerId() { return retailerId; }
    public void setRetailerId(UUID retailerId) { this.retailerId = retailerId; }

    public String getRetailerName() { return retailerName; }
    public void setRetailerName(String retailerName) { this.retailerName = retailerName; }

    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductDescription() { return productDescription; }
    public void setProductDescription(String productDescription) { this.productDescription = productDescription; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public String getProductSubCategory() { return productSubCategory; }
    public void setProductSubCategory(String productSubCategory) { this.productSubCategory = productSubCategory; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public Long getCurrentStock() { return currentStock; }
    public void setCurrentStock(Long currentStock) { this.currentStock = currentStock; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
