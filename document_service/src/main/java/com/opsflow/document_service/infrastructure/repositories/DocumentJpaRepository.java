package com.opsflow.document_service.infrastructure.repositories;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.opsflow.document_service.infrastructure.entities.Document;

@Repository
public interface DocumentJpaRepository extends JpaRepository<Document, Long> {
    List<Document> findByExpirationDateLessThanEqual(LocalDate expirationDate);
}
