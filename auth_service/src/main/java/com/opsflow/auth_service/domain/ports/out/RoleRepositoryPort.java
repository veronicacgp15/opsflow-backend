package com.opsflow.auth_service.domain.ports.out;

import com.opsflow.auth_service.infrastructure.entities.Role;
import java.util.Optional;
import java.util.List;

public interface RoleRepositoryPort {
    Optional<Role> findByName(String name);
    Optional<Role> findById(Long id);
    Role save(Role role);
    List<Role> findAll();
}
