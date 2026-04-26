package com.opsflow.document_service.application.services;

import com.opsflow.document_service.application.dtos.DocumentCreateDTO;
import com.opsflow.document_service.application.dtos.DocumentUpdateDTO;
import com.opsflow.document_service.domain.enums.DocumentStatus;
import com.opsflow.document_service.domain.models.DocumentDomain;
import com.opsflow.document_service.domain.port.out.DocumentEventPublisherPort;
import com.opsflow.document_service.domain.port.out.DocumentRepositoryPort;
import com.opsflow.document_service.domain.port.out.FileStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class DocumentServiceImpl implements DocumentService {

    private final DocumentRepositoryPort documentRepositoryPort;
    private final DocumentEventPublisherPort eventPublisherPort;
    private final FileStoragePort fileStoragePort;

    @Override
    @Transactional
    public DocumentDomain createDocument(DocumentCreateDTO dto, MultipartFile file) {
        DocumentDomain document = new DocumentDomain();
        document.setName(dto.getName());
        document.setDocumentTypeId(dto.getDocumentTypeId());
        document.setExpirationDate(dto.getExpirationDate());
        document.setOwnerId(dto.getUserId());
        document.setOrganizationId(dto.getOrganizationId());
        document.setTargetEntityType(dto.getTargetEntityType());
        document.setTargetEntityId(dto.getTargetEntityId());
        document.setStatus(DocumentStatus.ACTIVE);
        
        document = documentRepositoryPort.save(document);

        String fileUrl = fileStoragePort.uploadFile(file, generateFolderPath(document, 1));
        
        ensureVersionsList(document);
        document.addNewVersion(fileUrl, file.getSize(), dto.getUserId());

        return saveAndPublish(document, true);
    }

    @Override
    public Optional<DocumentDomain> getDocumentById(Long id) {
        return documentRepositoryPort.findById(id);
    }

    @Override
    public List<DocumentDomain> getAllDocuments() {
        return documentRepositoryPort.findAll();
    }

    @Override
    @Transactional
    public void deleteDocument(Long id) {
        documentRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional
    public DocumentDomain uploadNewVersion(Long documentId, MultipartFile file, Long userId) {
        DocumentDomain document = findOrThrow(documentId);

        int nextVersion = getNextVersionNumber(document);
        String fileUrl = fileStoragePort.uploadFile(file, generateFolderPath(document, nextVersion));
        
        document.addNewVersion(fileUrl, file.getSize(), userId);

        return saveAndPublish(document, false);
    }

    @Override
    @Transactional
    public DocumentDomain updateDocument(Long id, DocumentUpdateDTO dto) {
        DocumentDomain document = findOrThrow(id);

        Optional.ofNullable(dto.getName()).ifPresent(document::setName);
        Optional.ofNullable(dto.getDocumentTypeId()).ifPresent(document::setDocumentTypeId);
        Optional.ofNullable(dto.getExpirationDate()).ifPresent(document::setExpirationDate);
        Optional.ofNullable(dto.getTargetEntityType()).ifPresent(document::setTargetEntityType);
        Optional.ofNullable(dto.getTargetEntityId()).ifPresent(document::setTargetEntityId);

        return saveAndPublish(document, false);
    }

    @Override
    public List<DocumentDomain> getDocumentsByOrganization(Long organizationId) {
        return documentRepositoryPort.findAll().stream()
                .filter(doc -> organizationId.equals(doc.getOrganizationId()))
                .toList();
    }

    @Override
    @Transactional
    public DocumentDomain updateState(Long id, String state) {
        DocumentDomain document = findOrThrow(id);
        document.setStatus(DocumentStatus.valueOf(state.toUpperCase()));
        return saveAndPublish(document, false);
    }

    private DocumentDomain findOrThrow(Long id) {
        return documentRepositoryPort.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found with ID: " + id));
    }

    private DocumentDomain saveAndPublish(DocumentDomain document, boolean isNew) {
        DocumentDomain saved = documentRepositoryPort.save(document);
        if (isNew) eventPublisherPort.publishDocumentCreatedEvent(saved);
        else eventPublisherPort.publishDocumentUpdatedEvent(saved);
        return saved;
    }

    private void ensureVersionsList(DocumentDomain document) {
        if (document.getVersions() == null) document.setVersions(new ArrayList<>());
    }

    private int getNextVersionNumber(DocumentDomain document) {
        return (document.getVersions() != null ? document.getVersions().size() : 0) + 1;
    }

    private String generateFolderPath(DocumentDomain doc, int version) {
        return String.format("documents/org_%d/doc_%d/v%d", doc.getOrganizationId(), doc.getId(), version);
    }
}
