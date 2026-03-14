package com.noasuhive.supply_chain_management.service.product;

import com.noasuhive.supply_chain_management.dto.product.ProductCatalogDto;
import com.noasuhive.supply_chain_management.dto.product.ProductCreateDto;
import com.noasuhive.supply_chain_management.dto.product.ProductResponseDto;
import java.util.UUID;
import java.util.List;

public interface ProductService {
    ProductResponseDto createProduct(ProductCreateDto dto, UUID manufacturerId);
    ProductResponseDto getProduct(UUID id);
    List<ProductResponseDto> listAll();
    List<ProductCatalogDto> getMultiSellerCatalog();
}

