package com.noasuhive.supply_chain_management;

import com.noasuhive.supply_chain_management.models.ManufacturerProduct;
import com.noasuhive.supply_chain_management.models.ManufacturerProfile;
import com.noasuhive.supply_chain_management.models.Product;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProductRepository;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProfileRepository;
import com.noasuhive.supply_chain_management.repositories.ProductRepository;
import com.noasuhive.supply_chain_management.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class InventoryControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ManufacturerProductRepository manufacturerProductRepository;

    @Autowired
    private ManufacturerProfileRepository manufacturerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void manufacturerCanListOwnInventoryFromManufacturerProducts() throws Exception {
        ManufacturerProfile manufacturerProfile = demoManufacturerProfile();
        Product product = saveProduct("Manufacturer inventory");

        ManufacturerProduct manufacturerProduct = new ManufacturerProduct();
        manufacturerProduct.setManufacturerId(manufacturerProfile.getId());
        manufacturerProduct.setProduct(product);
        manufacturerProduct.setStock(27L);
        manufacturerProduct.setBasePrice(new BigDecimal("140.00"));
        manufacturerProduct.setDirectSalePrice(new BigDecimal("175.00"));
        manufacturerProductRepository.save(manufacturerProduct);

        JsonNode rows = fetchInventory(login("manufacturer_user", "manufacturerpass"));
        JsonNode row = findRowByProductId(rows, product.getId());

        assertNotNull(row, "Expected manufacturer inventory row");
        assertEquals(manufacturerProfile.getId().toString(), row.path("manufacturerId").asText());
        assertEquals("ACME Manufacturing Co.", row.path("manufacturerName").asText());
        assertEquals(product.getProductName(), row.path("productName").asText());
        assertEquals(27L, row.path("currentStock").asLong());
        assertEquals(140.00d, row.path("basePrice").asDouble(), 0.0001d);
        assertEquals(175.00d, row.path("directSalePrice").asDouble(), 0.0001d);
    }

    @Test
    void retailerCanStillListRetailerInventory() throws Exception {
        JsonNode rows = fetchInventory(login("retailer_user", "retailerpass"));

        assertTrue(rows.isArray());
        assertTrue(rows.size() > 0);
        JsonNode firstRow = rows.get(0);
        assertTrue(firstRow.has("itemId"));
        assertTrue(firstRow.has("itemCode"));
        assertTrue(firstRow.has("itemName"));
    }

    @Test
    void manufacturerCanManageInventoryCrudThroughRoleBasedEndpoint() throws Exception {
        ManufacturerProfile manufacturerProfile = demoManufacturerProfile();
        String token = login("manufacturer_user", "manufacturerpass");

        JsonNode created = createInventory(token, """
                {
                  "active": true,
                  "basePrice": 310.50,
                  "brand": "VoltWorks",
                  "category": "Electrical",
                  "currentStock": 11,
                  "directSalePrice": 349.99,
                  "manufacturerId": "%s",
                  "manufacturerName": "%s",
                  "productDescription": "Created through /api/inventory",
                  "productId": null,
                  "productName": "Manufacturer CRUD %s",
                  "subCategory": "Switches",
                  "unitOfMeasure": "box",
                  "specifications": "IP65"
                }
                """.formatted(
                manufacturerProfile.getId(),
                manufacturerProfile.getCompanyName(),
                UUID.randomUUID()));

        String productId = created.path("productId").asText();
        assertTrue(!productId.isBlank());
        assertEquals(manufacturerProfile.getId().toString(), created.path("manufacturerId").asText());
        assertEquals(manufacturerProfile.getCompanyName(), created.path("manufacturerName").asText());
        assertEquals("Created through /api/inventory", created.path("productDescription").asText());
        assertEquals(11L, created.path("currentStock").asLong());

        JsonNode fetched = getInventoryItem(token, productId, status().isOk());
        assertEquals(productId, fetched.path("productId").asText());
        assertEquals("VoltWorks", fetched.path("brand").asText());

        JsonNode updated = updateInventory(token, productId, """
                {
                  "active": true,
                  "basePrice": 299.00,
                  "brand": "VoltWorks Pro",
                  "category": "Electrical",
                  "currentStock": 19,
                  "directSalePrice": 332.10,
                  "manufacturerId": "%s",
                  "manufacturerName": "%s",
                  "productDescription": "Updated through /api/inventory",
                  "productId": "%s",
                  "productName": "Manufacturer CRUD Updated %s",
                  "subCategory": "Panels",
                  "unitOfMeasure": "unit",
                  "specifications": "Outdoor rated"
                }
                """.formatted(
                manufacturerProfile.getId(),
                manufacturerProfile.getCompanyName(),
                productId,
                UUID.randomUUID()));

        assertEquals("Updated through /api/inventory", updated.path("productDescription").asText());
        assertEquals("Panels", updated.path("subCategory").asText());
        assertEquals(19L, updated.path("currentStock").asLong());
        assertEquals(299.00d, updated.path("basePrice").asDouble(), 0.0001d);

        deleteInventory(token, productId);
        getInventoryItem(token, productId, status().isNotFound());
    }

    @Test
    void manufacturerGetsHelpfulBadRequestWhenUsingManufacturerIdAsPath() throws Exception {
        ManufacturerProfile manufacturerProfile = demoManufacturerProfile();
        String token = login("manufacturer_user", "manufacturerpass");

        JsonNode response = getInventoryItem(token, manufacturerProfile.getId().toString(), status().isBadRequest());
        assertEquals("Use productId in /api/inventory/{itemId} for manufacturer requests. The supplied value matches the authenticated manufacturerId.",
                response.path("message").asText());
    }

    @Test
    void retailerCanManageInventoryCrudThroughRoleBasedEndpoint() throws Exception {
        String token = login("retailer_user", "retailerpass");
        int itemId = ThreadLocalRandom.current().nextInt(100000, 999999);
        String itemCode = "RET-" + UUID.randomUUID();

        JsonNode created = createInventory(token, """
                {
                  "itemId": %d,
                  "itemCode": "%s",
                  "itemName": "Retailer CRUD Item",
                  "itemDescription": "Created through /api/inventory",
                  "itemType": "Groceries",
                  "itemSize": "Large",
                  "category": "Food",
                  "subCategory": "Snacks",
                  "supplierName": "Distributor Hub",
                  "unitMeasurementType": "Weight",
                  "unitName": "kg",
                  "quantity": 45,
                  "unitPrice": 72.25,
                  "discount": 4.50
                }
                """.formatted(itemId, itemCode));

        assertEquals(itemId, created.path("itemId").asInt());
        assertEquals(itemCode, created.path("itemCode").asText());

        JsonNode fetched = getInventoryItem(token, String.valueOf(itemId), status().isOk());
        assertEquals("Retailer CRUD Item", fetched.path("itemName").asText());

        JsonNode updated = updateInventory(token, String.valueOf(itemId), """
                {
                  "itemId": %d,
                  "itemCode": "%s",
                  "itemName": "Retailer CRUD Item Updated",
                  "itemDescription": "Updated through /api/inventory",
                  "itemType": "Groceries",
                  "itemSize": "Medium",
                  "category": "Food",
                  "subCategory": "Healthy Snacks",
                  "supplierName": "Distributor Hub",
                  "unitMeasurementType": "Weight",
                  "unitName": "kg",
                  "quantity": 63,
                  "unitPrice": 68.10,
                  "discount": 2.00
                }
                """.formatted(itemId, itemCode));

        assertEquals("Retailer CRUD Item Updated", updated.path("itemName").asText());
        assertEquals(63L, updated.path("quantity").asLong());
        assertEquals(68.10d, updated.path("unitPrice").asDouble(), 0.0001d);

        deleteInventory(token, String.valueOf(itemId));
        getInventoryItem(token, String.valueOf(itemId), status().isNotFound());
    }

    private ManufacturerProfile demoManufacturerProfile() {
        UUID userId = userRepository.findByUsername("manufacturer_user")
                .orElseThrow(() -> new AssertionError("manufacturer_user missing"))
                .getId();
        return manufacturerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AssertionError("manufacturer profile missing"));
    }

    private Product saveProduct(String prefix) {
        Product product = new Product();
        product.setProductName(prefix + " " + UUID.randomUUID());
        product.setDescription("Manufacturer inventory grid row");
        product.setCategory("Plumbing");
        product.setSubcategory("Fittings");
        product.setBrand("PipeWorks");
        product.setUnitOfMeasure("piece");
        product.setSpecifications("Industrial");
        return productRepository.save(product);
    }

    private String login(String usernameOrEmail, String password) throws Exception {
        String body = """
                {
                  "usernameOrEmail": "%s",
                  "password": "%s"
                }
                """.formatted(usernameOrEmail, password);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.path("token").asText();
    }

    private JsonNode fetchInventory(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/inventory")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode createInventory(String token, String body) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/inventory")
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode updateInventory(String token, String itemId, String body) throws Exception {
        MvcResult result = mockMvc.perform(put("/api/inventory/{itemId}", itemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private JsonNode getInventoryItem(String token, String itemId, org.springframework.test.web.servlet.ResultMatcher expectedStatus) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/inventory/{itemId}", itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(expectedStatus)
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void deleteInventory(String token, String itemId) throws Exception {
        mockMvc.perform(delete("/api/inventory/{itemId}", itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    private JsonNode findRowByProductId(JsonNode rows, UUID productId) {
        for (JsonNode row : rows) {
            if (productId.toString().equals(row.path("productId").asText())) {
                return row;
            }
        }
        return null;
    }
}
