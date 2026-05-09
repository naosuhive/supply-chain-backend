package com.noasuhive.supply_chain_management.controllers;

import com.noasuhive.supply_chain_management.dto.inventory.InventoryRequestDto;
import com.noasuhive.supply_chain_management.dto.inventory.ManufacturerInventoryRequestDto;
import com.noasuhive.supply_chain_management.security.JwtTokenProvider;
import com.noasuhive.supply_chain_management.service.inventory.InventoryService;
import com.noasuhive.supply_chain_management.service.inventory.ManufacturerInventoryService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory", description = "Role-based inventory APIs for manufacturer stock and retailer inventory data")
public class InventoryController {

    private final InventoryService inventoryService;
    private final ManufacturerInventoryService manufacturerInventoryService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public InventoryController(
            InventoryService inventoryService,
            ManufacturerInventoryService manufacturerInventoryService,
            JwtTokenProvider jwtTokenProvider,
            ObjectMapper objectMapper,
            Validator validator) {
        this.inventoryService = inventoryService;
        this.manufacturerInventoryService = manufacturerInventoryService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Operation(
            summary = "List inventory items",
            description = "Returns manufacturer stock rows for manufacturers and retailer inventory rows for retailers using grid-ready response structures.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping
    public ResponseEntity<List<?>> getAllInventoryItems(Authentication authentication, HttpServletRequest request) {
        if (hasRole(authentication, "ROLE_MANUFACTURER")) {
            return ResponseEntity.ok(manufacturerInventoryService.getAllInventoryItems(extractManufacturerId(request)));
        }
        if (hasRole(authentication, "ROLE_RETAILER")) {
            return ResponseEntity.ok(inventoryService.getAllInventoryItems(extractRetailerId(request)));
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Manufacturer or retailer access is required for inventory data");
    }

    @Operation(
            summary = "Search inventory items by item name",
            description = "Returns manufacturer stock rows or retailer inventory rows whose names contain the supplied value, using case-insensitive partial matching.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/search")
    public ResponseEntity<List<?>> searchInventoryItems(
            Authentication authentication,
            HttpServletRequest request,
            @RequestParam String itemName) {
        if (hasRole(authentication, "ROLE_MANUFACTURER")) {
            return ResponseEntity.ok(manufacturerInventoryService.searchInventoryItems(extractManufacturerId(request), itemName));
        }
        if (hasRole(authentication, "ROLE_RETAILER")) {
            return ResponseEntity.ok(inventoryService.searchInventoryItems(extractRetailerId(request), itemName));
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Manufacturer or retailer access is required for inventory data");
    }

    @Operation(
            summary = "Get inventory item by itemId",
            description = "Returns a single manufacturer inventory row for manufacturers using UUID productIds, or a retailer inventory row for retailers using numeric itemIds. Manufacturer requests must use productId, not manufacturerId, in the path.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/{itemId}")
    public ResponseEntity<?> getInventoryItem(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable String itemId) {
        if (hasRole(authentication, "ROLE_MANUFACTURER")) {
            UUID manufacturerId = extractManufacturerId(request);
            return ResponseEntity.ok(manufacturerInventoryService.getInventoryItem(
                    manufacturerId,
                    parseManufacturerProductId(itemId, manufacturerId)));
        }
        if (hasRole(authentication, "ROLE_RETAILER")) {
            return ResponseEntity.ok(inventoryService.getInventoryItem(
                    extractRetailerId(request),
                    parseRetailerItemId(itemId)));
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Manufacturer or retailer access is required for inventory data");
    }

    @Operation(
            summary = "Create inventory item",
            description = "Creates a manufacturer inventory row for manufacturer JWTs or a retailer inventory row for retailer JWTs. Manufacturer requests may send the same grid row shape returned by the manufacturer inventory APIs; manufacturer identity still comes from the JWT.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(oneOf = {ManufacturerInventoryRequestDto.class, InventoryRequestDto.class}),
                    examples = {
                            @ExampleObject(
                                    name = "Manufacturer Request",
                                    summary = "Use this body with ROLE_MANUFACTURER",
                                    value = """
                                            {
                                              "active": true,
                                              "basePrice": 100.00,
                                              "brand": "QA",
                                              "category": "Electronics",
                                              "currentStock": 0,
                                              "directSalePrice": 120.00,
                                              "manufacturerId": "e4666b34-e4b6-479c-840e-6d918bcfbca5",
                                              "manufacturerName": "ACME Manufacturing Co.",
                                              "productDescription": null,
                                              "productId": "23f16252-c558-4c29-bba3-f595d6be1812",
                                              "productName": "Retail Fix Verification A",
                                              "subCategory": "Audio",
                                              "unitOfMeasure": "piece"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Retailer Request",
                                    summary = "Use this body with ROLE_RETAILER",
                                    value = """
                                            {
                                              "itemId": 200,
                                              "itemCode": "PL000200",
                                              "itemName": "Test item",
                                              "itemDescription": "Inventory item created from Postman",
                                              "itemType": "PVC",
                                              "itemSize": "2 inch",
                                              "category": "Plumbing",
                                              "subCategory": "Fittings",
                                              "supplierName": "test-supplier",
                                              "unitMeasurementType": "pieces",
                                              "unitName": "boxes",
                                              "quantity": 0,
                                              "unitPrice": 0,
                                              "discount": 0
                                            }
                                            """
                            )
                    }
            )
    )
    @PostMapping
    public ResponseEntity<?> createInventoryItem(
            Authentication authentication,
            HttpServletRequest request,
            @RequestBody JsonNode requestBody) {
        if (hasRole(authentication, "ROLE_MANUFACTURER")) {
            UUID manufacturerId = extractManufacturerId(request);
            ManufacturerInventoryRequestDto requestDto = convertAndValidate(requestBody, ManufacturerInventoryRequestDto.class);
            validateManufacturerRequestContext(requestDto, manufacturerId, null);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(manufacturerInventoryService.createInventoryItem(manufacturerId, requestDto));
        }
        if (hasRole(authentication, "ROLE_RETAILER")) {
            InventoryRequestDto requestDto = convertAndValidate(requestBody, InventoryRequestDto.class);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(inventoryService.createInventoryItem(extractRetailerId(request), requestDto));
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Manufacturer or retailer access is required for inventory data");
    }

    @Operation(
            summary = "Update inventory item",
            description = "Updates a manufacturer inventory row or retailer inventory row using the authenticated role and role-specific request mapping. Manufacturers use UUID productIds, not manufacturerId values, in the path and can send the same row shape returned by manufacturer inventory APIs. Retailers use numeric itemIds.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(oneOf = {ManufacturerInventoryRequestDto.class, InventoryRequestDto.class}),
                    examples = {
                            @ExampleObject(
                                    name = "Manufacturer Update Request",
                                    summary = "Use this body with ROLE_MANUFACTURER",
                                    value = """
                                            {
                                              "active": true,
                                              "basePrice": 100.00,
                                              "brand": "QA",
                                              "category": "Electronics",
                                              "currentStock": 0,
                                              "directSalePrice": 120.00,
                                              "manufacturerId": "e4666b34-e4b6-479c-840e-6d918bcfbca5",
                                              "manufacturerName": "ACME Manufacturing Co.",
                                              "productDescription": null,
                                              "productId": "23f16252-c558-4c29-bba3-f595d6be1812",
                                              "productName": "Retail Fix Verification A",
                                              "subCategory": "Audio",
                                              "unitOfMeasure": "piece",
                                              "specifications": "Optional internal specification text"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Manufacturer Update Request With Changes",
                                    summary = "Editable manufacturer row with changed values",
                                    value = """
                                            {
                                              "active": true,
                                              "basePrice": 95.00,
                                              "brand": "QA Plus",
                                              "category": "Electronics",
                                              "currentStock": 8,
                                              "directSalePrice": 115.00,
                                              "manufacturerId": "e4666b34-e4b6-479c-840e-6d918bcfbca5",
                                              "manufacturerName": "ACME Manufacturing Co.",
                                              "productDescription": "Updated manufacturer inventory row from Postman",
                                              "productId": "23f16252-c558-4c29-bba3-f595d6be1812",
                                              "productName": "Retail Fix Verification A Updated",
                                              "subCategory": "Audio",
                                              "unitOfMeasure": "piece",
                                              "specifications": "Optional internal specification text"
                                            }
                                            """
                            ),
                            @ExampleObject(
                                    name = "Retailer Update Request",
                                    summary = "Use this body with ROLE_RETAILER",
                                    value = """
                                            {
                                              "itemId": 200,
                                              "itemCode": "PL000200",
                                              "itemName": "Test item updated",
                                              "itemDescription": "Inventory item updated from Postman",
                                              "itemType": "PVC",
                                              "itemSize": "2 inch",
                                              "category": "Plumbing",
                                              "subCategory": "Fittings",
                                              "supplierName": "test-supplier",
                                              "unitMeasurementType": "pieces",
                                              "unitName": "boxes",
                                              "quantity": 12,
                                              "unitPrice": 99.50,
                                              "discount": 5.00
                                            }
                                            """
                            )
                    }
            )
    )
    @PutMapping("/{itemId}")
    public ResponseEntity<?> updateInventoryItem(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable String itemId,
            @RequestBody JsonNode requestBody) {
        if (hasRole(authentication, "ROLE_MANUFACTURER")) {
            UUID manufacturerId = extractManufacturerId(request);
            UUID productId = parseManufacturerProductId(itemId, manufacturerId);
            ManufacturerInventoryRequestDto requestDto = convertAndValidate(requestBody, ManufacturerInventoryRequestDto.class);
            validateManufacturerRequestContext(requestDto, manufacturerId, productId);
            return ResponseEntity.ok(manufacturerInventoryService.updateInventoryItem(
                    manufacturerId,
                    productId,
                    requestDto));
        }
        if (hasRole(authentication, "ROLE_RETAILER")) {
            InventoryRequestDto requestDto = convertAndValidate(requestBody, InventoryRequestDto.class);
            return ResponseEntity.ok(inventoryService.updateInventoryItem(
                    extractRetailerId(request),
                    parseRetailerItemId(itemId),
                    requestDto));
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Manufacturer or retailer access is required for inventory data");
    }

    @Operation(
            summary = "Delete inventory item",
            description = "Deletes a manufacturer inventory row by UUID productId for manufacturers, or deletes a retailer inventory row by numeric itemId for retailers.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteInventoryItem(
            Authentication authentication,
            HttpServletRequest request,
            @PathVariable String itemId) {
        if (hasRole(authentication, "ROLE_MANUFACTURER")) {
            UUID manufacturerId = extractManufacturerId(request);
            manufacturerInventoryService.deleteInventoryItem(manufacturerId, parseManufacturerProductId(itemId, manufacturerId));
            return ResponseEntity.noContent().build();
        }
        if (hasRole(authentication, "ROLE_RETAILER")) {
            inventoryService.deleteInventoryItem(extractRetailerId(request), parseRetailerItemId(itemId));
            return ResponseEntity.noContent().build();
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Manufacturer or retailer access is required for inventory data");
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication != null
                && authentication.getAuthorities().stream().anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    private Integer parseRetailerItemId(String itemId) {
        try {
            return Integer.valueOf(itemId);
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Retailer inventory itemId must be a numeric value");
        }
    }

    private UUID parseManufacturerProductId(String itemId, UUID manufacturerId) {
        try {
            UUID parsedId = UUID.fromString(itemId);
            if (parsedId.equals(manufacturerId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Use productId in /api/inventory/{itemId} for manufacturer requests. The supplied value matches the authenticated manufacturerId.");
            }

            return parsedId;
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Manufacturer inventory itemId must be a valid UUID productId");
        }
    }

    private <T> T convertAndValidate(JsonNode requestBody, Class<T> dtoType) {
        T dto = objectMapper.convertValue(requestBody, dtoType);
        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        if (!violations.isEmpty()) {
            throw new IllegalArgumentException(violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .sorted()
                    .collect(Collectors.joining(", ")));
        }
        return dto;
    }

    private void validateManufacturerRequestContext(
            ManufacturerInventoryRequestDto requestDto,
            UUID authenticatedManufacturerId,
            UUID pathProductId) {
        if (requestDto.getManufacturerId() != null && !requestDto.getManufacturerId().equals(authenticatedManufacturerId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "manufacturerId in the request body must match the authenticated manufacturer");
        }

        if (pathProductId == null) {
            if (requestDto.getProductId() != null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "productId must be omitted or null when creating manufacturer inventory items");
            }
            return;
        }

        if (requestDto.getProductId() != null && !requestDto.getProductId().equals(pathProductId)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "productId in the request body must match the path itemId for manufacturer inventory updates");
        }
    }

    private UUID extractManufacturerId(HttpServletRequest request) {
        Claims claims = extractClaims(request);
        String manufacturerId = claims.get("manufacturer_id", String.class);
        if (manufacturerId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Manufacturer access is required for manufacturer inventory data");
        }

        return UUID.fromString(manufacturerId);
    }

    private UUID extractRetailerId(HttpServletRequest request) {
        Claims claims = extractClaims(request);
        String retailerId = claims.get("retailer_id", String.class);
        if (retailerId == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Retailer access is required for inventory data");
        }

        return UUID.fromString(retailerId);
    }

    private Claims extractClaims(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Bearer token is required");
        }

        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid JWT token");
        }

        return jwtTokenProvider.getAllClaims(token);
    }
}
