package com.noasuhive.supply_chain_management.dto.retailer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public class RetailerProductUpdateDto {

    @Min(value = 0, message = "Stock must be 0 or greater")
    private Long stock;

    @DecimalMin(value = "0.0", inclusive = true, message = "Retail price must be 0 or greater")
    private BigDecimal retailPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "Discount must be 0 or greater")
    private BigDecimal discount;

    private Boolean isActive;

    // Getters and setters
    public Long getStock() { return stock; }
    public void setStock(Long stock) { this.stock = stock; }

    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
