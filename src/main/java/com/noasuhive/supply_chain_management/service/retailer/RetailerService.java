package com.noasuhive.supply_chain_management.service.retailer;

import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductDto;
import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductResponseDto;
import com.noasuhive.supply_chain_management.dto.retailer.RetailerProductUpdateDto;

import java.util.List;
import java.util.UUID;

public interface RetailerService {
    void addProductToCatalog(UUID retailerId, RetailerProductDto dto);
    List<RetailerProductResponseDto> listRetailerProducts(UUID retailerId);
    RetailerProductResponseDto updateRetailerProduct(UUID productId, UUID retailerId, RetailerProductUpdateDto updateDto);
}

