package com.opsflow.document_service.infrastructure.mappers;

import com.opsflow.document_service.domain.models.DocumentDomain;
import com.opsflow.document_service.infrastructure.entities.Document;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {DocumentVersionMapper.class})
public interface DocumentMapper {

    @Mapping(source = "type.id", target = "documentTypeId")
    DocumentDomain toDomain(Document entity);

    @Mapping(source = "documentTypeId", target = "type.id")
    Document toEntity(DocumentDomain domain);

    @AfterMapping
    default void linkVersions(@MappingTarget Document document) {
        if (document.getVersions() != null) {
            document.getVersions().forEach(version -> version.setDocument(document));
        }
    }
}
