package com.opsflow.auth_service.infrastructure.config;

import com.opsflow.auth_service.domain.models.UserDomain;
import com.opsflow.auth_service.application.services.UserService;
import com.opsflow.auth_service.infrastructure.entities.Role;
import com.opsflow.auth_service.infrastructure.repositories.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Stream;

import static com.opsflow.auth_service.domain.constants.AuthConstants.*;

@Configuration
public class DataInitializer implements CommandLineRunner {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public DataInitializer(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        Stream.of(ROLE_ADMIN, ROLE_MANAGER, ROLE_USER).forEach(this::createRoleIfNotFound);


        createDefaultUser("admin", "admin@opsflow.com", "Admin", "System", ROLE_ADMIN, ROLE_USER);
        createDefaultUser("manager", "manager@opsflow.com", "Manager", "User", ROLE_MANAGER, ROLE_USER);
        createDefaultUser("user", "user@opsflow.com", "Standard", "User", ROLE_USER);
    }

    private void createDefaultUser(String username, String email, String name, String lastname,
                                   String... roles) {
        userService.findByUsername(username).ifPresentOrElse(
                user -> {},
                () -> {
                    UserDomain newUser = new UserDomain();
                    newUser.setUsername(username);
                    newUser.setPassword("123456");
                    newUser.setEmail(email);
                    newUser.setName(name);
                    newUser.setLastname(lastname);
                    newUser.setEnabled(true);
                    newUser.setOrganizationId(1L);

                    newUser.setRoles(Arrays.asList(roles));

                    userService.save(newUser);
                    System.out.println("Usuario " + username + " creado con roles: " + Arrays.toString(roles));
                }
        );
    }

    private void createRoleIfNotFound(String roleName) {
        if (roleRepository.findByName(roleName).isEmpty()) {
            Role role = new Role();
            role.setName(roleName);
            roleRepository.save(role);
            System.out.println("Rol " + roleName + " creado.");
        }
    }
}
