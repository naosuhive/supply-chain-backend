package com.noasuhive.supply_chain_management;

import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductDto;
import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductResponseDto;
import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductUpdateDto;
import com.noasuhive.supply_chain_management.models.Product;
import com.noasuhive.supply_chain_management.repositories.ProductRepository;
import com.noasuhive.supply_chain_management.repositories.RetailerProfileRepository;
import com.noasuhive.supply_chain_management.repositories.UserRepository;
import com.noasuhive.supply_chain_management.service.retailer.RetailerService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class RetailerServiceTests {

    @Autowired
    private RetailerService retailerService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RetailerProfileRepository retailerProfileRepository;

    @Autowired
    private Validator validator;

    @Test
    void addProductToCatalogRejectsDuplicateProductForRetailer() {
        Product product = saveProduct("Duplicate catalog product");
        RetailerProductDto request = validCreateRequest(product.getId());

        retailerService.addProductToCatalog(demoRetailerId(), request);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> retailerService.addProductToCatalog(demoRetailerId(), request));
        assertEquals("Product is already in your catalog", ex.getMessage());
    }

    @Test
    void updateRetailerProductUsesRetailerProductRowId() {
        Product product = saveProduct("Update catalog product");
        RetailerProductResponseDto created = retailerService.addProductToCatalog(demoRetailerId(), validCreateRequest(product.getId()));

        RetailerProductUpdateDto updateDto = new RetailerProductUpdateDto();
        updateDto.setStock(7L);
        updateDto.setRetailPrice(new BigDecimal("249.99"));
        updateDto.setDiscount(new BigDecimal("15.00"));
        updateDto.setIsActive(false);

        RetailerProductResponseDto updated = retailerService.updateRetailerProduct(created.getId(), demoRetailerId(), updateDto);

        assertEquals(created.getId(), updated.getId());
        assertEquals(product.getId(), updated.getProductId());
        assertEquals(7L, updated.getStock());
        assertEquals(new BigDecimal("249.99"), updated.getRetailPrice());
        assertEquals(new BigDecimal("15.00"), updated.getDiscount());
        assertEquals(false, updated.isActive());
    }

    @Test
    void retailerProductValidationRejectsNegativeValues() {
        RetailerProductDto createDto = new RetailerProductDto();
        createDto.setProductId(UUID.randomUUID());
        createDto.setStock(-1L);
        createDto.setRetailPrice(new BigDecimal("-1.00"));
        createDto.setDiscount(new BigDecimal("-2.00"));

        Set<ConstraintViolation<RetailerProductDto>> createViolations = validator.validate(createDto);
        assertTrue(createViolations.stream().anyMatch(violation -> "stock".equals(violation.getPropertyPath().toString())));
        assertTrue(createViolations.stream().anyMatch(violation -> "retailPrice".equals(violation.getPropertyPath().toString())));
        assertTrue(createViolations.stream().anyMatch(violation -> "discount".equals(violation.getPropertyPath().toString())));

        RetailerProductUpdateDto updateDto = new RetailerProductUpdateDto();
        updateDto.setStock(-1L);
        updateDto.setRetailPrice(new BigDecimal("-1.00"));
        updateDto.setDiscount(new BigDecimal("-2.00"));

        Set<ConstraintViolation<RetailerProductUpdateDto>> updateViolations = validator.validate(updateDto);
        assertTrue(updateViolations.stream().anyMatch(violation -> "stock".equals(violation.getPropertyPath().toString())));
        assertTrue(updateViolations.stream().anyMatch(violation -> "retailPrice".equals(violation.getPropertyPath().toString())));
        assertTrue(updateViolations.stream().anyMatch(violation -> "discount".equals(violation.getPropertyPath().toString())));
    }

    private Product saveProduct(String productName) {
        Product product = new Product();
        product.setProductName(productName + " " + UUID.randomUUID());
        product.setCategory("Electronics");
        product.setSubcategory("Audio");
        product.setBrand("QA");
        product.setUnitOfMeasure("piece");
        product.setDescription("Test product");
        return productRepository.save(product);
    }

    private RetailerProductDto validCreateRequest(UUID productId) {
        RetailerProductDto request = new RetailerProductDto();
        request.setProductId(productId);
        request.setStock(5L);
        request.setRetailPrice(new BigDecimal("199.99"));
        request.setDiscount(new BigDecimal("10.00"));
        return request;
    }

    private UUID demoRetailerId() {
        UUID userId = userRepository.findByUsername("retailer_user")
                .orElseThrow(() -> new AssertionError("retailer_user seed data missing"))
                .getId();
        return retailerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AssertionError("retailer profile seed data missing"))
                .getId();
    }
}
