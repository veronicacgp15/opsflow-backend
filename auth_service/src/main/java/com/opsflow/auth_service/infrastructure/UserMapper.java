package com.opsflow.auth_service.infrastructure;

import com.opsflow.auth_service.domain.models.UserDomain;
import com.opsflow.auth_service.domain.ports.out.RoleRepositoryPort;
import com.opsflow.auth_service.infrastructure.entities.Role;
import com.opsflow.auth_service.infrastructure.entities.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final RoleRepositoryPort roleRepositoryPort;

    public UserMapper(RoleRepositoryPort roleRepositoryPort) {
        this.roleRepositoryPort = roleRepositoryPort;
    }

    public UserDomain toDomain(User entity) {
        if (entity == null) return null;
        UserDomain domain = new UserDomain();
        domain.setId(entity.getId());
        domain.setName(entity.getName());
        domain.setLastname(entity.getLastname());
        domain.setUsername(entity.getUsername());
        domain.setPassword(entity.getPassword());
        domain.setEmail(entity.getEmail());
        domain.setEnabled(entity.getEnabled());
        domain.setOrganizationId(entity.getOrganizationId());
        domain.setRoles(entity.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toList()));
        return domain;
    }

    public User toEntity(UserDomain domain) {
        if (domain == null) return null;
        User entity = new User();
        entity.setId(domain.getId());
        entity.setName(domain.getName());
        entity.setLastname(domain.getLastname());
        entity.setUsername(domain.getUsername());
        entity.setPassword(domain.getPassword());
        entity.setEmail(domain.getEmail());
        entity.setEnabled(domain.getEnabled());
        entity.setOrganizationId(domain.getOrganizationId());

        if (domain.getRoles() != null && !domain.getRoles().isEmpty()) {
            List<Role> roles = domain.getRoles().stream()
                    .map(roleName -> roleRepositoryPort.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " is not found.")))
                    .collect(Collectors.toList());
            entity.setRoles(roles);
        } else {
            entity.setRoles(List.of());
        }
       return entity;
    }
}
