package com.opsflow.org_service.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.sql.Connection;
import java.sql.Statement;

@Configuration
public class SchemaInitializer {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean(name = "mscOrgSchemaInitializer")
    public boolean createSchema() {
        String schemaName = "msc_org";

        String baseUrl = url.split("\\?")[0];

        try (Connection conn = java.sql.DriverManager.getConnection(baseUrl, username, password);
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE SCHEMA IF NOT EXISTS " + schemaName);
            System.out.println("✅ Esquema '" + schemaName + "' verificado/creado exitosamente.");
            return true;
        } catch (Exception e) {
            System.err.println("❌ Error al crear el esquema: " + e.getMessage());
            return false;
        }
    }
}