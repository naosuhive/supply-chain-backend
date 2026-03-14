package com.noasuhive.supply_chain_management.repositories;

import com.noasuhive.supply_chain_management.models.ManufacturerProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ManufacturerProductRepository extends JpaRepository<ManufacturerProduct, UUID> {
    Optional<ManufacturerProduct> findByManufacturerIdAndProductId(UUID manufacturerId, UUID productId);
    List<ManufacturerProduct> findByManufacturerId(UUID manufacturerId);
    Optional<ManufacturerProduct> findByProductId(UUID productId);
    List<ManufacturerProduct> findAllByIsActiveTrue();
}
