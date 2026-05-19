package com.opsflow.document_service.infrastructure.controllers;

import com.opsflow.document_service.application.dtos.DocumentTypeDTO;
import com.opsflow.document_service.application.dtos.request.DocumentTypeActivePatchRequest;
import com.opsflow.document_service.application.dtos.request.DocumentTypeUpsertRequest;
import com.opsflow.document_service.application.services.DocumentTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Tipos de documento: solo rol ADMIN. El listado raíz devuelve solo tipos activos;
 * {@code /all} incluye inactivos. Desactivar o borrar (DELETE) solo es posible si ningún
 * documento referencia ese tipo.
 */
@RestController
@RequestMapping("/documents/types")
@RequiredArgsConstructor
@Tag(name = "Document Type Controller", description = "Tipos de documento")
public class DocumentTypeController {

    private final DocumentTypeService documentTypeService;

    @Operation(summary = "Listar tipos activos (catalogo admin)")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentTypeDTO>> listActiveCatalog() {
        return ResponseEntity.ok(documentTypeService.listActiveCatalog());
    }

    @Operation(summary = "Listar todos los tipos (incluye inactivos)")
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DocumentTypeDTO>> listAll() {
        return ResponseEntity.ok(documentTypeService.listAll());
    }

    @Operation(summary = "Obtener un tipo por id")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentTypeDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(documentTypeService.getById(id));
    }

    @Operation(summary = "Crear tipo de documento")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentTypeDTO> create(@Valid @RequestBody DocumentTypeUpsertRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(documentTypeService.create(request));
    }

    @Operation(summary = "Actualizar tipo de documento")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentTypeDTO> update(
            @PathVariable Long id,
            @Valid @RequestBody DocumentTypeUpsertRequest request) {
        return ResponseEntity.ok(documentTypeService.update(id, request));
    }

    @Operation(summary = "Cambiar solo el estado activo/inactivo", description = "Body JSON minimo: {\"active\": true|false}")
    @PatchMapping("/{id}/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentTypeDTO> patchActive(
            @PathVariable Long id,
            @Valid @RequestBody DocumentTypeActivePatchRequest body) {
        return ResponseEntity.ok(documentTypeService.setActive(id, Boolean.TRUE.equals(body.getActive())));
    }

    @Operation(summary = "Activar tipo (borrado logico revertido)")
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentTypeDTO> activate(@PathVariable Long id) {
        return ResponseEntity.ok(documentTypeService.activate(id));
    }

    @Operation(summary = "Desactivar tipo (borrado logico)")
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentTypeDTO> deactivate(@PathVariable Long id) {
        return ResponseEntity.ok(documentTypeService.deactivate(id));
    }

    @Operation(summary = "Borrado logico (equivalente a desactivar)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DocumentTypeDTO> softDelete(@PathVariable Long id) {
        return ResponseEntity.ok(documentTypeService.deactivate(id));
    }
}
