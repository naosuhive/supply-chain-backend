package com.noasuhive.supply_chain_management.repositories.projections;

import java.util.UUID;

public interface ProductSalesMetricsView {
    UUID getProductId();
    Long getTotalOrders();
    Long getTotalUnitsSupplied();
}
