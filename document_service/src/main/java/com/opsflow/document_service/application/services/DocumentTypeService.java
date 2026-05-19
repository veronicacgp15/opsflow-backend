package com.opsflow.document_service.application.services;

import com.opsflow.document_service.application.dtos.DocumentTypeDTO;
import com.opsflow.document_service.application.dtos.request.DocumentTypeUpsertRequest;

import java.util.List;

public interface DocumentTypeService {

    List<DocumentTypeDTO> listActiveCatalog();

    List<DocumentTypeDTO> listAll();

    DocumentTypeDTO getById(Long id);

    DocumentTypeDTO create(DocumentTypeUpsertRequest request);

    DocumentTypeDTO update(Long id, DocumentTypeUpsertRequest request);

    DocumentTypeDTO activate(Long id);

    DocumentTypeDTO deactivate(Long id);

    DocumentTypeDTO setActive(Long id, boolean active);
}
