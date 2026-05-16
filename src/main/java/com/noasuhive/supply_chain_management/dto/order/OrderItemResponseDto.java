package com.noasuhive.supply_chain_management.dto.order;

import java.math.BigDecimal;
import java.util.UUID;

public class OrderItemResponseDto {

    private UUID productId;
    private String productName;
    private String brand;
    private Long quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;

    // Getters and setters
    public UUID getProductId() { return productId; }
    public void setProductId(UUID productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getTotalPrice() { return totalPrice; }
    public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
}
