package com.noasuhive.supply_chain_management.repositories;

import com.noasuhive.supply_chain_management.models.Order;
import com.noasuhive.supply_chain_management.models.OrderItem;
import com.noasuhive.supply_chain_management.repositories.projections.ProductSalesMetricsView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
    List<OrderItem> findByOrderId(UUID orderId);

    @Query("""
            select
                oi.productId as productId,
                count(distinct o.id) as totalOrders,
                coalesce(sum(oi.quantity), 0) as totalUnitsSupplied
            from OrderItem oi
            join oi.order o
            where o.sellerType = :sellerType
              and oi.productId in :productIds
            group by oi.productId
            """)
    List<ProductSalesMetricsView> summarizeSalesBySellerTypeAndProductIds(
            @Param("sellerType") Order.SellerType sellerType,
            @Param("productIds") Collection<UUID> productIds);
}
