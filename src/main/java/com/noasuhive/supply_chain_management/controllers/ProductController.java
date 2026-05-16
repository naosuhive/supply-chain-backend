package com.noasuhive.supply_chain_management.controllers;

import com.noasuhive.supply_chain_management.dto.product.ProductCreateDto;
import com.noasuhive.supply_chain_management.dto.product.ProductResponseDto;
import com.noasuhive.supply_chain_management.security.JwtTokenProvider;
import com.noasuhive.supply_chain_management.service.product.ProductCatalogAudience;
import com.noasuhive.supply_chain_management.service.product.ProductService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product Catalog", description = "Product catalog and direct manufacturer product APIs")
public class ProductController {

    private final ProductService productService;
    private final JwtTokenProvider jwtTokenProvider;

    public ProductController(ProductService productService, JwtTokenProvider jwtTokenProvider) {
        this.productService = productService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/manufacturer")
    public ResponseEntity<ProductResponseDto> createProduct(HttpServletRequest request, @Valid @RequestBody ProductCreateDto dto) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) return ResponseEntity.status(401).build();
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String manufacturerIdStr = claims.get("manufacturer_id", String.class);
        if (manufacturerIdStr == null) return ResponseEntity.status(403).build();
        UUID manufacturerId = UUID.fromString(manufacturerIdStr);
        ProductResponseDto resp = productService.createProduct(dto, manufacturerId);
        return ResponseEntity.ok(resp);
    }

    @GetMapping
    public ResponseEntity<List<?>> getProducts(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        ProductCatalogAudience audience = ProductCatalogAudience.fromAuthorities(authentication.getAuthorities());
        return ResponseEntity.ok(productService.getProductsForCatalogAudience(audience, authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProduct(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProduct(id));
    }
}
