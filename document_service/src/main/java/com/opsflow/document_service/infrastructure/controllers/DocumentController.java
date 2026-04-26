package com.opsflow.document_service.infrastructure.controllers;

import com.opsflow.common.JwtUtils;
import com.opsflow.document_service.application.dtos.DocumentCreateDTO;
import com.opsflow.document_service.application.dtos.DocumentUpdateDTO;
import com.opsflow.document_service.application.dtos.response.MessageResponse;
import com.opsflow.document_service.application.services.DocumentService;
import com.opsflow.document_service.domain.models.DocumentDomain;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.opsflow.document_service.domain.constants.DocumentConstants.*;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Document Controller", description = "Endpoints for managing documents")
public class DocumentController {

    private final DocumentService documentService;
    private final JwtUtils jwtUtils;

    @Operation(summary = "Create a new document", description = "Admin, Manager or User can create documents")
    @PostMapping(value = "/create", consumes = { "multipart/form-data" })
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER', 'USER')")
    public ResponseEntity<DocumentDomain> createDocument(
            @RequestPart("data") DocumentCreateDTO dto,
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        
        if (!jwtUtils.hasRole(authentication, "ADMIN")) {
            Long orgId = jwtUtils.getOrganizationIdFromAuthentication(authentication);
            dto.setOrganizationId(orgId);
        }
        
        Long userId = jwtUtils.getUserIdFromAuthentication(authentication);
        dto.setUserId(userId);

        return new ResponseEntity<>(documentService.createDocument(dto, file), HttpStatus.CREATED);
    }

    @Operation(summary = "Get document by ID")
    @GetMapping("/{id}")
    @PreAuthorize("@securityService.canAccessDocument(#id)")
    public ResponseEntity<DocumentDomain> getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all documents")
    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DocumentDomain>> getAllDocuments(Authentication authentication) {
        if (jwtUtils.hasRole(authentication, "ADMIN")) {
            return ResponseEntity.ok(documentService.getAllDocuments());
        } else {
            Long orgId = jwtUtils.getOrganizationIdFromAuthentication(authentication);
            return ResponseEntity.ok(documentService.getDocumentsByOrganization(orgId));
        }
    }

    @Operation(summary = "Update document metadata")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @securityService.isUserInSameOrganizationAsDocument(#id)) or @securityService.isDocumentOwner(#id)")
    public ResponseEntity<DocumentDomain> updateDocument(
            @PathVariable Long id,
            @RequestBody DocumentUpdateDTO dto) {
        return ResponseEntity.ok(documentService.updateDocument(id, dto));
    }

    @Operation(summary = "Delete a document")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @securityService.isUserInSameOrganizationAsDocument(#id))")
    public ResponseEntity<MessageResponse> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new MessageResponse(DOCUMENTO_CON_ID + id + ELIMINADO_EXITOSAMENTE));
    }

    @Operation(summary = "Upload a new version")
    @PostMapping("/add-version/{id}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('MANAGER') and @securityService.isUserInSameOrganizationAsDocument(#id)) or @securityService.isDocumentOwner(#id)")
    public ResponseEntity<DocumentDomain> uploadNewVersion(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file,
            Authentication authentication) {
        Long userId = jwtUtils.getUserIdFromAuthentication(authentication);
        return ResponseEntity.ok(documentService.uploadNewVersion(id, file, userId));
    }

    @Operation(summary = "Force state change", description = "Admin only")
    @PatchMapping("/{id}/force-state")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentDomain> forceStateChange(@PathVariable Long id, @RequestParam String state) {
        return ResponseEntity.ok(documentService.updateState(id, state));
    }
}
