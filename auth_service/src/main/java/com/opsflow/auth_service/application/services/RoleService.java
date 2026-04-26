package com.opsflow.auth_service.application.services;

import com.opsflow.auth_service.infrastructure.entities.Role;
import java.util.List;
import java.util.Optional;

public interface RoleService {
    Role save(String roleName);
    List<Role> findAll();
    Optional<Role> findByName(String name);
    Role update(Long id, String roleName);
    void delete(Long id);
    Optional<Role> findById(Long id);
}
