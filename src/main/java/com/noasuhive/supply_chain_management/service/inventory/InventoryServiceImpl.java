package com.noasuhive.supply_chain_management.service.inventory;

import com.noasuhive.supply_chain_management.dto.inventory.InventoryRequestDto;
import com.noasuhive.supply_chain_management.dto.inventory.InventoryResponseDto;
import com.noasuhive.supply_chain_management.exceptions.InventoryItemNotFoundException;
import com.noasuhive.supply_chain_management.models.InventoryItem;
import com.noasuhive.supply_chain_management.repositories.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryServiceImpl(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponseDto> getAllInventoryItems(UUID retailerId) {
        return inventoryRepository.findByRetailerIdOrderByItemIdAsc(retailerId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponseDto> searchInventoryItems(UUID retailerId, String itemName) {
        String normalizedItemName = normalize(itemName);
        if (normalizedItemName == null) {
            throw new IllegalArgumentException("itemName query parameter is required");
        }

        return inventoryRepository.findByRetailerIdAndItemNameContainingIgnoreCaseOrderByItemIdAsc(retailerId, normalizedItemName).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponseDto getInventoryItem(UUID retailerId, Integer itemId) {
        return toResponseDto(findInventoryItem(retailerId, itemId));
    }

    @Override
    @Transactional
    public InventoryResponseDto createInventoryItem(UUID retailerId, InventoryRequestDto requestDto) {
        if (inventoryRepository.existsByRetailerIdAndItemId(retailerId, requestDto.getItemId())) {
            throw new IllegalArgumentException("itemId " + requestDto.getItemId() + " already exists");
        }

        inventoryRepository.findByRetailerIdAndItemCode(retailerId, normalize(requestDto.getItemCode()))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("itemCode " + requestDto.getItemCode() + " already exists");
                });

        InventoryItem inventoryItem = new InventoryItem();
        inventoryItem.setRetailerId(retailerId);
        applyRequest(inventoryItem, requestDto);
        return toResponseDto(inventoryRepository.save(inventoryItem));
    }

    @Override
    @Transactional
    public InventoryResponseDto updateInventoryItem(UUID retailerId, Integer itemId, InventoryRequestDto requestDto) {
        if (!itemId.equals(requestDto.getItemId())) {
            throw new IllegalArgumentException("Path itemId and request itemId must match");
        }

        InventoryItem inventoryItem = findInventoryItem(retailerId, itemId);
        inventoryRepository.findByRetailerIdAndItemCode(retailerId, normalize(requestDto.getItemCode()))
                .filter(existing -> !existing.getItemId().equals(itemId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("itemCode " + requestDto.getItemCode() + " already exists");
                });

        applyRequest(inventoryItem, requestDto);
        return toResponseDto(inventoryRepository.save(inventoryItem));
    }

    @Override
    @Transactional
    public void deleteInventoryItem(UUID retailerId, Integer itemId) {
        InventoryItem inventoryItem = findInventoryItem(retailerId, itemId);
        inventoryRepository.delete(inventoryItem);
    }

    private InventoryItem findInventoryItem(UUID retailerId, Integer itemId) {
        return inventoryRepository.findByRetailerIdAndItemId(retailerId, itemId)
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "Inventory item with itemId " + itemId + " was not found for retailer " + retailerId));
    }

    private void applyRequest(InventoryItem inventoryItem, InventoryRequestDto requestDto) {
        inventoryItem.setItemId(requestDto.getItemId());
        inventoryItem.setItemCode(normalize(requestDto.getItemCode()));
        inventoryItem.setItemName(normalize(requestDto.getItemName()));
        inventoryItem.setItemDescription(normalize(requestDto.getItemDescription()));
        inventoryItem.setItemType(normalize(requestDto.getItemType()));
        inventoryItem.setItemSize(normalize(requestDto.getItemSize()));
        inventoryItem.setCategory(normalize(requestDto.getCategory()));
        inventoryItem.setSubCategory(normalize(requestDto.getSubCategory()));
        inventoryItem.setSupplierName(normalize(requestDto.getSupplierName()));
        inventoryItem.setUnitMeasurementType(normalize(requestDto.getUnitMeasurementType()));
        inventoryItem.setUnitName(normalize(requestDto.getUnitName()));
        inventoryItem.setQuantity(requestDto.getQuantity());
        inventoryItem.setUnitPrice(requestDto.getUnitPrice());
        inventoryItem.setDiscount(requestDto.getDiscount());
    }

    private InventoryResponseDto toResponseDto(InventoryItem inventoryItem) {
        InventoryResponseDto responseDto = new InventoryResponseDto();
        responseDto.setItemId(inventoryItem.getItemId());
        responseDto.setItemCode(inventoryItem.getItemCode());
        responseDto.setItemName(inventoryItem.getItemName());
        responseDto.setItemDescription(inventoryItem.getItemDescription());
        responseDto.setItemType(inventoryItem.getItemType());
        responseDto.setItemSize(inventoryItem.getItemSize());
        responseDto.setCategory(inventoryItem.getCategory());
        responseDto.setSubCategory(inventoryItem.getSubCategory());
        responseDto.setSupplierName(inventoryItem.getSupplierName());
        responseDto.setUnitMeasurementType(inventoryItem.getUnitMeasurementType());
        responseDto.setUnitName(inventoryItem.getUnitName());
        responseDto.setQuantity(inventoryItem.getQuantity());
        responseDto.setUnitPrice(inventoryItem.getUnitPrice());
        responseDto.setDiscount(inventoryItem.getDiscount());
        return responseDto;
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
