package com.opsflow.document_service.infrastructure.mappers;

import com.opsflow.document_service.domain.models.DocumentVersionDomain;
import com.opsflow.document_service.infrastructure.entities.DocumentVersion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DocumentVersionMapper {

    DocumentVersionDomain toDomain(DocumentVersion entity);

    @Mapping(target = "document", ignore = true)
    DocumentVersion toEntity(DocumentVersionDomain domain);

    List<DocumentVersionDomain> toDomainList(List<DocumentVersion> entities);
    List<DocumentVersion> toEntityList(List<DocumentVersionDomain> domains);
}
