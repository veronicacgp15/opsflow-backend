package com.opsflow.document_service.domain.port.out;

import com.opsflow.document_service.domain.models.DocumentDomain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DocumentRepositoryPort {
    List<DocumentDomain> findAll();
    Optional<DocumentDomain> findById(Long id);
    DocumentDomain save(DocumentDomain document);
    void deleteById(Long id);
    List<DocumentDomain> findExpiredCandidates(LocalDate referenceDate);
}
