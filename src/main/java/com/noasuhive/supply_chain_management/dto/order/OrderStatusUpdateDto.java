package com.noasuhive.supply_chain_management.dto.order;

import jakarta.validation.constraints.NotBlank;

public class OrderStatusUpdateDto {

    @NotBlank(message = "Order status is required")
    private String status;

    // Valid statuses
    public static final String CREATED = "CREATED";
    public static final String PAID = "PAID";
    public static final String SHIPPED = "SHIPPED";
    public static final String DELIVERED = "DELIVERED";
    public static final String CANCELLED = "CANCELLED";

    // Getters and setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
