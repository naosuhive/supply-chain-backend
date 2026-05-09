package com.noasuhive.supply_chain_management.service.retailer;

import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductDto;
import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductResponseDto;
import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductUpdateDto;
import com.noasuhive.supply_chain_management.exceptions.ProductNotFoundException;
import com.noasuhive.supply_chain_management.models.Product;
import com.noasuhive.supply_chain_management.models.RetailerProduct;
import com.noasuhive.supply_chain_management.repositories.ProductRepository;
import com.noasuhive.supply_chain_management.repositories.RetailerProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class RetailerServiceImpl implements RetailerService {

    private final RetailerProductRepository retailerProductRepository;
    private final ProductRepository productRepository;

    public RetailerServiceImpl(RetailerProductRepository retailerProductRepository, ProductRepository productRepository) {
        this.retailerProductRepository = retailerProductRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public RetailerProductResponseDto addProductToCatalog(UUID retailerId, RetailerProductDto dto) {
        Product p = productRepository.findById(dto.getProductId()).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (retailerProductRepository.existsByRetailerIdAndProductId(retailerId, dto.getProductId())) {
            throw new IllegalArgumentException("Product is already in your catalog");
        }

        RetailerProduct rp = new RetailerProduct();
        rp.setRetailerId(retailerId);
        rp.setProduct(p);
        rp.setStock(dto.getStock());
        rp.setRetailPrice(dto.getRetailPrice());
        rp.setDiscount(dto.getDiscount());
        return toResponseDto(retailerProductRepository.save(rp));
    }

    @Override
    public List<RetailerProductResponseDto> listRetailerProducts(UUID retailerId) {
        return retailerProductRepository.findByRetailerId(retailerId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RetailerProductResponseDto updateRetailerProduct(UUID retailerProductId, UUID retailerId, RetailerProductUpdateDto updateDto) {
        RetailerProduct rp = retailerProductRepository
                .findByIdAndRetailerId(retailerProductId, retailerId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found in your catalog"));

        if (updateDto.getStock() != null) {
            rp.setStock(updateDto.getStock());
        }
        if (updateDto.getRetailPrice() != null) {
            rp.setRetailPrice(updateDto.getRetailPrice());
        }
        if (updateDto.getDiscount() != null) {
            rp.setDiscount(updateDto.getDiscount());
        }
        if (updateDto.getIsActive() != null) {
            rp.setActive(updateDto.getIsActive());
        }

        return toResponseDto(retailerProductRepository.save(rp));
    }

    private RetailerProductResponseDto toResponseDto(RetailerProduct retailerProduct) {
        Product product = retailerProduct.getProduct();
        RetailerProductResponseDto response = new RetailerProductResponseDto();
        response.setId(retailerProduct.getId());
        response.setProductId(product.getId());
        response.setProductName(product.getProductName());
        response.setCategory(product.getCategory());
        response.setBrand(product.getBrand());
        response.setRetailPrice(retailerProduct.getRetailPrice());
        response.setDiscount(retailerProduct.getDiscount());
        response.setStock(retailerProduct.getStock());
        response.setActive(retailerProduct.isActive());
        return response;
    }
}
