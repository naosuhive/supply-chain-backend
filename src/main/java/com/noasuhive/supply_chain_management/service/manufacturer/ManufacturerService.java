package com.noasuhive.supply_chain_management.service.manufacturer;

import com.noasuhive.supply_chain_management.dto.manufacturer.ManufacturerProductResponseDto;
import com.noasuhive.supply_chain_management.dto.product.ProductCreateDto;
import com.noasuhive.supply_chain_management.dto.product.ProductUpdateDto;

import java.util.List;
import java.util.UUID;

public interface ManufacturerService {
    ManufacturerProductResponseDto createProduct(UUID manufacturerId, ProductCreateDto dto);
    List<ManufacturerProductResponseDto> listProducts(UUID manufacturerId);
    ManufacturerProductResponseDto updateProduct(UUID productId, UUID manufacturerId, ProductUpdateDto updateDto);
}

