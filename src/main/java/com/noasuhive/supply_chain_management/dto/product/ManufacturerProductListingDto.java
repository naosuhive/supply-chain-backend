package com.noasuhive.supply_chain_management.dto.product;

import java.math.BigDecimal;
import java.util.UUID;

public class ManufacturerProductListingDto {
    private UUID manufacturerId;
    private String manufacturerName;
    private UUID productId;
    private String productName;
    private String productDescription;
    private String productCategory;
    private String productSubCategory;
    private String productType;
    private BigDecimal unitPrice;
    private Long currentStock;
    private Integer leadTimeDays;
    private Long totalOrders;
    private Long totalUnitsSupplied;
    private Long returnUnits;
    private Integer responseTimeInHrs;

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

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public String getProductSubCategory() { return productSubCategory; }
    public void setProductSubCategory(String productSubCategory) { this.productSubCategory = productSubCategory; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public Long getCurrentStock() { return currentStock; }
    public void setCurrentStock(Long currentStock) { this.currentStock = currentStock; }

    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }

    public Long getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }

    public Long getTotalUnitsSupplied() { return totalUnitsSupplied; }
    public void setTotalUnitsSupplied(Long totalUnitsSupplied) { this.totalUnitsSupplied = totalUnitsSupplied; }

    public Long getReturnUnits() { return returnUnits; }
    public void setReturnUnits(Long returnUnits) { this.returnUnits = returnUnits; }

    public Integer getResponseTimeInHrs() { return responseTimeInHrs; }
    public void setResponseTimeInHrs(Integer responseTimeInHrs) { this.responseTimeInHrs = responseTimeInHrs; }
}
