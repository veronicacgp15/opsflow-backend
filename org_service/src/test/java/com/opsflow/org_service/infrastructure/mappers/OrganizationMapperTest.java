package com.opsflow.org_service.infrastructure.mappers;

import com.opsflow.org_service.domain.models.OrganizationDomain;
import com.opsflow.org_service.infrastructure.entities.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class OrganizationMapperTest {

    private OrganizationMapper organizationMapper;

    @BeforeEach
    void setUp() {
        organizationMapper = new OrganizationMapper();
    }

    @Test
    @DisplayName("Debe mapear de Entity a Domain correctamente")
    void toDomainShouldMapCorrectly() {
        // GIVEN
        LocalDateTime now = LocalDateTime.now();
        Organization entity = new Organization();
        entity.setId(10L);
        entity.setName("OpsFlow");
        entity.setTaxId("B12345678");
        entity.setAddress("Calle Principal 1");
        entity.setEmail("contacto@opsflow.com");
        entity.setPhone("555-0000");
        entity.setActive(true);
        entity.setCreatedAt(now);

        // WHEN
        OrganizationDomain domain = organizationMapper.toDomain(entity);

        // THEN
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(10L);
        assertThat(domain.getName()).isEqualTo("OpsFlow");
        assertThat(domain.getTaxId()).isEqualTo("B12345678");
        assertThat(domain.getAddress()).isEqualTo("Calle Principal 1");
        assertThat(domain.getEmail()).isEqualTo("contacto@opsflow.com");
        assertThat(domain.getPhone()).isEqualTo("555-0000");
        assertThat(domain.getActive()).isTrue();
        assertThat(domain.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Debe mapear nulo de Entity a Domain")
    void shouldEntityIsNull() {
        assertThat(organizationMapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Debe mapear de Domain a Entity correctamente")
    void shouldMapCorrectly() {
        // GIVEN
        LocalDateTime now = LocalDateTime.now();
        OrganizationDomain domain = new OrganizationDomain();
        domain.setId(20L);
        domain.setName("Test Org");
        domain.setTaxId("A87654321");
        domain.setAddress("Avenida Secundaria 2");
        domain.setEmail("info@testorg.com");
        domain.setPhone("555-1111");
        domain.setActive(false);
        domain.setCreatedAt(now);

        // WHEN
        Organization entity = organizationMapper.toEntity(domain);

        // THEN
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(20L);
        assertThat(entity.getName()).isEqualTo("Test Org");
        assertThat(entity.getTaxId()).isEqualTo("A87654321");
        assertThat(entity.getAddress()).isEqualTo("Avenida Secundaria 2");
        assertThat(entity.getEmail()).isEqualTo("info@testorg.com");
        assertThat(entity.getPhone()).isEqualTo("555-1111");
        assertThat(entity.getActive()).isFalse();
        assertThat(entity.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Debe mapear nulo de Domain a Entity")
    void shouldReturnNullWhenDomainIsNull() {
        assertThat(organizationMapper.toEntity(null)).isNull();
    }
}
