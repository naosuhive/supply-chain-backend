package com.noasuhive.supply_chain_management.dto.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class InventoryRequestDto {

    @NotNull(message = "itemId is required")
    private Integer itemId;

    @NotBlank(message = "itemCode is required")
    private String itemCode;

    @NotBlank(message = "itemName is required")
    private String itemName;

    @NotBlank(message = "itemDescription is required")
    private String itemDescription;

    @NotBlank(message = "itemType is required")
    private String itemType;

    @NotBlank(message = "itemSize is required")
    private String itemSize;

    @NotBlank(message = "category is required")
    private String category;

    @NotBlank(message = "subCategory is required")
    private String subCategory;

    private String supplierName;

    @NotBlank(message = "unitMeasurementType is required")
    private String unitMeasurementType;

    @NotBlank(message = "unitName is required")
    private String unitName;

    @Min(value = 0, message = "quantity must be 0 or greater")
    private Long quantity;

    @DecimalMin(value = "0.0", inclusive = true, message = "unitPrice must be 0 or greater")
    private BigDecimal unitPrice;

    @DecimalMin(value = "0.0", inclusive = true, message = "discount must be 0 or greater")
    private BigDecimal discount;

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public String getItemCode() { return itemCode; }
    public void setItemCode(String itemCode) { this.itemCode = itemCode; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getItemDescription() { return itemDescription; }
    public void setItemDescription(String itemDescription) { this.itemDescription = itemDescription; }

    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }

    public String getItemSize() { return itemSize; }
    public void setItemSize(String itemSize) { this.itemSize = itemSize; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getSubCategory() { return subCategory; }
    public void setSubCategory(String subCategory) { this.subCategory = subCategory; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getUnitMeasurementType() { return unitMeasurementType; }
    public void setUnitMeasurementType(String unitMeasurementType) { this.unitMeasurementType = unitMeasurementType; }

    public String getUnitName() { return unitName; }
    public void setUnitName(String unitName) { this.unitName = unitName; }

    public Long getQuantity() { return quantity; }
    public void setQuantity(Long quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }
}
