package com.noasuhive.supply_chain_management.repositories;

import com.noasuhive.supply_chain_management.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID> {
    List<Address> findByUserId(UUID userId);
}

