package com.opsflow.document_service.infrastructure.exceptions;

public class DocumentTypeInUseException extends RuntimeException {

    public DocumentTypeInUseException(String message) {
        super(message);
    }
}
