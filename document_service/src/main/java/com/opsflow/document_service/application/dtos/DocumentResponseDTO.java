package com.opsflow.document_service.application.dtos;

import com.opsflow.document_service.domain.enums.DocumentStatus;
import java.time.LocalDate;
import java.util.List;

public record DocumentResponseDTO(
        Long id,
        String name,
        String documentTypeName,
        DocumentStatus status,
        LocalDate expirationDate,
        Long organizationId,
        String targetEntityType,
        Long targetEntityId,
        String currentFileUrl,
        List<VersionResponseDTO> versions
) {
}

record VersionResponseDTO(
        Integer versionNumber,
        String fileUrl,
        Long fileSize,
        java.time.LocalDateTime createdAt
) {}
