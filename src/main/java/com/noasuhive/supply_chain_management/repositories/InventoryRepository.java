package com.noasuhive.supply_chain_management.repositories;

import com.noasuhive.supply_chain_management.models.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<InventoryItem, UUID> {

    boolean existsByRetailerIdAndItemId(UUID retailerId, Integer itemId);

    Optional<InventoryItem> findByRetailerIdAndItemId(UUID retailerId, Integer itemId);

    Optional<InventoryItem> findByRetailerIdAndItemCode(UUID retailerId, String itemCode);

    List<InventoryItem> findByRetailerIdOrderByItemIdAsc(UUID retailerId);

    List<InventoryItem> findByRetailerIdAndItemNameContainingIgnoreCaseOrderByItemIdAsc(UUID retailerId, String itemName);
}
