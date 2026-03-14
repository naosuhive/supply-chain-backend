package com.noasuhive.supply_chain_management;

import com.noasuhive.supply_chain_management.models.Role;
import com.noasuhive.supply_chain_management.models.User;
import com.noasuhive.supply_chain_management.repositories.RoleRepository;
import com.noasuhive.supply_chain_management.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
@Profile("dev")
public class DevDataLoader implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DevDataLoader(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed roles
        String[] roles = {"ROLE_ADMIN", "ROLE_MANUFACTURER", "ROLE_RETAILER", "ROLE_CUSTOMER"};
        for (String r : roles) {
            roleRepository.findByName(r).orElseGet(() -> roleRepository.save(new Role(r)));
        }

        // Seed admin user if not present
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@example.com");
            admin.setPassword(passwordEncoder.encode("adminpass"));
            Set<Role> adminRoles = new HashSet<>();
            roleRepository.findByName("ROLE_ADMIN").ifPresent(adminRoles::add);
            admin.setRoles(adminRoles);
            userRepository.save(admin);
        }
    }
}

