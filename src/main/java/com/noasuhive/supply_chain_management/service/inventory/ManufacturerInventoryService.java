package com.noasuhive.supply_chain_management.service.inventory;

import com.noasuhive.supply_chain_management.dto.inventory.ManufacturerInventoryRequestDto;
import com.noasuhive.supply_chain_management.dto.inventory.ManufacturerInventoryResponseDto;

import java.util.List;
import java.util.UUID;

public interface ManufacturerInventoryService {
    List<ManufacturerInventoryResponseDto> getAllInventoryItems(UUID manufacturerId);

    List<ManufacturerInventoryResponseDto> searchInventoryItems(UUID manufacturerId, String itemName);

    ManufacturerInventoryResponseDto getInventoryItem(UUID manufacturerId, UUID productId);

    ManufacturerInventoryResponseDto createInventoryItem(UUID manufacturerId, ManufacturerInventoryRequestDto requestDto);

    ManufacturerInventoryResponseDto updateInventoryItem(UUID manufacturerId, UUID productId, ManufacturerInventoryRequestDto requestDto);

    void deleteInventoryItem(UUID manufacturerId, UUID productId);
}
