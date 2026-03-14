package com.noasuhive.supply_chain_management.controllers;

import com.noasuhive.supply_chain_management.models.Address;
import com.noasuhive.supply_chain_management.security.JwtTokenProvider;
import com.noasuhive.supply_chain_management.service.AddressService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final JwtTokenProvider jwtTokenProvider;

    public AddressController(AddressService addressService, JwtTokenProvider jwtTokenProvider) {
        this.addressService = addressService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping
    public ResponseEntity<Address> createAddress(HttpServletRequest request, 
                                         @Valid @RequestBody Address address) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String userIdStr = claims.get("user_id", String.class);
        if (userIdStr == null) {
            return ResponseEntity.status(403).build();
        }
        
        UUID userId = UUID.fromString(userIdStr);
        Address createdAddress = addressService.createAddress(address, userId);
        return ResponseEntity.ok(createdAddress);
    }

    @GetMapping
    public ResponseEntity<List<Address>> getUserAddresses(HttpServletRequest request) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String userIdStr = claims.get("user_id", String.class);
        if (userIdStr == null) {
            return ResponseEntity.status(403).build();
        }
        
        UUID userId = UUID.fromString(userIdStr);
        List<Address> addresses = addressService.getUserAddresses(userId);
        return ResponseEntity.ok(addresses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Address> getAddressById(HttpServletRequest request,
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
        String userIdStr = claims.get("user_id", String.class);
        if (userIdStr == null) {
            return ResponseEntity.status(403).build();
        }
        
        UUID userId = UUID.fromString(userIdStr);
        Address address = addressService.getAddressById(id, userId);
        return ResponseEntity.ok(address);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(HttpServletRequest request,
                                           @PathVariable UUID id,
                                           @Valid @RequestBody Address address) {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }
        
        String token = auth.substring(7);
        if (!jwtTokenProvider.validateToken(token)) {
            return ResponseEntity.status(401).build();
        }
        
        Claims claims = jwtTokenProvider.getAllClaims(token);
        String userIdStr = claims.get("user_id", String.class);
        if (userIdStr == null) {
            return ResponseEntity.status(403).build();
        }
        
        UUID userId = UUID.fromString(userIdStr);
        Address updatedAddress = addressService.updateAddress(id, userId, address);
        return ResponseEntity.ok(updatedAddress);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(HttpServletRequest request,
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
        String userIdStr = claims.get("user_id", String.class);
        if (userIdStr == null) {
            return ResponseEntity.status(403).build();
        }
        
        UUID userId = UUID.fromString(userIdStr);
        addressService.deleteAddress(id, userId);
        return ResponseEntity.ok().build();
    }
}
