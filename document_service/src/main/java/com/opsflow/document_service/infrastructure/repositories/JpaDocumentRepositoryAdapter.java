package com.opsflow.document_service.infrastructure.repositories;

import com.opsflow.document_service.domain.models.DocumentDomain;
import com.opsflow.document_service.domain.port.out.DocumentRepositoryPort;
import com.opsflow.document_service.infrastructure.entities.Document;
import com.opsflow.document_service.infrastructure.mappers.DocumentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class JpaDocumentRepositoryAdapter implements DocumentRepositoryPort {

    private final DocumentJpaRepository documentJpaRepository;
    private final DocumentMapper documentMapper;

    @Override
    public List<DocumentDomain> findAll() {
        return documentJpaRepository.findAll().stream()
                .map(documentMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<DocumentDomain> findById(Long id) {
        return documentJpaRepository.findById(id)
                .map(documentMapper::toDomain);
    }

    @Override
    public DocumentDomain save(DocumentDomain document) {
        Document entity = documentMapper.toEntity(document);
        Document savedEntity = documentJpaRepository.save(entity);
        return documentMapper.toDomain(savedEntity);
    }


    @Override
    public void deleteById(Long id) {
        documentJpaRepository.deleteById(id);
    }

    @Override
    public List<DocumentDomain> findExpiredCandidates(LocalDate referenceDate) {
        return documentJpaRepository.findByExpirationDateLessThanEqual(referenceDate).stream()
                .map(documentMapper::toDomain)
                .toList();
    }


}
