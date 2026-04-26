package com.opsflow.document_service.domain.port.out;

import com.opsflow.document_service.domain.models.DocumentDomain;

public interface DocumentEventPublisherPort {
    void publishDocumentCreatedEvent(DocumentDomain document);
    void publishDocumentUpdatedEvent(DocumentDomain document);
    void publishDocumentExpiringEvent(DocumentDomain document);
    void publishDocumentExpiredEvent(DocumentDomain document);
}
