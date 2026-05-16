package com.noasuhive.supply_chain_management.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class ProductUpdateDto {

    @Positive(message = "Stock must be positive")
    private Long stock;

    @Min(value = 0, message = "Price must be positive")
    private BigDecimal basePrice;

    @Min(value = 0, message = "Price must be positive")
    private BigDecimal directSalePrice;

    private Boolean isActive;

    // Getters and setters
    public Long getStock() { return stock; }
    public void setStock(Long stock) { this.stock = stock; }

    public BigDecimal getBasePrice() { return basePrice; }
    public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }

    public BigDecimal getDirectSalePrice() { return directSalePrice; }
    public void setDirectSalePrice(BigDecimal directSalePrice) { this.directSalePrice = directSalePrice; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
