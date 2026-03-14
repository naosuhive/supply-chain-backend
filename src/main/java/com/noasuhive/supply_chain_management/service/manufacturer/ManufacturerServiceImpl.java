package com.noasuhive.supply_chain_management.service.manufacturer;

import com.noasuhive.supply_chain_management.dto.manufacturer.ManufacturerProductResponseDto;
import com.noasuhive.supply_chain_management.dto.product.ProductCreateDto;
import com.noasuhive.supply_chain_management.dto.product.ProductUpdateDto;
import com.noasuhive.supply_chain_management.exceptions.ProductNotFoundException;
import com.noasuhive.supply_chain_management.exceptions.UnauthorizedAccessException;
import com.noasuhive.supply_chain_management.models.ManufacturerProduct;
import com.noasuhive.supply_chain_management.models.Product;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProductRepository;
import com.noasuhive.supply_chain_management.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ManufacturerServiceImpl implements ManufacturerService {

    private final ProductRepository productRepository;
    private final ManufacturerProductRepository manufacturerProductRepository;

    public ManufacturerServiceImpl(ProductRepository productRepository, ManufacturerProductRepository manufacturerProductRepository) {
        this.productRepository = productRepository;
        this.manufacturerProductRepository = manufacturerProductRepository;
    }

    @Override
    @Transactional
    public ManufacturerProductResponseDto createProduct(UUID manufacturerId, ProductCreateDto dto) {
        // delegate to product service style implementation
        Product p = new Product();
        p.setProductName(dto.getProductName());
        p.setCategory(dto.getCategory());
        p.setSubcategory(dto.getSubcategory());
        p.setBrand(dto.getBrand());
        p.setUnitOfMeasure(dto.getUnitOfMeasure());
        p.setSpecifications(dto.getSpecifications());
        p = productRepository.save(p);

        ManufacturerProduct mp = new ManufacturerProduct();
        mp.setManufacturerId(manufacturerId);
        mp.setProduct(p);
        mp.setStock(dto.getStock() != null ? dto.getStock() : 0L);
        mp.setBasePrice(dto.getBasePrice());
        mp.setDirectSalePrice(dto.getDirectSalePrice());
        manufacturerProductRepository.save(mp);

        ManufacturerProductResponseDto r = new ManufacturerProductResponseDto();
        r.setId(p.getId());
        r.setProductName(p.getProductName());
        r.setCategory(p.getCategory());
        r.setBrand(p.getBrand());
        r.setBasePrice(dto.getBasePrice());
        r.setDirectSalePrice(dto.getDirectSalePrice());
        return r;
    }

    @Override
    public List<ManufacturerProductResponseDto> listProducts(UUID manufacturerId) {
        return manufacturerProductRepository.findByManufacturerId(manufacturerId).stream()
                .map(mp -> {
                    Product p = mp.getProduct();
                    ManufacturerProductResponseDto d = new ManufacturerProductResponseDto();
                    d.setId(p.getId());
                    d.setProductName(p.getProductName());
                    d.setCategory(p.getCategory());
                    d.setBrand(p.getBrand());
                    d.setBasePrice(mp.getBasePrice());
                    d.setDirectSalePrice(mp.getDirectSalePrice());
                    return d;
                }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ManufacturerProductResponseDto updateProduct(UUID productId, UUID manufacturerId, ProductUpdateDto updateDto) {
        ManufacturerProduct mp = manufacturerProductRepository
                .findByManufacturerIdAndProductId(manufacturerId, productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found in your catalog"));

        if (updateDto.getStock() != null) {
            mp.setStock(updateDto.getStock());
        }
        if (updateDto.getBasePrice() != null) {
            mp.setBasePrice(updateDto.getBasePrice());
        }
        if (updateDto.getDirectSalePrice() != null) {
            mp.setDirectSalePrice(updateDto.getDirectSalePrice());
        }
        if (updateDto.getIsActive() != null) {
            mp.setActive(updateDto.getIsActive());
        }

        manufacturerProductRepository.save(mp);

        Product p = mp.getProduct();
        ManufacturerProductResponseDto response = new ManufacturerProductResponseDto();
        response.setId(p.getId());
        response.setProductName(p.getProductName());
        response.setCategory(p.getCategory());
        response.setBrand(p.getBrand());
        response.setBasePrice(mp.getBasePrice());
        response.setDirectSalePrice(mp.getDirectSalePrice());
        return response;
    }
}

