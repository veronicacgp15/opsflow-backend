package com.opsflow.document_service.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.sql.Connection;
import java.sql.Statement;

@Configuration
@Order(1)
public class SchemaInitializer {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @PostConstruct
    public void init() {
        String schemaName = "msc_document";

        String baseUrl = url.contains("?") ? url.split("\\?")[0] : url;

        try (Connection conn = java.sql.DriverManager.getConnection(baseUrl, username, password);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            System.out.println("✅ Esquema '" + schemaName + "' verificado/creado exitosamente.");
        } catch (Exception e) {
            System.err.println("❌ Error crítico en SchemaInitializer: " + e.getMessage());
        }
    }
}
