package com.noasuhive.supply_chain_management.service.inventory;

import com.noasuhive.supply_chain_management.dto.inventory.ManufacturerInventoryRequestDto;
import com.noasuhive.supply_chain_management.dto.inventory.ManufacturerInventoryResponseDto;
import com.noasuhive.supply_chain_management.exceptions.InventoryItemNotFoundException;
import com.noasuhive.supply_chain_management.exceptions.UnauthorizedAccessException;
import com.noasuhive.supply_chain_management.models.ManufacturerProduct;
import com.noasuhive.supply_chain_management.models.ManufacturerProfile;
import com.noasuhive.supply_chain_management.models.Product;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProductRepository;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProfileRepository;
import com.noasuhive.supply_chain_management.repositories.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ManufacturerInventoryServiceImpl implements ManufacturerInventoryService {

    private final ManufacturerProductRepository manufacturerProductRepository;
    private final ManufacturerProfileRepository manufacturerProfileRepository;
    private final ProductRepository productRepository;

    public ManufacturerInventoryServiceImpl(
            ManufacturerProductRepository manufacturerProductRepository,
            ManufacturerProfileRepository manufacturerProfileRepository,
            ProductRepository productRepository) {
        this.manufacturerProductRepository = manufacturerProductRepository;
        this.manufacturerProfileRepository = manufacturerProfileRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturerInventoryResponseDto> getAllInventoryItems(UUID manufacturerId) {
        ManufacturerProfile manufacturerProfile = findManufacturerProfile(manufacturerId);
        return manufacturerProductRepository.findByManufacturerId(manufacturerId).stream()
                .filter(ManufacturerProduct::isActive)
                .sorted(Comparator.comparing(mp -> safeLower(mp.getProduct().getProductName())))
                .map(mp -> toResponseDto(mp, manufacturerProfile.getCompanyName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ManufacturerInventoryResponseDto> searchInventoryItems(UUID manufacturerId, String itemName) {
        String normalizedItemName = normalize(itemName);
        if (normalizedItemName == null) {
            throw new IllegalArgumentException("itemName query parameter is required");
        }

        ManufacturerProfile manufacturerProfile = findManufacturerProfile(manufacturerId);
        return manufacturerProductRepository.findByManufacturerId(manufacturerId).stream()
                .filter(ManufacturerProduct::isActive)
                .filter(mp -> containsIgnoreCase(mp.getProduct().getProductName(), normalizedItemName))
                .sorted(Comparator.comparing(mp -> safeLower(mp.getProduct().getProductName())))
                .map(mp -> toResponseDto(mp, manufacturerProfile.getCompanyName()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ManufacturerInventoryResponseDto getInventoryItem(UUID manufacturerId, UUID productId) {
        ManufacturerProfile manufacturerProfile = findManufacturerProfile(manufacturerId);
        ManufacturerProduct manufacturerProduct = findActiveInventoryItem(manufacturerId, productId);
        return toResponseDto(manufacturerProduct, manufacturerProfile.getCompanyName());
    }

    @Override
    @Transactional
    public ManufacturerInventoryResponseDto createInventoryItem(UUID manufacturerId, ManufacturerInventoryRequestDto requestDto) {
        ManufacturerProfile manufacturerProfile = findManufacturerProfile(manufacturerId);

        Product product = new Product();
        applyRequest(product, requestDto, manufacturerId);
        product.setCreatedByManufacturer(manufacturerId);
        product = productRepository.save(product);

        ManufacturerProduct manufacturerProduct = new ManufacturerProduct();
        manufacturerProduct.setManufacturerId(manufacturerId);
        manufacturerProduct.setProduct(product);
        manufacturerProduct.setStock(requestDto.getCurrentStock());
        manufacturerProduct.setBasePrice(requestDto.getBasePrice());
        manufacturerProduct.setDirectSalePrice(requestDto.getDirectSalePrice());
        manufacturerProduct.setActive(requestDto.getActive() == null || requestDto.getActive());
        manufacturerProduct.setUpdatedAt(LocalDateTime.now());
        manufacturerProduct = manufacturerProductRepository.save(manufacturerProduct);

        return toResponseDto(manufacturerProduct, manufacturerProfile.getCompanyName());
    }

    @Override
    @Transactional
    public ManufacturerInventoryResponseDto updateInventoryItem(UUID manufacturerId, UUID productId, ManufacturerInventoryRequestDto requestDto) {
        ManufacturerProfile manufacturerProfile = findManufacturerProfile(manufacturerId);
        ManufacturerProduct manufacturerProduct = findActiveInventoryItem(manufacturerId, productId);
        Product product = manufacturerProduct.getProduct();

        applyRequest(product, requestDto, manufacturerId);
        product.setUpdatedAt(LocalDateTime.now());
        productRepository.save(product);

        manufacturerProduct.setStock(requestDto.getCurrentStock());
        manufacturerProduct.setBasePrice(requestDto.getBasePrice());
        manufacturerProduct.setDirectSalePrice(requestDto.getDirectSalePrice());
        if (requestDto.getActive() != null) {
            manufacturerProduct.setActive(requestDto.getActive());
        }
        manufacturerProduct.setUpdatedAt(LocalDateTime.now());
        manufacturerProduct = manufacturerProductRepository.save(manufacturerProduct);

        return toResponseDto(manufacturerProduct, manufacturerProfile.getCompanyName());
    }

    @Override
    @Transactional
    public void deleteInventoryItem(UUID manufacturerId, UUID productId) {
        ManufacturerProduct manufacturerProduct = findActiveInventoryItem(manufacturerId, productId);
        manufacturerProduct.setActive(false);
        manufacturerProduct.setUpdatedAt(LocalDateTime.now());
        manufacturerProductRepository.save(manufacturerProduct);
    }

    private ManufacturerProfile findManufacturerProfile(UUID manufacturerId) {
        return manufacturerProfileRepository.findById(manufacturerId)
                .orElseThrow(() -> new UnauthorizedAccessException("Manufacturer profile was not found for the authenticated user"));
    }

    private ManufacturerProduct findActiveInventoryItem(UUID manufacturerId, UUID productId) {
        ManufacturerProduct manufacturerProduct = manufacturerProductRepository.findByManufacturerIdAndProductId(manufacturerId, productId)
                .orElseThrow(() -> new InventoryItemNotFoundException(
                        "Inventory item with productId " + productId + " was not found for manufacturer " + manufacturerId));

        if (!manufacturerProduct.isActive()) {
            throw new InventoryItemNotFoundException(
                    "Inventory item with productId " + productId + " was not found for manufacturer " + manufacturerId);
        }

        return manufacturerProduct;
    }

    private void applyRequest(Product product, ManufacturerInventoryRequestDto requestDto, UUID manufacturerId) {
        product.setProductName(normalize(requestDto.getProductName()));
        product.setDescription(normalize(requestDto.getProductDescription()));
        product.setCategory(normalize(requestDto.getCategory()));
        product.setSubcategory(normalize(requestDto.getSubCategory()));
        product.setBrand(normalize(requestDto.getBrand()));
        product.setUnitOfMeasure(normalize(requestDto.getUnitOfMeasure()));
        product.setSpecifications(normalize(requestDto.getSpecifications()));
        product.setCreatedByManufacturer(manufacturerId);
    }

    private ManufacturerInventoryResponseDto toResponseDto(ManufacturerProduct manufacturerProduct, String manufacturerName) {
        Product product = manufacturerProduct.getProduct();

        ManufacturerInventoryResponseDto responseDto = new ManufacturerInventoryResponseDto();
        responseDto.setManufacturerId(manufacturerProduct.getManufacturerId());
        responseDto.setManufacturerName(manufacturerName);
        responseDto.setProductId(product.getId());
        responseDto.setProductName(product.getProductName());
        responseDto.setProductDescription(product.getDescription());
        responseDto.setCategory(product.getCategory());
        responseDto.setSubCategory(product.getSubcategory());
        responseDto.setBrand(product.getBrand());
        responseDto.setUnitOfMeasure(product.getUnitOfMeasure());
        responseDto.setCurrentStock(manufacturerProduct.getStock());
        responseDto.setBasePrice(manufacturerProduct.getBasePrice());
        responseDto.setDirectSalePrice(manufacturerProduct.getDirectSalePrice());
        responseDto.setActive(manufacturerProduct.isActive());
        return responseDto;
    }

    private boolean containsIgnoreCase(String value, String searchText) {
        return value != null && value.toLowerCase().contains(searchText.toLowerCase());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
