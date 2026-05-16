package com.noasuhive.supply_chain_management.repositories;

import com.noasuhive.supply_chain_management.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByProductName(String productName);
}

