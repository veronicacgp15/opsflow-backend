package com.opsflow.auth_service.application.services;

import com.opsflow.auth_service.infrastructure.entities.Role;
import com.opsflow.auth_service.infrastructure.repositories.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.opsflow.auth_service.domain.constants.AuthConstants.ROL_NO_PUEDE_ESTAR_VACIO;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    public RoleServiceImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    @Transactional
    public Role save(String roleName) {
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new IllegalArgumentException(ROL_NO_PUEDE_ESTAR_VACIO);
        }

        String cleanRoleName = roleName.trim().replace("\"", "").toUpperCase();

        if (roleRepository.findByName(cleanRoleName).isPresent()) {
            throw new RuntimeException("El rol '" + cleanRoleName + "' ya existe en el sistema.");
        }

        Role newRole = new Role(cleanRoleName);
        return roleRepository.save(newRole);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return roleRepository.findByName(name.trim().toUpperCase());
    }

    @Override
    @Transactional
    public Role update(Long id, String roleName) {
        return roleRepository.findById(id)
                .map(role -> {
                    role.setName(roleName.trim().toUpperCase());
                    return roleRepository.save(role);
                })
                .orElseThrow(() -> new RuntimeException("Rol no encontrado con ID: " + id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Role> findById(Long id) {
        return roleRepository.findById(id);
    }
}
