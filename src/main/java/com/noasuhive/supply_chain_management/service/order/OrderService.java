package com.noasuhive.supply_chain_management.service.order;

import com.noasuhive.supply_chain_management.dto.order.OrderCreateDto;
import com.noasuhive.supply_chain_management.dto.order.OrderResponseDto;
import com.noasuhive.supply_chain_management.dto.order.OrderStatusUpdateDto;

import java.util.List;
import java.util.UUID;

public interface OrderService {
    OrderResponseDto createOrder(OrderCreateDto orderDto, UUID customerId);
    List<OrderResponseDto> getCustomerOrders(UUID customerId);
    OrderResponseDto updateOrderStatus(UUID orderId, OrderStatusUpdateDto statusDto, UUID sellerId);
    OrderResponseDto getOrderById(UUID orderId, UUID customerId);
}
