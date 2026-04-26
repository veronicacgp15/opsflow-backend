package com.opsflow.document_service.domain.models;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class DocumentVersionDomain {

    private Long id;
    private Integer versionNumber;
    private String fileUrl;
    private Long fileSize;
    private Long uploadedByUserId;
    private LocalDateTime createdAt;
}
