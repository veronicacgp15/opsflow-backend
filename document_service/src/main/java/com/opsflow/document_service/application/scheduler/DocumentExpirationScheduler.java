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
public class DocumentExpirationScheduler {

    private final DocumentRepositoryPort repositoryPort;
    private final DocumentEventPublisherPort eventPublisher;

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void checkExpirations() {
        log.info("Iniciando escaneo diario de vencimientos para OpsFlow...");
        LocalDate today = LocalDate.now();

        List<DocumentDomain> candidates = repositoryPort.findExpiredCandidates(today);

        if (candidates.isEmpty()) {
            log.info("No se encontraron documentos pendientes de actualización de estado.");
            return;
        }

        log.info("Procesando {} posibles cambios de estado.", candidates.size());

        candidates.forEach(doc -> processDocumentStatus(doc, today));

        log.info("Escaneo de vencimientos completado con éxito.");
    }

    private void processDocumentStatus(DocumentDomain doc, LocalDate today) {
        DocumentStatus oldStatus = doc.getStatus();


        doc.updateStatusBasedOnDate(today);


        if (oldStatus != doc.getStatus()) {
            repositoryPort.save(doc);
            notifyStatusChange(doc);
            log.debug("Documento ID {}: Cambio de estado {} -> {}", doc.getId(), oldStatus, doc.getStatus());
        }
    }

    private void notifyStatusChange(DocumentDomain doc) {
        switch (doc.getStatus()) {
            case EXPIRING -> eventPublisher.publishDocumentExpiringEvent(doc);
            case EXPIRED  -> eventPublisher.publishDocumentExpiredEvent(doc);
            case ACTIVE   -> log.debug("Documento {} sigue activo, no se requiere notificación.", doc.getId());
        }
    }
}