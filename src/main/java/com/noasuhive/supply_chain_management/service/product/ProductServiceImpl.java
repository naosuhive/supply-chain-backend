package com.noasuhive.supply_chain_management.service.product;

import com.noasuhive.supply_chain_management.dto.product.ProductCatalogDto;
import com.noasuhive.supply_chain_management.dto.product.ProductCreateDto;
import com.noasuhive.supply_chain_management.dto.product.ProductResponseDto;
import com.noasuhive.supply_chain_management.exceptions.ProductNotFoundException;
import com.noasuhive.supply_chain_management.models.ManufacturerProduct;
import com.noasuhive.supply_chain_management.models.Product;
import com.noasuhive.supply_chain_management.models.RetailerProduct;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProductRepository;
import com.noasuhive.supply_chain_management.repositories.ProductRepository;
import com.noasuhive.supply_chain_management.repositories.RetailerProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ManufacturerProductRepository manufacturerProductRepository;
    private final RetailerProductRepository retailerProductRepository;

    public ProductServiceImpl(ProductRepository productRepository, 
                        ManufacturerProductRepository manufacturerProductRepository,
                        RetailerProductRepository retailerProductRepository) {
        this.productRepository = productRepository;
        this.manufacturerProductRepository = manufacturerProductRepository;
        this.retailerProductRepository = retailerProductRepository;
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductCreateDto dto, UUID manufacturerId) {
        Product p = new Product();
        p.setProductName(dto.getProductName());
        p.setCategory(dto.getCategory());
        p.setSubcategory(dto.getSubcategory());
        p.setBrand(dto.getBrand());
        p.setUnitOfMeasure(dto.getUnitOfMeasure());
        p.setSpecifications(dto.getSpecifications());
        p.setCreatedByManufacturer(manufacturerId);
        p = productRepository.save(p);

        ManufacturerProduct mp = new ManufacturerProduct();
        mp.setManufacturerId(manufacturerId);
        mp.setProduct(p);
        mp.setStock(0L);
        mp.setBasePrice(dto.getBasePrice());
        
        // If direct sale price not provided, use base price + 20% markup
        if (dto.getDirectSalePrice() == null) {
            BigDecimal markup = dto.getBasePrice().multiply(BigDecimal.valueOf(0.20));
            mp.setDirectSalePrice(dto.getBasePrice().add(markup));
        } else {
            mp.setDirectSalePrice(dto.getDirectSalePrice());
        }
        
        manufacturerProductRepository.save(mp);

        return toDto(p, mp.getBasePrice());
    }

    @Override
    public ProductResponseDto getProduct(UUID id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID " + id + " not found"));
        // find base price from manufacturer product if exists
        ManufacturerProduct mp = manufacturerProductRepository.findByProductId(id).orElse(null);
        return toDto(p, mp != null ? mp.getBasePrice() : null);
    }

    @Override
    public List<ProductResponseDto> listAll() {
        return productRepository.findAll().stream()
                .map(p -> {
                    ManufacturerProduct mp = manufacturerProductRepository.findByProductId(p.getId()).orElse(null);
                    return toDto(p, mp != null ? mp.getBasePrice() : null);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductCatalogDto> getMultiSellerCatalog() {
        List<ProductCatalogDto> catalog = new java.util.ArrayList<>();
        
        // Add manufacturer products
        List<ManufacturerProduct> manufacturerProducts = manufacturerProductRepository.findAllByIsActiveTrue();
        for (ManufacturerProduct mp : manufacturerProducts) {
            ProductCatalogDto dto = new ProductCatalogDto();
            dto.setProductId(mp.getProduct().getId());
            dto.setProductName(mp.getProduct().getProductName());
            dto.setBrand(mp.getProduct().getBrand());
            dto.setCategory(mp.getProduct().getCategory());
            dto.setDescription(mp.getProduct().getDescription());
            dto.setSellerType("MANUFACTURER");
            dto.setSellerId(mp.getManufacturerId().toString());
            dto.setPrice(mp.getDirectSalePrice()); // Use direct sale price for customers
            dto.setStock(mp.getStock());
            dto.setIsActive(mp.isActive());
            catalog.add(dto);
        }
        
        // Add retailer products
        List<RetailerProduct> retailerProducts = retailerProductRepository.findAllByIsActiveTrue();
        for (RetailerProduct rp : retailerProducts) {
            ProductCatalogDto dto = new ProductCatalogDto();
            dto.setProductId(rp.getProduct().getId());
            dto.setProductName(rp.getProduct().getProductName());
            dto.setBrand(rp.getProduct().getBrand());
            dto.setCategory(rp.getProduct().getCategory());
            dto.setDescription(rp.getProduct().getDescription());
            dto.setSellerType("RETAILER");
            dto.setSellerId(rp.getRetailerId().toString());
            dto.setPrice(rp.getRetailPrice());
            dto.setStock(rp.getStock());
            dto.setIsActive(rp.isActive());
            catalog.add(dto);
        }
        
        return catalog;
    }

    private ProductResponseDto toDto(Product p, java.math.BigDecimal basePrice) {
        ProductResponseDto d = new ProductResponseDto();
        d.setId(p.getId());
        d.setProductName(p.getProductName());
        d.setCategory(p.getCategory());
        d.setBrand(p.getBrand());
        d.setBasePrice(basePrice);
        return d;
    }
}
