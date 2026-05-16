package com.noasuhive.supply_chain_management.service.product;

import com.noasuhive.supply_chain_management.exceptions.UnauthorizedAccessException;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public enum ProductCatalogAudience {
    MANUFACTURER,
    RETAILER,
    CUSTOMER;

    public static ProductCatalogAudience fromAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<String> roles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (roles.contains("ROLE_MANUFACTURER")) {
            return MANUFACTURER;
        }
        if (roles.contains("ROLE_RETAILER")) {
            return RETAILER;
        }
        if (roles.contains("ROLE_USER") || roles.contains("ROLE_CUSTOMER")) {
            return CUSTOMER;
        }

        throw new UnauthorizedAccessException("Your role is not allowed to use /api/products");
    }
}
