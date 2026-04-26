package com.opsflow.document_service.application.dtos;

import java.time.LocalDate;

public record DocumentEvent(
        Long id,
        String name,
        Long organizationId,
        LocalDate expirationDate,
        String status,
        String eventType
) {
}
