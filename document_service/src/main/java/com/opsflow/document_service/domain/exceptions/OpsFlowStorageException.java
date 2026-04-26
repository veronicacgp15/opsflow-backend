package com.opsflow.document_service.domain.exceptions;

import com.opsflow.document_service.domain.enums.ErrorCode;

public class OpsFlowStorageException extends DomainException {

    public OpsFlowStorageException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

    public OpsFlowStorageException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, errorCode, cause);
    }
}
