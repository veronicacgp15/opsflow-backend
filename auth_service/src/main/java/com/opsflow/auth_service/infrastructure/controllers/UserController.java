package com.opsflow.auth_service.infrastructure.controllers;

import com.opsflow.auth_service.application.dtos.MessageResponse;
import com.opsflow.auth_service.application.services.RefreshTokenService;
import com.opsflow.auth_service.application.services.UserService;
import com.opsflow.auth_service.domain.models.UserDomain;
import com.opsflow.common.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@Tag(name = "User Controller", description = "Endpoints for user management with role-based access control")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    public record PasswordChangeRequest(String newPassword) {}

    private final UserService userService;
    private final RefreshTokenService refreshTokenService;
    private final JwtUtils jwtUtils;

    public UserController(UserService userService, RefreshTokenService refreshTokenService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.jwtUtils = jwtUtils;
    }

    @Operation(summary = "Get all users", description = "Admin only")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDomain>> list() {
        return ResponseEntity.ok(userService.findAll());
    }

    @Operation(summary = "Get user by ID", description = "Admin or Manager of the same organization")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @securityService.isUserInSameOrganization(#id))")
    public ResponseEntity<UserDomain> getById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get users in my organization", description = "Manager: See users of their org")
    @GetMapping("/my-organization")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<List<UserDomain>> getMyOrgUsers(Authentication authentication) {
        return userService.findByUsername(authentication.getName())
                .map(user -> ResponseEntity.ok(userService.findByOrganizationId(user.getOrganizationId())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @Operation(summary = "Register user", description = "Admin: Register anyone. Manager: Invite to their org (only ROLE_USER)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    public ResponseEntity<UserDomain> create(@RequestBody UserDomain userDomain,
                                             Authentication authentication) {
        if (!jwtUtils.hasRole(authentication, "ADMIN")) {
            UserDomain manager = userService.findByUsername(authentication.getName())
                    .orElseThrow(() -> new AccessDeniedException("Manager info not found"));
            
            userDomain.setOrganizationId(manager.getOrganizationId());
            userDomain.setRoles(List.of("ROLE_USER"));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.save(userDomain));
    }

    @Operation(summary = "Update user", description = "Admin: Update any user details")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDomain> update(@PathVariable Long id, @RequestBody UserDomain userDomain) {
        return userService.update(id, userDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Modify roles", description = "Admin: Assign and modify roles")
    @PatchMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDomain> updateRoles(@PathVariable Long id, @RequestBody List<String> roles) {
        return userService.findById(id)
                .map(user -> {
                    user.setRoles(roles);
                    return ResponseEntity.ok(userService.save(user));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Deactivate account", description = "Admin only")
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> deactivate(@PathVariable Long id) {
        userService.deactivateAccount(id);
        return ResponseEntity.ok(new MessageResponse("Usuario desactivado exitosamente"));
    }

    @Operation(summary = "Revoke user sessions", description = "Admin: Revoke tokens and sessions")
    @DeleteMapping("/{id}/sessions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> revokeSessions(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    refreshTokenService.deleteByUsername(user.getUsername());
                    return ResponseEntity.ok(new MessageResponse("Sesiones revocadas para " + user.getUsername()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Change own password", description = "User: Change their own password")
    @PatchMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MessageResponse> changePassword(@RequestBody PasswordChangeRequest request, Authentication authentication) {
        UserDomain user = userService.findByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("User not found"));
        
        userService.changePassword(user.getId(), request.newPassword());
        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada exitosamente"));
    }
}
