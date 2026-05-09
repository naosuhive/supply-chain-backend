package com.noasuhive.supply_chain_management;

import com.noasuhive.supply_chain_management.dto.inventory.InventoryRequestDto;
import com.noasuhive.supply_chain_management.dto.inventory.InventoryResponseDto;
import com.noasuhive.supply_chain_management.service.inventory.InventoryService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class InventoryServiceTests {

    private static final UUID RETAILER_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440031");

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private Validator validator;

    @Test
    void getInventoryListReturnsGridReadyRows() {
        List<InventoryResponseDto> rows = inventoryService.getAllInventoryItems(RETAILER_ID);

        assertEquals(149, rows.size());

        InventoryResponseDto firstRow = rows.getFirst();
        assertEquals(1, firstRow.getItemId());
        assertEquals("PL00001", firstRow.getItemCode());
        assertEquals("Lbow", firstRow.getItemName());
        assertEquals("1/2'' pvc lbow", firstRow.getItemDescription());
        assertEquals("PVC", firstRow.getItemType());
        assertEquals("1/2 inch", firstRow.getItemSize());
        assertEquals("Plumbing", firstRow.getCategory());
        assertEquals("Fittings", firstRow.getSubCategory());
        assertEquals("waterflo", firstRow.getSupplierName());
        assertEquals("pieces", firstRow.getUnitMeasurementType());
        assertEquals("bags", firstRow.getUnitName());
    }

    @Test
    void getInventoryListIsScopedToRetailer() {
        List<InventoryResponseDto> rows = inventoryService.getAllInventoryItems(UUID.randomUUID());
        assertTrue(rows.isEmpty());
    }

    @Test
    void searchInventoryItemsReturnsCaseInsensitivePartialMatches() {
        List<InventoryResponseDto> rows = inventoryService.searchInventoryItems(RETAILER_ID, "lBoW");

        assertTrue(rows.size() >= 3);
        assertTrue(rows.stream()
                .allMatch(row -> row.getItemName().toLowerCase().contains("lbow")));
        assertTrue(rows.stream().anyMatch(row -> "Lbow".equals(row.getItemName())));
        assertTrue(rows.stream().anyMatch(row -> "thread lbow".equals(row.getItemName())));
        assertTrue(rows.stream().anyMatch(row -> "Brass lbow".equals(row.getItemName())));
    }

    @Test
    void searchInventoryItemsRequiresItemName() {
        try {
            inventoryService.searchInventoryItems(RETAILER_ID, "   ");
        } catch (IllegalArgumentException ex) {
            assertEquals("itemName query parameter is required", ex.getMessage());
            return;
        }

        throw new AssertionError("Expected IllegalArgumentException");
    }

    @Test
    void searchInventoryItemsIsScopedToRetailer() {
        List<InventoryResponseDto> rows = inventoryService.searchInventoryItems(UUID.randomUUID(), "lbow");
        assertTrue(rows.isEmpty());
    }

    @Test
    void inventoryRequestAllowsZeroQuantityAndZeroUnitPrice() {
        InventoryRequestDto requestDto = validRequest();
        requestDto.setQuantity(0L);
        requestDto.setUnitPrice(BigDecimal.ZERO);

        Set<ConstraintViolation<InventoryRequestDto>> violations = validator.validate(requestDto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void inventoryRequestRejectsNegativeUnitPrice() {
        InventoryRequestDto requestDto = validRequest();
        requestDto.setQuantity(0L);
        requestDto.setUnitPrice(new BigDecimal("-1.00"));

        Set<ConstraintViolation<InventoryRequestDto>> violations = validator.validate(requestDto);
        assertTrue(violations.stream()
                .anyMatch(violation -> "unitPrice".equals(violation.getPropertyPath().toString())));
    }

    private InventoryRequestDto validRequest() {
        InventoryRequestDto requestDto = new InventoryRequestDto();
        requestDto.setItemId(200);
        requestDto.setItemCode("PL000200");
        requestDto.setItemName("Test item");
        requestDto.setItemDescription("Test inventory item");
        requestDto.setItemType("PVC");
        requestDto.setItemSize("2 inch");
        requestDto.setCategory("Plumbing");
        requestDto.setSubCategory("Fittings");
        requestDto.setSupplierName("test-supplier");
        requestDto.setUnitMeasurementType("pieces");
        requestDto.setUnitName("boxes");
        requestDto.setDiscount(BigDecimal.ZERO);
        return requestDto;
    }
}
