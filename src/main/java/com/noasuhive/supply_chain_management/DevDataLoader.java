package com.noasuhive.supply_chain_management;

import com.noasuhive.supply_chain_management.models.Address;
import com.noasuhive.supply_chain_management.models.ManufacturerProfile;
import com.noasuhive.supply_chain_management.models.Role;
import com.noasuhive.supply_chain_management.models.RetailerProfile;
import com.noasuhive.supply_chain_management.models.User;
import com.noasuhive.supply_chain_management.repositories.AddressRepository;
import com.noasuhive.supply_chain_management.repositories.ManufacturerProfileRepository;
import com.noasuhive.supply_chain_management.repositories.RetailerProfileRepository;
import com.noasuhive.supply_chain_management.repositories.RoleRepository;
import com.noasuhive.supply_chain_management.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Order(1)
public class DevDataLoader implements CommandLineRunner {
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String MANUFACTURER_ROLE = "ROLE_MANUFACTURER";
    private static final String RETAILER_ROLE = "ROLE_RETAILER";
    private static final String CUSTOMER_ROLE = "ROLE_CUSTOMER";

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ManufacturerProfileRepository manufacturerProfileRepository;
    private final RetailerProfileRepository retailerProfileRepository;
    private final AddressRepository addressRepository;

    public DevDataLoader(
            RoleRepository roleRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            ManufacturerProfileRepository manufacturerProfileRepository,
            RetailerProfileRepository retailerProfileRepository,
            AddressRepository addressRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.manufacturerProfileRepository = manufacturerProfileRepository;
        this.retailerProfileRepository = retailerProfileRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    public void run(String... args) {
        Role adminRole = getOrCreateRole(ADMIN_ROLE);
        Role manufacturerRole = getOrCreateRole(MANUFACTURER_ROLE);
        Role retailerRole = getOrCreateRole(RETAILER_ROLE);
        Role customerRole = getOrCreateRole(CUSTOMER_ROLE);

        User admin = getOrCreateUser("admin", "admin@example.com", "+1234567890", "adminpass", adminRole);
        User manufacturer = getOrCreateUser("manufacturer_user", "manufacturer@example.com", "+1234567891", "manufacturerpass", manufacturerRole);
        User retailer = getOrCreateUser("retailer_user", "retailer@example.com", "+1234567892", "retailerpass", retailerRole);
        User customer = getOrCreateUser("customer_user", "customer@example.com", "+1234567893", "securePassword123", customerRole);

        ensureManufacturerProfile(manufacturer, "ACME Manufacturing Co.", "GST123456789", "Electronics");
        ensureRetailerProfile(retailer, "Best Retail Store", "GST987654321", "Electronics");

        ensureAddress(manufacturer, "BUSINESS", "456 Industrial Ave", "Detroit", "MI", "USA", "48201");
        ensureAddress(retailer, "BUSINESS", "789 Shopping Blvd", "Los Angeles", "CA", "USA", "90001");
        ensureAddress(customer, "SHIPPING", "123 Main St", "New York", "NY", "USA", "10001");
        ensureAddress(admin, "SHIPPING", "1 Admin Plaza", "New York", "NY", "USA", "10002");
    }

    private Role getOrCreateRole(String roleName) {
        return roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
    }

    private User getOrCreateUser(String username, String email, String phoneNumber, String password, Role role) {
        return userRepository.findByUsername(username).orElseGet(() -> {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            user.setPassword(passwordEncoder.encode(password));
            user.setRoles(Set.of(role));
            return userRepository.save(user);
        });
    }

    private void ensureManufacturerProfile(User user, String companyName, String gstNumber, String businessType) {
        manufacturerProfileRepository.findByUserId(user.getId()).orElseGet(() -> {
            ManufacturerProfile profile = new ManufacturerProfile();
            profile.setUserId(user.getId());
            profile.setCompanyName(companyName);
            profile.setGstNumber(gstNumber);
            profile.setBusinessType(businessType);
            return manufacturerProfileRepository.save(profile);
        });
    }

    private void ensureRetailerProfile(User user, String businessName, String gstNumber, String storeType) {
        retailerProfileRepository.findByUserId(user.getId()).orElseGet(() -> {
            RetailerProfile profile = new RetailerProfile();
            profile.setUserId(user.getId());
            profile.setBusinessName(businessName);
            profile.setGstNumber(gstNumber);
            profile.setStoreType(storeType);
            return retailerProfileRepository.save(profile);
        });
    }

    private void ensureAddress(
            User user,
            String addressType,
            String line1,
            String city,
            String state,
            String country,
            String postalCode) {
        if (!addressRepository.findByUserId(user.getId()).isEmpty()) {
            return;
        }

        Address address = new Address();
        address.setUserId(user.getId());
        address.setAddressType(addressType);
        address.setLine1(line1);
        address.setCity(city);
        address.setState(state);
        address.setCountry(country);
        address.setPostalCode(postalCode);
        address.setPrimary(true);
        addressRepository.save(address);
    }
}
