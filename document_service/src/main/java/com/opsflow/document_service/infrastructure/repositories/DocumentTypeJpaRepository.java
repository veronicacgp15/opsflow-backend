package com.opsflow.document_service.infrastructure.repositories;

import com.opsflow.document_service.infrastructure.entities.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentTypeJpaRepository extends JpaRepository<DocumentType, Long> {

    List<DocumentType> findByActiveTrueOrderByNameAsc();

    List<DocumentType> findAllByOrderByNameAsc();

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
