package com.opsflow.document_service.domain.exceptions;

import com.opsflow.document_service.domain.enums.ErrorCode;

public abstract class DomainException extends RuntimeException {

    private final ErrorCode errorCode;

    public DomainException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public DomainException(String message, ErrorCode errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }


}
