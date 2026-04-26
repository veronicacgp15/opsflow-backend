package com.opsflow.auth_service.infrastructure.controllers;

import com.opsflow.auth_service.application.dtos.MessageResponse;
import com.opsflow.auth_service.application.dtos.request.CreateRoleRequest;
import com.opsflow.auth_service.application.dtos.request.ChangeRoleRequest;
import com.opsflow.auth_service.application.services.RoleService;
import com.opsflow.auth_service.application.services.UserService;
import com.opsflow.auth_service.infrastructure.entities.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auth/roles")
@Tag(name = "Role Controller", description = "Endpoints for managing roles and user role assignments. Restricted to ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;
    private final UserService userService;

    public RoleController(RoleService roleService, UserService userService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @Operation(summary = "Create a new role", description = "Only accessible by users with ROLE_ADMIN")
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> createRole(@Valid @RequestBody CreateRoleRequest request) {
        Role savedRole = roleService.save(request.name());
        return ResponseEntity.ok(savedRole);
    }

    @Operation(summary = "Update an existing role", description = "Only accessible by users with ROLE_ADMIN")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.ok(roleService.update(id, request.name()));
    }

    @Operation(summary = "Delete a role", description = "Only accessible by users with ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deleteRole(@PathVariable Long id) {
        roleService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Rol eliminado exitosamente."));
    }

    @Operation(summary = "Change a user's role", description = "Only accessible by users with ROLE_ADMIN")
    @PutMapping("/users/{userId}/change-role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> changeUserRole(@PathVariable Long userId,
                                                          @Valid @RequestBody
                                                          ChangeRoleRequest request) {
        return userService.findById(userId)
                .map(user -> {
                    user.setRoles(List.of(request.roleName().toUpperCase()));
                    userService.update(userId, user);
                    return ResponseEntity.ok(new MessageResponse("Rol de usuario " + userId + " actualizado a " + request.roleName()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all roles", description = "Only accessible by users with ROLE_ADMIN")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @Operation(summary = "Get role by ID", description = "Only accessible by users with ROLE_ADMIN")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        return roleService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
