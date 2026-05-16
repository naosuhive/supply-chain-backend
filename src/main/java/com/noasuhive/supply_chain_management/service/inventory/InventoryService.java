package com.noasuhive.supply_chain_management.service.inventory;

import com.noasuhive.supply_chain_management.dto.inventory.InventoryRequestDto;
import com.noasuhive.supply_chain_management.dto.inventory.InventoryResponseDto;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    List<InventoryResponseDto> getAllInventoryItems(UUID retailerId);

    List<InventoryResponseDto> searchInventoryItems(UUID retailerId, String itemName);

    InventoryResponseDto getInventoryItem(UUID retailerId, Integer itemId);

    InventoryResponseDto createInventoryItem(UUID retailerId, InventoryRequestDto requestDto);

    InventoryResponseDto updateInventoryItem(UUID retailerId, Integer itemId, InventoryRequestDto requestDto);

    void deleteInventoryItem(UUID retailerId, Integer itemId);
}
