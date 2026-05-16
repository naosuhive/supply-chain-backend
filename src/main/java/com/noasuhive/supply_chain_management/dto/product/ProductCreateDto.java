package com.noasuhive.supply_chain_management.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ProductCreateDto {

    @NotBlank
    private String productName;

    private String category;
    private String subcategory;
    private String brand;
    private String unitOfMeasure;
    private String specifications;
    private String description;

    @NotNull
    private BigDecimal basePrice;

    private BigDecimal directSalePrice; // Price for direct customer sales

    @Min(value = 0, message = "Stock must be non-negative")
    private Long stock;

    // Getters and setters
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubcategory() { return subcategory; }
    public void setSubcategory(String subcategory) { this.subcategory = subcategory; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public String getSpecifications() { return specifications; }
    public void setSpecifications(String specifications) { this.specifications = specifications; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getDirectSalePrice() { return directSalePrice; }
    public void setDirectSalePrice(BigDecimal directSalePrice) { this.directSalePrice = directSalePrice; }

    public Long getStock() { return stock; }
    public void setStock(Long stock) { this.stock = stock; }
}
