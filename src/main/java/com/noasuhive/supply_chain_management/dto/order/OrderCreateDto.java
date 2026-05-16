package com.noasuhive.supply_chain_management.dto.order;

import com.noasuhive.supply_chain_management.models.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class OrderCreateDto {

    @NotNull(message = "Seller type is required")
    private Order.SellerType sellerType;

    @NotNull(message = "Seller ID is required")
    private String sellerId;

    @NotNull(message = "Shipping address is required")
    private String shippingAddressId;

    @NotEmpty(message = "Order items cannot be empty")
    @Valid
    private List<OrderItemDto> items;

    // Getters and setters
    public Order.SellerType getSellerType() { return sellerType; }
    public void setSellerType(Order.SellerType sellerType) { this.sellerType = sellerType; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public String getShippingAddressId() { return shippingAddressId; }
    public void setShippingAddressId(String shippingAddressId) { this.shippingAddressId = shippingAddressId; }

    public List<OrderItemDto> getItems() { return items; }
    public void setItems(List<OrderItemDto> items) { this.items = items; }
}
