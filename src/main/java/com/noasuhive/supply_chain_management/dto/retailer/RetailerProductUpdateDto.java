package com.noasuhive.supply_chain_management.dto.retailer;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public class RetailerProductUpdateDto {

    @Positive(message = "Stock must be positive")
    private Long stock;

    @Min(value = 0, message = "Price must be positive")
    private BigDecimal retailPrice;

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
