package com.noasuhive.supply_chain_management.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
        name = "inventory_items",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"retailer_id", "item_id"}),
                @UniqueConstraint(columnNames = {"retailer_id", "item_code"})
        }
)
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Integer itemId;

    @Column(nullable = false)
    private UUID retailerId;

    @Column(nullable = false)
    private String itemCode;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false, length = 2000)
    private String itemDescription;

    @Column(nullable = false)
    private String itemType;

    @Column(nullable = false)
    private String itemSize;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String subCategory;

    private String supplierName;

    @Column(nullable = false)
    private String unitMeasurementType;

    @Column(nullable = false)
    private String unitName;

    private Long quantity;

    @Column(precision = 19, scale = 2)
    private BigDecimal unitPrice;

    @Column(precision = 19, scale = 2)
    private BigDecimal discount;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }

    public UUID getRetailerId() { return retailerId; }
    public void setRetailerId(UUID retailerId) { this.retailerId = retailerId; }

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
