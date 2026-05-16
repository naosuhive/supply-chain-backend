package com.noasuhive.supply_chain_management.repositories;

import com.noasuhive.supply_chain_management.models.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);
}

