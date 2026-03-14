package com.noasuhive.supply_chain_management.controllers;

import com.noasuhive.supply_chain_management.dto.order.*;
import com.noasuhive.supply_chain_management.security.JwtTokenProvider;
import com.noasuhive.supply_chain_management.service.order.OrderService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final JwtTokenProvider jwtTokenProvider;

    public OrderController(OrderService orderService, JwtTokenProvider jwtTokenProvider) {
        this.orderService = orderService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> createOrder(HttpServletRequest request, 
                                                     @Valid @RequestBody OrderCreateDto orderDto) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String customerIdStr = claims.get("user_id", String.class);
        if (customerIdStr == null) {
            return ResponseEntity.status(403).build();
        }
        
        UUID customerId = UUID.fromString(customerIdStr);
        OrderResponseDto response = orderService.createOrder(orderDto, customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponseDto>> getCustomerOrders(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String customerIdStr = claims.get("user_id", String.class);
        if (customerIdStr == null) {
            return ResponseEntity.status(403).build();
        }
        
        UUID customerId = UUID.fromString(customerIdStr);
        List<OrderResponseDto> orders = orderService.getCustomerOrders(customerId);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponseDto> getOrderById(HttpServletRequest request, 
                                                   @PathVariable UUID id) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String customerIdStr = claims.get("user_id", String.class);
        if (customerIdStr == null) {
            return ResponseEntity.status(403).build();
        }
        
        UUID customerId = UUID.fromString(customerIdStr);
        OrderResponseDto order = orderService.getOrderById(id, customerId);
        return ResponseEntity.ok(order);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponseDto> updateOrderStatus(HttpServletRequest request,
                                                         @PathVariable UUID id,
                                                         @Valid @RequestBody OrderStatusUpdateDto statusDto) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String sellerIdStr = null;
        String role = null;
        
        // Check if manufacturer or retailer
        if (claims.get("manufacturer_id") != null) {
            sellerIdStr = claims.get("manufacturer_id", String.class);
            role = "MANUFACTURER";
        } else if (claims.get("retailer_id") != null) {
            sellerIdStr = claims.get("retailer_id", String.class);
            role = "RETAILER";
        }
        
        if (sellerIdStr == null) {
            return ResponseEntity.status(403).build();
        }
        
        UUID sellerId = UUID.fromString(sellerIdStr);
        OrderResponseDto order = orderService.updateOrderStatus(id, statusDto, sellerId);
        return ResponseEntity.ok(order);
    }
}
