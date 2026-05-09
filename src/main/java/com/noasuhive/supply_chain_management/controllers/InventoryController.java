package com.noasuhive.supply_chain_management.controllers;

import com.noasuhive.supply_chain_management.dto.inventory.InventoryRequestDto;
import com.noasuhive.supply_chain_management.dto.inventory.InventoryResponseDto;
import com.noasuhive.supply_chain_management.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import com.noasuhive.supply_chain_management.service.inventory.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Retailer inventory APIs backed by Excel inventory data")
public class InventoryController {

    private final InventoryService inventoryService;
    private final JwtTokenProvider jwtTokenProvider;

    public InventoryController(InventoryService inventoryService, JwtTokenProvider jwtTokenProvider) {
        this.inventoryService = inventoryService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Operation(
            summary = "List inventory items",
            description = "Returns the full inventory dataset using the exact Excel columns so the frontend can render the grid directly.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<List<InventoryResponseDto>> getAllInventoryItems(HttpServletRequest request) {
        return ResponseEntity.ok(inventoryService.getAllInventoryItems(extractRetailerId(request)));
    }

    @Operation(
            summary = "Search inventory items by item name",
            description = "Returns retailer inventory rows whose itemName contains the supplied value, using case-insensitive partial matching and the same grid-ready response structure.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/search")
    public ResponseEntity<List<InventoryResponseDto>> searchInventoryItems(
            HttpServletRequest request,
            @RequestParam String itemName) {
        return ResponseEntity.ok(inventoryService.searchInventoryItems(extractRetailerId(request), itemName));
    }

    @Operation(
            summary = "Get inventory item by itemId",
            description = "Returns a single inventory item using the exact Excel field structure.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{itemId:\\d+}")
    public ResponseEntity<InventoryResponseDto> getInventoryItem(
            HttpServletRequest request,
            @PathVariable Integer itemId) {
        return ResponseEntity.ok(inventoryService.getInventoryItem(extractRetailerId(request), itemId));
    }

    @Operation(
            summary = "Create inventory item",
            description = "Creates an inventory item using the exact Excel fields and without old basePrice or directSalePrice logic.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PostMapping
    public ResponseEntity<InventoryResponseDto> createInventoryItem(
            HttpServletRequest request,
            @Valid @RequestBody InventoryRequestDto requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inventoryService.createInventoryItem(extractRetailerId(request), requestDto));
    }

    @Operation(
            summary = "Update inventory item",
            description = "Updates an inventory item using the exact Excel field structure.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @PutMapping("/{itemId}")
    public ResponseEntity<InventoryResponseDto> updateInventoryItem(
            HttpServletRequest request,
            @PathVariable Integer itemId,
            @Valid @RequestBody InventoryRequestDto requestDto) {
        return ResponseEntity.ok(inventoryService.updateInventoryItem(extractRetailerId(request), itemId, requestDto));
    }

    @Operation(
            summary = "Delete inventory item",
            description = "Deletes an inventory item by itemId.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteInventoryItem(HttpServletRequest request, @PathVariable Integer itemId) {
        inventoryService.deleteInventoryItem(extractRetailerId(request), itemId);
        return ResponseEntity.noContent().build();
    }

    private UUID extractRetailerId(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token is required");
        }

        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token");
        }

        Claims claims = jwtTokenProvider.getAllClaims(token);
        String retailerId = claims.get("retailer_id", String.class);
        if (retailerId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Retailer access is required for inventory data");
        }

        return UUID.fromString(retailerId);
    }
}
