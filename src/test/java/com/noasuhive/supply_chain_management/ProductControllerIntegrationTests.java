package com.noasuhive.supply_chain_management;

import com.noasuhive.supply_chain_management.models.ManufacturerProduct;
import com.noasuhive.supply_chain_management.models.ManufacturerProfile;
import com.noasuhive.supply_chain_management.models.Order;
import com.noasuhive.supply_chain_management.models.OrderItem;
import com.noasuhive.supply_chain_management.models.Product;
import com.noasuhive.supply_chain_management.models.RetailerProduct;
import com.noasuhive.supply_chain_management.models.RetailerProfile;
import com.noasuhive.supply_chain_management.models.Role;
import com.noasuhive.supply_chain_management.models.User;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProductRepository;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProfileRepository;
import com.noasuhive.supply_chain_management.repositories.OrderItemRepository;
import com.noasuhive.supply_chain_management.repositories.OrderRepository;
import com.noasuhive.supply_chain_management.repositories.ProductRepository;
import com.noasuhive.supply_chain_management.repositories.RetailerProductRepository;
import com.noasuhive.supply_chain_management.repositories.RetailerProfileRepository;
import com.noasuhive.supply_chain_management.repositories.RoleRepository;
import com.noasuhive.supply_chain_management.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ProductControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ManufacturerProductRepository manufacturerProductRepository;

    @Autowired
    private RetailerProductRepository retailerProductRepository;

    @Autowired
    private ManufacturerProfileRepository manufacturerProfileRepository;

    @Autowired
    private RetailerProfileRepository retailerProfileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void retailerRoleGetsManufacturerProductsFromManufacturerInventory() throws Exception {
        ManufacturerProfile manufacturerProfile = demoManufacturerProfile();
        Product product = saveProduct("Retailer listing manufacturer");

        ManufacturerProduct manufacturerProduct = new ManufacturerProduct();
        manufacturerProduct.setManufacturerId(manufacturerProfile.getId());
        manufacturerProduct.setProduct(product);
        manufacturerProduct.setStock(40L);
        manufacturerProduct.setBasePrice(new BigDecimal("125.50"));
        manufacturerProduct.setDirectSalePrice(new BigDecimal("150.60"));
        manufacturerProductRepository.save(manufacturerProduct);

        saveManufacturerOrder(manufacturerProfile.getId(), product.getId(), 7L);

        JsonNode rows = fetchProducts(login("retailer_user", "retailerpass"));
        JsonNode row = findRowByProductId(rows, product.getId());

        assertNotNull(row, "Expected manufacturer product in retailer listing");
        assertEquals(manufacturerProfile.getId().toString(), row.path("manufacturerId").asText());
        assertEquals("ACME Manufacturing Co.", row.path("manufacturerName").asText());
        assertEquals(product.getProductName(), row.path("productName").asText());
        assertEquals(product.getDescription(), row.path("productDescription").asText());
        assertEquals(product.getCategory(), row.path("productCategory").asText());
        assertEquals(product.getSubcategory(), row.path("productSubCategory").asText());
        assertEquals(125.50d, row.path("unitPrice").asDouble(), 0.0001d);
        assertEquals(40L, row.path("currentStock").asLong());
        assertEquals(1L, row.path("totalOrders").asLong());
        assertEquals(7L, row.path("totalUnitsSupplied").asLong());
        assertTrue(row.has("leadTimeDays") && row.get("leadTimeDays").isNull());
        assertTrue(row.has("responseTimeInHrs") && row.get("responseTimeInHrs").isNull());
    }

    @Test
    void customerRoleGetsRetailerProductsFromRetailerCatalog() throws Exception {
        RetailerProfile retailerProfile = demoRetailerProfile();
        Product product = saveProduct("Customer listing retailer");

        RetailerProduct retailerProduct = new RetailerProduct();
        retailerProduct.setRetailerId(retailerProfile.getId());
        retailerProduct.setProduct(product);
        retailerProduct.setStock(18L);
        retailerProduct.setRetailPrice(new BigDecimal("89.99"));
        retailerProduct.setDiscount(new BigDecimal("5.00"));
        retailerProductRepository.save(retailerProduct);

        JsonNode rows = fetchProducts(login("customer_user", "securePassword123"));
        JsonNode row = findRowByProductId(rows, product.getId());

        assertNotNull(row, "Expected retailer product in customer listing");
        assertEquals(retailerProduct.getId().toString(), row.path("retailerProductId").asText());
        assertEquals(retailerProfile.getId().toString(), row.path("retailerId").asText());
        assertEquals("Best Retail Store", row.path("retailerName").asText());
        assertEquals(product.getProductName(), row.path("productName").asText());
        assertEquals(product.getDescription(), row.path("productDescription").asText());
        assertEquals(product.getCategory(), row.path("productCategory").asText());
        assertEquals(product.getSubcategory(), row.path("productSubCategory").asText());
        assertEquals(product.getBrand(), row.path("brand").asText());
        assertEquals(product.getUnitOfMeasure(), row.path("unitOfMeasure").asText());
        assertEquals(89.99d, row.path("retailPrice").asDouble(), 0.0001d);
        assertEquals(5.00d, row.path("discount").asDouble(), 0.0001d);
        assertEquals(18L, row.path("currentStock").asLong());
        assertTrue(row.path("active").asBoolean());
    }

    @Test
    void roleUserAlsoGetsRetailerProducts() throws Exception {
        ensureRoleUserExists();
        Product product = saveProduct("Role user retailer");

        RetailerProduct retailerProduct = new RetailerProduct();
        retailerProduct.setRetailerId(demoRetailerProfile().getId());
        retailerProduct.setProduct(product);
        retailerProduct.setStock(9L);
        retailerProduct.setRetailPrice(new BigDecimal("49.99"));
        retailerProduct.setDiscount(BigDecimal.ZERO);
        retailerProductRepository.save(retailerProduct);

        JsonNode rows = fetchProducts(login("grid_user", "gridpass123"));
        JsonNode row = findRowByProductId(rows, product.getId());

        assertNotNull(row, "Expected retailer product in ROLE_USER listing");
        assertEquals("Best Retail Store", row.path("retailerName").asText());
        assertEquals(49.99d, row.path("retailPrice").asDouble(), 0.0001d);
    }

    @Test
    void manufacturerRoleGetsManufacturerProductsFromManufacturerInventory() throws Exception {
        ManufacturerProfile manufacturerProfile = demoManufacturerProfile();
        Product product = saveProduct("Manufacturer own listing");

        ManufacturerProduct manufacturerProduct = new ManufacturerProduct();
        manufacturerProduct.setManufacturerId(manufacturerProfile.getId());
        manufacturerProduct.setProduct(product);
        manufacturerProduct.setStock(12L);
        manufacturerProduct.setBasePrice(new BigDecimal("210.00"));
        manufacturerProduct.setDirectSalePrice(new BigDecimal("260.00"));
        manufacturerProductRepository.save(manufacturerProduct);

        JsonNode rows = fetchProducts(login("manufacturer_user", "manufacturerpass"));
        JsonNode row = findRowByProductId(rows, product.getId());

        assertNotNull(row, "Expected manufacturer product in manufacturer listing");
        assertEquals(manufacturerProfile.getId().toString(), row.path("manufacturerId").asText());
        assertEquals("ACME Manufacturing Co.", row.path("manufacturerName").asText());
        assertEquals(product.getProductName(), row.path("productName").asText());
        assertEquals(210.00d, row.path("unitPrice").asDouble(), 0.0001d);
        assertEquals(12L, row.path("currentStock").asLong());
    }

    private Product saveProduct(String prefix) {
        Product product = new Product();
        product.setProductName(prefix + " " + UUID.randomUUID());
        product.setDescription("Grid-ready listing product");
        product.setCategory("Plumbing");
        product.setSubcategory("Valves");
        product.setBrand("FlowPro");
        product.setUnitOfMeasure("piece");
        product.setSpecifications("Commercial");
        return productRepository.save(product);
    }

    private void saveManufacturerOrder(UUID manufacturerId, UUID productId, Long quantity) {
        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID());
        order.setCustomerId(UUID.randomUUID());
        order.setSellerId(manufacturerId);
        order.setSellerType(Order.SellerType.MANUFACTURER);
        order.setTotalAmount(new BigDecimal("878.50"));
        order = orderRepository.save(order);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setProductId(productId);
        orderItem.setQuantity(quantity);
        orderItem.setUnitPrice(new BigDecimal("125.50"));
        orderItem.setTotalPrice(new BigDecimal("878.50"));
        orderItemRepository.save(orderItem);
    }

    private ManufacturerProfile demoManufacturerProfile() {
        UUID userId = userRepository.findByUsername("manufacturer_user")
                .orElseThrow(() -> new AssertionError("manufacturer_user missing"))
                .getId();
        return manufacturerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AssertionError("manufacturer profile missing"));
    }

    private RetailerProfile demoRetailerProfile() {
        UUID userId = userRepository.findByUsername("retailer_user")
                .orElseThrow(() -> new AssertionError("retailer_user missing"))
                .getId();
        return retailerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AssertionError("retailer profile missing"));
    }

    private void ensureRoleUserExists() {
        if (userRepository.findByUsername("grid_user").isPresent()) {
            return;
        }

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        User user = new User();
        user.setUsername("grid_user");
        user.setEmail("grid_user@example.com");
        user.setPhoneNumber("+1234567999");
        user.setPassword(passwordEncoder.encode("gridpass123"));
        user.setRoles(Set.of(roleUser));
        userRepository.save(user);
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

    private JsonNode fetchProducts(String token) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
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
