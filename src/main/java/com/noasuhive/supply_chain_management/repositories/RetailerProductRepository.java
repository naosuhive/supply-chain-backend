package com.noasuhive.supply_chain_management.repositories;

import com.noasuhive.supply_chain_management.models.RetailerProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RetailerProductRepository extends JpaRepository<RetailerProduct, UUID> {
    Optional<RetailerProduct> findByRetailerIdAndProductId(UUID retailerId, UUID productId);
    List<RetailerProduct> findByRetailerId(UUID retailerId);
    List<RetailerProduct> findAllByIsActiveTrue();
}

