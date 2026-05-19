package com.opsflow.document_service.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Repara la columna {@code active} en {@code document_types} cuando Hibernate la creo como
 * nullable o con filas nulas: rellena, fija DEFAULT y NOT NULL en PostgreSQL.
 */
@Component
@Order(1)
public class DocumentTypeActiveColumnInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DocumentTypeActiveColumnInitializer.class);

    private static final String TABLE = "msc_document.document_types";

    private final JdbcTemplate jdbcTemplate;

    public DocumentTypeActiveColumnInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int updated = jdbcTemplate.update(
                    "UPDATE " + TABLE + " SET active = TRUE WHERE active IS NULL");
            if (updated > 0) {
                log.info("document_types: {} filas con active NULL actualizadas a TRUE", updated);
            }
        } catch (Exception e) {
            log.warn("document_types: no se pudo ejecutar backfill de active (tabla o columna ausente?): {}",
                    e.getMessage());
            return;
        }
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE " + TABLE + " ALTER COLUMN active SET DEFAULT TRUE");
        } catch (Exception e) {
            log.debug("document_types: ALTER COLUMN active SET DEFAULT omitido: {}", e.getMessage());
        }
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE " + TABLE + " ALTER COLUMN active SET NOT NULL");
        } catch (Exception e) {
            log.warn(
                    "document_types: no se pudo fijar NOT NULL en active. "
                            + "Si quedaron NULLs, ejecuta manualmente: UPDATE {} SET active = TRUE WHERE active IS NULL; "
                            + "luego ALTER COLUMN active SET NOT NULL;",
                    TABLE);
        }
    }
}
