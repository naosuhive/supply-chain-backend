package com.noasuhive.supply_chain_management.dto.retailer;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class RetailerProductDto {
    @NotNull
    private UUID productId;
    @NotNull
    @Min(value = 0, message = "Stock must be 0 or greater")
    private Long stock;
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true, message = "Retail price must be 0 or greater")
    private BigDecimal retailPrice;
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount must be 0 or greater")
    private BigDecimal discount;

    // Getters/Setters
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public Long getStock() { return stock; }
    public void setStock(Long stock) { this.stock = stock; }

    public BigDecimal getRetailPrice() { return retailPrice; }
    public void setRetailPrice(BigDecimal retailPrice) { this.retailPrice = retailPrice; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
}
