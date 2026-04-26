package com.opsflow.org_service.infrastructure.mappers;

import com.opsflow.org_service.application.dtos.LocationResponse;
import com.opsflow.org_service.domain.models.LocationDomain;
import com.opsflow.org_service.infrastructure.entities.Location;
import com.opsflow.org_service.infrastructure.entities.Organization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LocationMapperTest {

    private LocationMapper locationMapper;

    @BeforeEach
    void setUp() {
        locationMapper = new LocationMapper();
    }

    @Test
    @DisplayName("Debe mapear de Entity a Domain correctamente")
    void toDomainShouldMapCorrectly() {
        // GIVEN
        Location entity = new Location();
        entity.setId(1L);
        entity.setName("Central HQ");
        entity.setAddress("Calle Falsa 123");
        entity.setCity("Madrid");

        Organization org = new Organization();
        org.setId(10L);
        entity.setOrganization(org);

        // WHEN
        LocationDomain domain = locationMapper.toDomain(entity);

        // THEN
        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(1L);
        assertThat(domain.getName()).isEqualTo("Central HQ");
        assertThat(domain.getAddress()).isEqualTo("Calle Falsa 123");
        assertThat(domain.getCity()).isEqualTo("Madrid");
        assertThat(domain.getOrganizationId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Debe mapear nulo de Entity a Domain")
    void shouldReturnNullWhenEntityIsNull() {
        assertThat(locationMapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("Debe mapear de Domain a Entity correctamente")
    void ShouldMapCorrectly() {
        // GIVEN
        LocationDomain domain = new LocationDomain();
        domain.setId(2L);
        domain.setName("Sucursal");
        domain.setAddress("Avenida Siempreviva 742");
        domain.setCity("Springfield");

        // WHEN
        Location entity = locationMapper.toEntity(domain);

        // THEN
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getName()).isEqualTo("Sucursal");
        assertThat(entity.getAddress()).isEqualTo("Avenida Siempreviva 742");
        assertThat(entity.getCity()).isEqualTo("Springfield");

    }

    @Test
    @DisplayName("Debe mapear nulo de Domain a Entity")
    void shouldReturnNullWhenDomainIsNull() {
        assertThat(locationMapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("Debe mapear de Domain a Response correctamente")
    void shouldMapCorrectly() {
        // GIVEN
        LocationDomain domain = new LocationDomain();
        domain.setId(3L);
        domain.setName("Oficina");
        domain.setAddress("Plaza Mayor");
        domain.setCity("Barcelona");
        domain.setOrganizationId(15L);

        // WHEN
        LocationResponse response = locationMapper.toResponse(domain);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.name()).isEqualTo("Oficina");
        assertThat(response.address()).isEqualTo("Plaza Mayor");
        assertThat(response.city()).isEqualTo("Barcelona");
        assertThat(response.organizationId()).isEqualTo(15L);
    }

    @Test
    @DisplayName("Debe mapear nulo de Domain a Response")
    void toResponseShouldReturnNullWhenDomainIsNull() {
        assertThat(locationMapper.toResponse(null)).isNull();
    }
}
