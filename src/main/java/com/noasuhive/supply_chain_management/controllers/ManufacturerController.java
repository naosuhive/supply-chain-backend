package com.noasuhive.supply_chain_management.controllers;

import com.noasuhive.supply_chain_management.dto.manufacturer.ManufacturerProductResponseDto;
import com.noasuhive.supply_chain_management.dto.product.ProductCreateDto;
import com.noasuhive.supply_chain_management.dto.product.ProductUpdateDto;
import com.noasuhive.supply_chain_management.security.JwtTokenProvider;
import com.noasuhive.supply_chain_management.service.manufacturer.ManufacturerService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/manufacturer")
@Tag(name = "Manufacturer Operations", description = "Manufacturer product management APIs")
public class ManufacturerController {

    private final ManufacturerService manufacturerService;
    private final JwtTokenProvider jwtTokenProvider;

    public ManufacturerController(ManufacturerService manufacturerService, JwtTokenProvider jwtTokenProvider) {
        this.manufacturerService = manufacturerService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/products")
    public ResponseEntity<ManufacturerProductResponseDto> createProduct(HttpServletRequest request, @Valid @RequestBody ProductCreateDto dto) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.status(401).build();
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) return ResponseEntity.status(401).build();
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String manufacturerIdStr = claims.get("manufacturer_id", String.class);
        if (manufacturerIdStr == null) return ResponseEntity.status(403).build();
        UUID manufacturerId = UUID.fromString(manufacturerIdStr);
        ManufacturerProductResponseDto resp = manufacturerService.createProduct(manufacturerId, dto);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/products")
    public ResponseEntity<List<ManufacturerProductResponseDto>> listProducts(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.status(401).build();
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) return ResponseEntity.status(401).build();
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String manufacturerIdStr = claims.get("manufacturer_id", String.class);
        if (manufacturerIdStr == null) return ResponseEntity.status(403).build();
        UUID manufacturerId = UUID.fromString(manufacturerIdStr);
        return ResponseEntity.ok(manufacturerService.listProducts(manufacturerId));
    }

    @PatchMapping("/products/{id}")
    public ResponseEntity<ManufacturerProductResponseDto> updateProduct(HttpServletRequest request,
                                                           @PathVariable UUID id,
                                                           @Valid @RequestBody ProductUpdateDto updateDto) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) return ResponseEntity.status(401).build();
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) return ResponseEntity.status(401).build();
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String manufacturerIdStr = claims.get("manufacturer_id", String.class);
        if (manufacturerIdStr == null) return ResponseEntity.status(403).build();
        UUID manufacturerId = UUID.fromString(manufacturerIdStr);
        
        ManufacturerProductResponseDto updated = manufacturerService.updateProduct(id, manufacturerId, updateDto);
        return ResponseEntity.ok(updated);
    }
}
