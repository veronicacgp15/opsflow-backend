package com.opsflow.document_service.application.scheduler;

import com.opsflow.document_service.domain.enums.DocumentStatus;
import com.opsflow.document_service.domain.models.DocumentDomain;
import com.opsflow.document_service.domain.port.out.DocumentEventPublisherPort;
import com.opsflow.document_service.domain.port.out.DocumentRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DocumentStatusScheduler {

    private final DocumentRepositoryPort repositoryPort;
    private final DocumentEventPublisherPort eventPublisher;

    @Scheduled(cron = "0 0 1 * * ?") // Se ejecuta a la 1:00 AM todos los días
    @Transactional
    public void checkDocumentStatuses() {
        log.info("Iniciando escaneo diario de estados de documentos...");
        LocalDate today = LocalDate.now();
        List<DocumentDomain> documents = repositoryPort.findAll();

        for (DocumentDomain doc : documents) {
            DocumentStatus previousStatus = doc.getStatus();
            doc.updateStatusBasedOnDate(today);

            if (previousStatus != doc.getStatus()) {
                repositoryPort.save(doc);
                if (doc.getStatus() == DocumentStatus.EXPIRING) eventPublisher.publishDocumentExpiringEvent(doc);
                if (doc.getStatus() == DocumentStatus.EXPIRED) eventPublisher.publishDocumentExpiredEvent(doc);
            }
        }
    }
}