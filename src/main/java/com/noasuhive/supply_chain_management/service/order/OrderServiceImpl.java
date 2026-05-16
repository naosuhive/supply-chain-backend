package com.noasuhive.supply_chain_management.service.order;

import com.noasuhive.supply_chain_management.dto.order.*;
import com.noasuhive.supply_chain_management.exceptions.InsufficientStockException;
import com.noasuhive.supply_chain_management.exceptions.ProductNotFoundException;
import com.noasuhive.supply_chain_management.exceptions.UnauthorizedAccessException;
import com.noasuhive.supply_chain_management.models.*;
import com.noasuhive.supply_chain_management.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ManufacturerProductRepository manufacturerProductRepository;
    private final RetailerProductRepository retailerProductRepository;
    private final AddressRepository addressRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         ProductRepository productRepository,
                         ManufacturerProductRepository manufacturerProductRepository,
                         RetailerProductRepository retailerProductRepository,
                         AddressRepository addressRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.manufacturerProductRepository = manufacturerProductRepository;
        this.retailerProductRepository = retailerProductRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto orderDto, UUID customerId) {
        // Validate shipping address
        Address shippingAddress = addressRepository.findById(UUID.fromString(orderDto.getShippingAddressId()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid shipping address"));

        // Validate seller
        UUID sellerId = UUID.fromString(orderDto.getSellerId());
        validateSellerAccess(orderDto.getSellerType(), sellerId, orderDto.getItems());

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerId(customerId);
        order.setSellerId(sellerId);
        order.setSellerType(Order.SellerType.valueOf(orderDto.getSellerType().name()));
        order.setShippingAddressId(UUID.fromString(orderDto.getShippingAddressId()));
        order.setOrderStatus(OrderStatusUpdateDto.CREATED);
        order = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OrderItemDto itemDto : orderDto.getItems()) {
            Product product = productRepository.findById(itemDto.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found: " + itemDto.getProductId()));

            BigDecimal unitPrice = getProductPrice(orderDto.getSellerType(), sellerId, itemDto.getProductId());
            BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(itemDto.getQuantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProductId(itemDto.getProductId());
            orderItem.setQuantity(itemDto.getQuantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(totalPrice);
            orderItem = orderItemRepository.save(orderItem);

            orderItems.add(orderItem);
            totalAmount = totalAmount.add(totalPrice);

            // Update stock
            updateStock(orderDto.getSellerType(), sellerId, itemDto.getProductId(), itemDto.getQuantity());
        }

        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        return toOrderResponseDto(order, orderItems);
    }

    @Override
    public List<OrderResponseDto> getCustomerOrders(UUID customerId) {
        List<Order> orders = orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return orders.stream()
                .map(order -> {
                    List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
                    return toOrderResponseDto(order, items);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(UUID orderId, OrderStatusUpdateDto statusDto, UUID sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Validate seller access
        if (!order.getSellerId().equals(sellerId)) {
            throw new UnauthorizedAccessException("You can only update status for your own orders");
        }

        // Validate status
        validateOrderStatus(statusDto.getStatus());

        order.setOrderStatus(statusDto.getStatus());
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toOrderResponseDto(order, items);
    }

    @Override
    public OrderResponseDto getOrderById(UUID orderId, UUID customerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // Validate customer access
        if (!order.getCustomerId().equals(customerId)) {
            throw new UnauthorizedAccessException("You can only view your own orders");
        }

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        return toOrderResponseDto(order, items);
    }

    private void validateSellerAccess(Order.SellerType sellerType, UUID sellerId, List<OrderItemDto> items) {
        if (sellerType == Order.SellerType.MANUFACTURER) {
            for (OrderItemDto item : items) {
                ManufacturerProduct mp = manufacturerProductRepository
                        .findByManufacturerIdAndProductId(sellerId, item.getProductId())
                        .orElseThrow(() -> new UnauthorizedAccessException("Product not found in manufacturer catalog"));
                if (!mp.isActive()) {
                    throw new UnauthorizedAccessException("Product is not active");
                }
            }
        } else if (sellerType == Order.SellerType.RETAILER) {
            for (OrderItemDto item : items) {
                RetailerProduct rp = retailerProductRepository
                        .findByRetailerIdAndProductId(sellerId, item.getProductId())
                        .orElseThrow(() -> new UnauthorizedAccessException("Product not found in retailer catalog"));
                if (!rp.isActive()) {
                    throw new UnauthorizedAccessException("Product is not active");
                }
            }
        }
    }

    private BigDecimal getProductPrice(Order.SellerType sellerType, UUID sellerId, UUID productId) {
        if (sellerType == Order.SellerType.MANUFACTURER) {
            ManufacturerProduct mp = manufacturerProductRepository
                    .findByManufacturerIdAndProductId(sellerId, productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found in manufacturer catalog"));
            // Use direct sale price if available, otherwise fall back to base price
            BigDecimal price = mp.getDirectSalePrice() != null ? mp.getDirectSalePrice() : mp.getBasePrice();
            if (price == null) {
                throw new ProductNotFoundException("Product price not set for manufacturer product: " + productId);
            }
            return price;
        } else {
            RetailerProduct rp = retailerProductRepository
                    .findByRetailerIdAndProductId(sellerId, productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found in retailer catalog"));
            return rp.getRetailPrice();
        }
    }

    private void updateStock(Order.SellerType sellerType, UUID sellerId, UUID productId, Long quantity) {
        if (sellerType == Order.SellerType.MANUFACTURER) {
            ManufacturerProduct mp = manufacturerProductRepository
                    .findByManufacturerIdAndProductId(sellerId, productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found"));
            if (mp.getStock() < quantity) {
                throw new InsufficientStockException("Insufficient stock for product: " + productId);
            }
            mp.setStock(mp.getStock() - quantity);
            manufacturerProductRepository.save(mp);
        } else {
            RetailerProduct rp = retailerProductRepository
                    .findByRetailerIdAndProductId(sellerId, productId)
                    .orElseThrow(() -> new ProductNotFoundException("Product not found"));
            if (rp.getStock() < quantity) {
                throw new InsufficientStockException("Insufficient stock for product: " + productId);
            }
            rp.setStock(rp.getStock() - quantity);
            retailerProductRepository.save(rp);
        }
    }

    private void validateOrderStatus(String status) {
        List<String> validStatuses = List.of(
                OrderStatusUpdateDto.CREATED,
                OrderStatusUpdateDto.PAID,
                OrderStatusUpdateDto.SHIPPED,
                OrderStatusUpdateDto.DELIVERED,
                OrderStatusUpdateDto.CANCELLED
        );

        if (!validStatuses.contains(status)) {
            throw new IllegalArgumentException("Invalid order status: " + status);
        }
    }

    private String generateOrderNumber() {
        return "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    }

    private OrderResponseDto toOrderResponseDto(Order order, List<OrderItem> items) {
        OrderResponseDto dto = new OrderResponseDto();
        dto.setId(order.getId());
        dto.setOrderNumber(order.getOrderNumber());
        dto.setCustomerId(order.getCustomerId());
        dto.setSellerId(order.getSellerId().toString());
        dto.setSellerType(order.getSellerType().name());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setOrderStatus(order.getOrderStatus());
        dto.setCreatedAt(order.getCreatedAt());

        List<OrderItemResponseDto> itemDtos = items.stream()
                .map(this::toOrderItemResponseDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);

        return dto;
    }

    private OrderItemResponseDto toOrderItemResponseDto(OrderItem item) {
        Product product = productRepository.findById(item.getProductId()).orElse(null);
        
        OrderItemResponseDto dto = new OrderItemResponseDto();
        dto.setProductId(item.getProductId());
        dto.setProductName(product != null ? product.getProductName() : "Unknown");
        dto.setBrand(product != null ? product.getBrand() : "Unknown");
        dto.setQuantity(item.getQuantity());
        dto.setUnitPrice(item.getUnitPrice());
        dto.setTotalPrice(item.getTotalPrice());
        
        return dto;
    }
}
