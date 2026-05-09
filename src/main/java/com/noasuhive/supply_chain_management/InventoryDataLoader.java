package com.noasuhive.supply_chain_management;

import com.noasuhive.supply_chain_management.models.InventoryItem;
import com.noasuhive.supply_chain_management.repositories.InventoryRepository;
import com.noasuhive.supply_chain_management.repositories.RetailerProfileRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class InventoryDataLoader implements CommandLineRunner {

    private static final String INVENTORY_RESOURCE = "inventory-data.csv";

    private final InventoryRepository inventoryRepository;
    private final RetailerProfileRepository retailerProfileRepository;

    public InventoryDataLoader(
            InventoryRepository inventoryRepository,
            RetailerProfileRepository retailerProfileRepository) {
        this.inventoryRepository = inventoryRepository;
        this.retailerProfileRepository = retailerProfileRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        UUID retailerId = retailerProfileRepository.findFirstByOrderByCreatedAtAsc()
                .orElseThrow(() -> new IllegalStateException("Cannot load inventory data because no retailer profile exists"))
                .getId();
        inventoryRepository.deleteAllInBatch();
        inventoryRepository.saveAll(loadInventoryItems(retailerId));
    }

    private List<InventoryItem> loadInventoryItems(UUID retailerId) {
        List<InventoryItem> items = new ArrayList<>();
        ClassPathResource resource = new ClassPathResource(INVENTORY_RESOURCE);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine(); // header
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                items.add(parseRow(line, retailerId));
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load inventory data from " + INVENTORY_RESOURCE, ex);
        }

        return items;
    }

    private InventoryItem parseRow(String line, UUID retailerId) {
        String[] values = line.split(",", -1);
        if (values.length != 14) {
            throw new IllegalStateException("Inventory data row has " + values.length + " columns instead of 14: " + line);
        }

        InventoryItem item = new InventoryItem();
        item.setItemId(parseInteger(values[0]));
        item.setRetailerId(retailerId);
        item.setItemCode(parseText(values[1]));
        item.setItemName(parseText(values[2]));
        item.setItemDescription(parseText(values[3]));
        item.setItemType(parseText(values[4]));
        item.setItemSize(parseText(values[5]));
        item.setCategory(parseText(values[6]));
        item.setSubCategory(parseText(values[7]));
        item.setSupplierName(parseText(values[8]));
        item.setUnitMeasurementType(parseText(values[9]));
        item.setUnitName(parseText(values[10]));
        item.setQuantity(parseLong(values[11]));
        item.setUnitPrice(parseDecimal(values[12]));
        item.setDiscount(parseDecimal(values[13]));
        return item;
    }

    private Integer parseInteger(String value) {
        String text = parseText(value);
        return text == null ? null : Integer.valueOf(text);
    }

    private Long parseLong(String value) {
        String text = parseText(value);
        return text == null ? null : Long.valueOf(text);
    }

    private BigDecimal parseDecimal(String value) {
        String text = parseText(value);
        return text == null ? null : new BigDecimal(text);
    }

    private String parseText(String value) {
        String trimmed = value == null ? null : value.trim();
        return trimmed == null || trimmed.isEmpty() ? null : trimmed;
    }
}
