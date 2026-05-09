package com.noasuhive.supply_chain_management.controllers;

import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductDto;
import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductResponseDto;
import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductUpdateDto;
import com.noasuhive.supply_chain_management.security.JwtTokenProvider;
import com.noasuhive.supply_chain_management.service.retailer.RetailerService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/retailer")
@Tag(name = "Retailer Operations", description = "Retailer catalog and pricing management APIs")
public class RetailerController {

    private final RetailerService retailerService;
    private final JwtTokenProvider jwtTokenProvider;

    public RetailerController(RetailerService retailerService, JwtTokenProvider jwtTokenProvider) {
        this.retailerService = retailerService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/products")
    public ResponseEntity<Void> addProduct(HttpServletRequest request, @Valid @RequestBody RetailerProductDto dto) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.status(401).build();
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) return ResponseEntity.status(401).build();
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String retailerIdStr = claims.get("retailer_id", String.class);
        if (retailerIdStr == null) return ResponseEntity.status(403).build();
        UUID retailerId = UUID.fromString(retailerIdStr);
        retailerService.addProductToCatalog(retailerId, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/products")
    public ResponseEntity<List<RetailerProductResponseDto>> listProducts(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.status(401).build();
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) return ResponseEntity.status(401).build();
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String retailerIdStr = claims.get("retailer_id", String.class);
        if (retailerIdStr == null) return ResponseEntity.status(403).build();
        UUID retailerId = UUID.fromString(retailerIdStr);
        return ResponseEntity.ok(retailerService.listRetailerProducts(retailerId));
    }

    @PatchMapping("/products/{id}")
    public ResponseEntity<RetailerProductResponseDto> updateRetailerProduct(HttpServletRequest request,
                                                               @PathVariable UUID id,
                                                               @Valid @RequestBody RetailerProductUpdateDto updateDto) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.status(401).build();
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) return ResponseEntity.status(401).build();
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String retailerIdStr = claims.get("retailer_id", String.class);
        if (retailerIdStr == null) return ResponseEntity.status(403).build();
        UUID retailerId = UUID.fromString(retailerIdStr);
        
        RetailerProductResponseDto updated = retailerService.updateRetailerProduct(id, retailerId, updateDto);
        return ResponseEntity.ok(updated);
    }
}
