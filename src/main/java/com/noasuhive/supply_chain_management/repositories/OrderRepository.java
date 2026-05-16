package com.noasuhive.supply_chain_management.repositories;

import com.noasuhive.supply_chain_management.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByCustomerId(UUID customerId);
    List<Order> findBySellerId(UUID sellerId);
    List<Order> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}

