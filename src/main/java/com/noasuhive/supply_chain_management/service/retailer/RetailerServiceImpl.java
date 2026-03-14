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
    public void addProductToCatalog(UUID retailerId, RetailerProductDto dto) {
        Product p = productRepository.findById(dto.getProductId()).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        RetailerProduct rp = new RetailerProduct();
        rp.setRetailerId(retailerId);
        rp.setProduct(p);
        rp.setStock(dto.getStock());
        rp.setRetailPrice(dto.getRetailPrice());
        rp.setDiscount(dto.getDiscount());
        retailerProductRepository.save(rp);
    }

    @Override
    public List<RetailerProductResponseDto> listRetailerProducts(UUID retailerId) {
        return retailerProductRepository.findByRetailerId(retailerId).stream().map(rp -> {
            Product p = rp.getProduct();
            RetailerProductResponseDto d = new RetailerProductResponseDto();
            d.setId(rp.getId());
            d.setProductId(p.getId());
            d.setProductName(p.getProductName());
            d.setCategory(p.getCategory());
            d.setBrand(p.getBrand());
            d.setRetailPrice(rp.getRetailPrice());
            d.setDiscount(rp.getDiscount());
            d.setStock(rp.getStock());
            d.setActive(rp.isActive());
            return d;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RetailerProductResponseDto updateRetailerProduct(UUID productId, UUID retailerId, RetailerProductUpdateDto updateDto) {
        RetailerProduct rp = retailerProductRepository
                .findByRetailerIdAndProductId(retailerId, productId)
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

        retailerProductRepository.save(rp);

        Product p = rp.getProduct();
        RetailerProductResponseDto response = new RetailerProductResponseDto();
        response.setId(rp.getId());
        response.setProductId(p.getId());
        response.setProductName(p.getProductName());
        response.setCategory(p.getCategory());
        response.setBrand(p.getBrand());
        response.setRetailPrice(rp.getRetailPrice());
        response.setDiscount(rp.getDiscount());
        response.setStock(rp.getStock());
        response.setActive(rp.isActive());
        return response;
    }
}

