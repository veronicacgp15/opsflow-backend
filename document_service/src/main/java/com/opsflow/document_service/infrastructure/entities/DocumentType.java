package com.opsflow.document_service.infrastructure.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "document_types")
@Getter
@Setter
public class DocumentType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    /**
     * Borrado logico: {@code false} indica tipo inactivo (no se ofrece en el catalogo para nuevos documentos).
     * {@code nullable = true} evita que Hibernate falle al anadir la columna en tablas ya pobladas
     * (ADD NOT NULL sin default); {@link com.opsflow.document_service.infrastructure.config.DocumentTypeActiveColumnInitializer}
     * normaliza datos y deja la columna NOT NULL + DEFAULT en PostgreSQL.
     */
    @Column(nullable = true)
    @ColumnDefault("true")
    private Boolean active = true;

    @PrePersist
    @PreUpdate
    void normalizeActive() {
        if (active == null) {
            active = true;
        }
    }
}
