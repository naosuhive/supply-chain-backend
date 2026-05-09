package com.noasuhive.supply_chain_management.service.product;

import com.noasuhive.supply_chain_management.dto.product.ManufacturerProductListingDto;
import com.noasuhive.supply_chain_management.dto.product.ProductCreateDto;
import com.noasuhive.supply_chain_management.dto.product.ProductResponseDto;
import com.noasuhive.supply_chain_management.dto.product.RetailerCatalogProductDto;
import com.noasuhive.supply_chain_management.exceptions.ProductNotFoundException;
import com.noasuhive.supply_chain_management.exceptions.UnauthorizedAccessException;
import com.noasuhive.supply_chain_management.models.ManufacturerProduct;
import com.noasuhive.supply_chain_management.models.ManufacturerProfile;
import com.noasuhive.supply_chain_management.models.Order;
import com.noasuhive.supply_chain_management.models.Product;
import com.noasuhive.supply_chain_management.models.RetailerProduct;
import com.noasuhive.supply_chain_management.models.RetailerProfile;
import com.noasuhive.supply_chain_management.models.User;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProfileRepository;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProductRepository;
import com.noasuhive.supply_chain_management.repositories.OrderItemRepository;
import com.noasuhive.supply_chain_management.repositories.ProductRepository;
import com.noasuhive.supply_chain_management.repositories.RetailerProfileRepository;
import com.noasuhive.supply_chain_management.repositories.RetailerProductRepository;
import com.noasuhive.supply_chain_management.repositories.UserRepository;
import com.noasuhive.supply_chain_management.repositories.projections.ProductSalesMetricsView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ManufacturerProductRepository manufacturerProductRepository;
    private final RetailerProductRepository retailerProductRepository;
    private final ManufacturerProfileRepository manufacturerProfileRepository;
    private final RetailerProfileRepository retailerProfileRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;

    public ProductServiceImpl(ProductRepository productRepository,
                              ManufacturerProductRepository manufacturerProductRepository,
                              RetailerProductRepository retailerProductRepository,
                              ManufacturerProfileRepository manufacturerProfileRepository,
                              RetailerProfileRepository retailerProfileRepository,
                              OrderItemRepository orderItemRepository,
                              UserRepository userRepository) {
        this.productRepository = productRepository;
        this.manufacturerProductRepository = manufacturerProductRepository;
        this.retailerProductRepository = retailerProductRepository;
        this.manufacturerProfileRepository = manufacturerProfileRepository;
        this.retailerProfileRepository = retailerProfileRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ProductResponseDto createProduct(ProductCreateDto dto, UUID manufacturerId) {
        Product product = new Product();
        product.setProductName(dto.getProductName());
        product.setCategory(dto.getCategory());
        product.setSubcategory(dto.getSubcategory());
        product.setBrand(dto.getBrand());
        product.setUnitOfMeasure(dto.getUnitOfMeasure());
        product.setSpecifications(dto.getSpecifications());
        product.setDescription(dto.getDescription());
        product.setCreatedByManufacturer(manufacturerId);
        product = productRepository.save(product);

        ManufacturerProduct manufacturerProduct = new ManufacturerProduct();
        manufacturerProduct.setManufacturerId(manufacturerId);
        manufacturerProduct.setProduct(product);
        manufacturerProduct.setStock(0L);
        manufacturerProduct.setBasePrice(dto.getBasePrice());

        if (dto.getDirectSalePrice() == null) {
            BigDecimal markup = dto.getBasePrice().multiply(BigDecimal.valueOf(0.20));
            manufacturerProduct.setDirectSalePrice(dto.getBasePrice().add(markup));
        } else {
            manufacturerProduct.setDirectSalePrice(dto.getDirectSalePrice());
        }

        manufacturerProductRepository.save(manufacturerProduct);

        return toDto(product, manufacturerProduct.getBasePrice());
    }

    @Override
    public ProductResponseDto getProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID " + id + " not found"));
        ManufacturerProduct manufacturerProduct = manufacturerProductRepository.findByProductId(id).orElse(null);
        return toDto(product, manufacturerProduct != null ? manufacturerProduct.getBasePrice() : null);
    }

    @Override
    public List<ProductResponseDto> listAll() {
        return productRepository.findAll().stream()
                .map(product -> {
                    ManufacturerProduct manufacturerProduct = manufacturerProductRepository.findByProductId(product.getId()).orElse(null);
                    return toDto(product, manufacturerProduct != null ? manufacturerProduct.getBasePrice() : null);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<?> getProductsForCatalogAudience(ProductCatalogAudience audience, String username) {
        return switch (audience) {
            case MANUFACTURER -> listManufacturerProductsForManufacturer(username);
            case RETAILER -> listManufacturerProductsForRetailers();
            case CUSTOMER -> listRetailerProductsForCustomers();
        };
    }

    private List<ManufacturerProductListingDto> listManufacturerProductsForManufacturer(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedAccessException("Authenticated manufacturer user was not found"));

        ManufacturerProfile manufacturerProfile = manufacturerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new UnauthorizedAccessException("Manufacturer profile was not found for user " + username));

        List<ManufacturerProduct> manufacturerProducts = manufacturerProductRepository.findByManufacturerId(manufacturerProfile.getId()).stream()
                .sorted(Comparator.comparing(mp -> safeLower(mp.getProduct().getProductName())))
                .toList();

        Map<UUID, ProductSalesMetricsView> salesMetricsByProductId = buildSalesMetricsByProductId(
                manufacturerProducts.stream().map(mp -> mp.getProduct().getId()).toList(),
                Order.SellerType.MANUFACTURER);

        return manufacturerProducts.stream()
                .map(manufacturerProduct -> toManufacturerListingDto(
                        manufacturerProduct,
                        manufacturerProfile.getCompanyName(),
                        salesMetricsByProductId.get(manufacturerProduct.getProduct().getId())))
                .toList();
    }

    private List<ManufacturerProductListingDto> listManufacturerProductsForRetailers() {
        List<ManufacturerProduct> manufacturerProducts = manufacturerProductRepository.findAllByIsActiveTrue().stream()
                .sorted(Comparator.comparing(mp -> safeLower(mp.getProduct().getProductName())))
                .toList();

        Map<UUID, String> manufacturerNames = manufacturerProfileRepository.findAllById(
                        manufacturerProducts.stream().map(ManufacturerProduct::getManufacturerId).toList())
                .stream()
                .collect(Collectors.toMap(ManufacturerProfile::getId, ManufacturerProfile::getCompanyName));

        Map<UUID, ProductSalesMetricsView> salesMetricsByProductId = buildSalesMetricsByProductId(
                manufacturerProducts.stream().map(mp -> mp.getProduct().getId()).toList(),
                Order.SellerType.MANUFACTURER);

        return manufacturerProducts.stream()
                .map(manufacturerProduct -> toManufacturerListingDto(
                        manufacturerProduct,
                        manufacturerNames.get(manufacturerProduct.getManufacturerId()),
                        salesMetricsByProductId.get(manufacturerProduct.getProduct().getId())))
                .toList();
    }

    private List<RetailerCatalogProductDto> listRetailerProductsForCustomers() {
        List<RetailerProduct> retailerProducts = retailerProductRepository.findAllByIsActiveTrue().stream()
                .sorted(Comparator.comparing(rp -> safeLower(rp.getProduct().getProductName())))
                .toList();

        Map<UUID, String> retailerNames = retailerProfileRepository.findAllById(
                        retailerProducts.stream().map(RetailerProduct::getRetailerId).toList())
                .stream()
                .collect(Collectors.toMap(RetailerProfile::getId, RetailerProfile::getBusinessName));

        return retailerProducts.stream()
                .map(retailerProduct -> toRetailerCatalogDto(
                        retailerProduct,
                        retailerNames.get(retailerProduct.getRetailerId())))
                .toList();
    }

    private Map<UUID, ProductSalesMetricsView> buildSalesMetricsByProductId(Collection<UUID> productIds, Order.SellerType sellerType) {
        if (productIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, ProductSalesMetricsView> metricsByProductId = new HashMap<>();
        for (ProductSalesMetricsView metrics : orderItemRepository.summarizeSalesBySellerTypeAndProductIds(sellerType, productIds)) {
            metricsByProductId.put(metrics.getProductId(), metrics);
        }
        return metricsByProductId;
    }

    private ManufacturerProductListingDto toManufacturerListingDto(
            ManufacturerProduct manufacturerProduct,
            String manufacturerName,
            ProductSalesMetricsView salesMetrics) {
        Product product = manufacturerProduct.getProduct();

        ManufacturerProductListingDto dto = new ManufacturerProductListingDto();
        dto.setManufacturerId(manufacturerProduct.getManufacturerId());
        dto.setManufacturerName(manufacturerName);
        dto.setProductId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setProductDescription(product.getDescription());
        dto.setProductCategory(product.getCategory());
        dto.setProductSubCategory(product.getSubcategory());
        dto.setProductType(null);
        dto.setUnitPrice(manufacturerProduct.getBasePrice());
        dto.setCurrentStock(manufacturerProduct.getStock());
        dto.setLeadTimeDays(null);
        dto.setTotalOrders(salesMetrics != null ? salesMetrics.getTotalOrders() : 0L);
        dto.setTotalUnitsSupplied(salesMetrics != null ? salesMetrics.getTotalUnitsSupplied() : 0L);
        dto.setReturnUnits(null);
        dto.setResponseTimeInHrs(null);
        return dto;
    }

    private RetailerCatalogProductDto toRetailerCatalogDto(RetailerProduct retailerProduct, String retailerName) {
        Product product = retailerProduct.getProduct();

        RetailerCatalogProductDto dto = new RetailerCatalogProductDto();
        dto.setRetailerProductId(retailerProduct.getId());
        dto.setRetailerId(retailerProduct.getRetailerId());
        dto.setRetailerName(retailerName);
        dto.setProductId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setProductDescription(product.getDescription());
        dto.setProductCategory(product.getCategory());
        dto.setProductSubCategory(product.getSubcategory());
        dto.setBrand(product.getBrand());
        dto.setUnitOfMeasure(product.getUnitOfMeasure());
        dto.setRetailPrice(retailerProduct.getRetailPrice());
        dto.setDiscount(retailerProduct.getDiscount());
        dto.setCurrentStock(retailerProduct.getStock());
        dto.setActive(retailerProduct.isActive());
        return dto;
    }

    private ProductResponseDto toDto(Product product, BigDecimal basePrice) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.setId(product.getId());
        dto.setProductName(product.getProductName());
        dto.setCategory(product.getCategory());
        dto.setBrand(product.getBrand());
        dto.setBasePrice(basePrice);
        return dto;
    }

    private String safeLower(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
