package com.opsflow.org_service.infrastructure.controllers;

import com.opsflow.org_service.application.dtos.request.OrganizationRequest;
import com.opsflow.org_service.application.dtos.OrganizationResponse;
import com.opsflow.org_service.application.dtos.MessageResponse;
import com.opsflow.org_service.domain.ports.in.OrganizationServicePort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/org")
@Tag(name = "Organization Controller", description = "Endpoints for managing organizations. Restricted to ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class OrganizationController {

    private final OrganizationServicePort organizationServicePort;

    public OrganizationController(OrganizationServicePort organizationServicePort) {
        this.organizationServicePort = organizationServicePort;
    }

    @Operation(summary = "Create a new organization", description = "Only accessible by users with ROLE_ADMIN")
    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrganizationResponse> create(@Valid @RequestBody OrganizationRequest request) {
        OrganizationResponse response = organizationServicePort.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get all organizations", description = "Only accessible by users with ROLE_ADMIN")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrganizationResponse>> findAll() {
        List<OrganizationResponse> response = organizationServicePort.findAll();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get organization by ID", description = "Admin: Any. Manager/User: Their own.")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isMemberOfOrganization(#id)")
    public ResponseEntity<OrganizationResponse> findById(@PathVariable Long id) {
        return organizationServicePort.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update an organization", description = "Admin: Any. Manager: Only their own.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @securityService.isMemberOfOrganization(#id))")
    public ResponseEntity<OrganizationResponse> update(@PathVariable Long id, @Valid @RequestBody OrganizationRequest request) {
        // En un escenario real, aquí podrías filtrar que el Manager no cambie el planLimit
        return organizationServicePort.update(id, request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Delete an organization", description = "Only accessible by users with ROLE_ADMIN")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        organizationServicePort.delete(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageResponse("Organización con ID " + id + " eliminada exitosamente."));
    }
}
