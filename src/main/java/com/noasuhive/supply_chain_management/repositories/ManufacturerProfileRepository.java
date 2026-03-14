package com.noasuhive.supply_chain_management.repositories;

import com.noasuhive.supply_chain_management.models.ManufacturerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ManufacturerProfileRepository extends JpaRepository<ManufacturerProfile, UUID> {
    Optional<ManufacturerProfile> findByUserId(UUID userId);
}

