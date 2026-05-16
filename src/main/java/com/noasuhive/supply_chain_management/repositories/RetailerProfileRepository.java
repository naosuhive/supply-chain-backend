package com.noasuhive.supply_chain_management.repositories;

import com.noasuhive.supply_chain_management.models.RetailerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RetailerProfileRepository extends JpaRepository<RetailerProfile, UUID> {
    Optional<RetailerProfile> findByUserId(UUID userId);

    Optional<RetailerProfile> findFirstByOrderByCreatedAtAsc();
}
