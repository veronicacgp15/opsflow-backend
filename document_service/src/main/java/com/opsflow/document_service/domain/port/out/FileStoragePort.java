package com.opsflow.document_service.domain.port.out;

import org.springframework.web.multipart.MultipartFile;

public interface FileStoragePort {
    String uploadFile(MultipartFile file, String folder);
    byte[] downloadFile(String fileUrl);
    void deleteFile(String fileUrl);
}
