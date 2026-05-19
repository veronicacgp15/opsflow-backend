package com.opsflow.document_service.application.services;

import com.opsflow.document_service.application.dtos.DocumentTypeDTO;
import com.opsflow.document_service.application.dtos.request.DocumentTypeUpsertRequest;
import com.opsflow.document_service.infrastructure.entities.DocumentType;
import com.opsflow.document_service.infrastructure.exceptions.DocumentTypeInUseException;
import com.opsflow.document_service.infrastructure.repositories.DocumentJpaRepository;
import com.opsflow.document_service.infrastructure.repositories.DocumentTypeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentTypeServiceImpl implements DocumentTypeService {

    private final DocumentTypeJpaRepository repository;
    private final DocumentJpaRepository documentRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTypeDTO> listActiveCatalog() {
        return repository.findByActiveTrueOrderByNameAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentTypeDTO> listAll() {
        return repository.findAllByOrderByNameAsc().stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentTypeDTO getById(Long id) {
        return toDto(findEntity(id));
    }

    @Override
    @Transactional
    public DocumentTypeDTO create(DocumentTypeUpsertRequest request) {
        assertNameUnique(trimName(request.getName()), null);
        DocumentType entity = new DocumentType();
        entity.setName(trimName(request.getName()));
        entity.setDescription(trimToNull(request.getDescription()));
        entity.setActive(Boolean.TRUE);
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public DocumentTypeDTO update(Long id, DocumentTypeUpsertRequest request) {
        DocumentType entity = findEntity(id);
        assertNameUnique(trimName(request.getName()), id);
        entity.setName(trimName(request.getName()));
        entity.setDescription(trimToNull(request.getDescription()));
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public DocumentTypeDTO activate(Long id) {
        DocumentType entity = findEntity(id);
        entity.setActive(Boolean.TRUE);
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public DocumentTypeDTO deactivate(Long id) {
        if (documentRepository.existsByType_Id(id)) {
            throw new DocumentTypeInUseException(
                    "No se puede eliminar ni desactivar el tipo de documento porque hay documentos que lo estan usando.");
        }
        DocumentType entity = findEntity(id);
        entity.setActive(Boolean.FALSE);
        return toDto(repository.save(entity));
    }

    @Override
    @Transactional
    public DocumentTypeDTO setActive(Long id, boolean active) {
        if (active) {
            return activate(id);
        }
        return deactivate(id);
    }

    private DocumentType findEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de documento no encontrado: id=" + id));
    }

    private void assertNameUnique(String name, Long excludeId) {
        if (excludeId == null) {
            if (repository.existsByNameIgnoreCase(name)) {
                throw new IllegalArgumentException("Ya existe un tipo de documento con ese nombre.");
            }
        } else if (repository.existsByNameIgnoreCaseAndIdNot(name, excludeId)) {
            throw new IllegalArgumentException("Ya existe un tipo de documento con ese nombre.");
        }
    }

    private static String trimName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        String t = name.trim();
        if (t.isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio.");
        }
        return t;
    }

    private static String trimToNull(String description) {
        if (description == null) {
            return null;
        }
        String t = description.trim();
        return t.isEmpty() ? null : t;
    }

    private DocumentTypeDTO toDto(DocumentType t) {
        return new DocumentTypeDTO(
                t.getId(),
                t.getName(),
                t.getDescription(),
                !Boolean.FALSE.equals(t.getActive()));
    }
}
