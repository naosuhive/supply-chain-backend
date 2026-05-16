package com.noasuhive.supply_chain_management.service.product;

import com.noasuhive.supply_chain_management.dto.product.ProductCreateDto;
import com.noasuhive.supply_chain_management.dto.product.ProductResponseDto;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductResponseDto createProduct(ProductCreateDto dto, UUID manufacturerId);
    ProductResponseDto getProduct(UUID id);
    List<ProductResponseDto> listAll();
    List<?> getProductsForCatalogAudience(ProductCatalogAudience audience, String username);
}
