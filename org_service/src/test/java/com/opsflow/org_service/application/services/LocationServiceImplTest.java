package com.opsflow.org_service.application.services;

import com.opsflow.org_service.application.dtos.LocationResponse;
import com.opsflow.org_service.application.dtos.request.LocationRequest;
import com.opsflow.org_service.domain.models.LocationDomain;
import com.opsflow.org_service.domain.ports.out.LocationRepositoryPort;
import com.opsflow.org_service.infrastructure.mappers.LocationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepositoryPort locationRepositoryPort;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationServiceImpl locationService;

    private LocationDomain dummyDomain;
    private LocationResponse dummyResponse;
    private LocationRequest dummyRequest;

    @BeforeEach
    void setUp() {
        dummyDomain = new LocationDomain();
        dummyDomain.setId(5L);
        dummyDomain.setCity("Madrid");
        dummyDomain.setOrganizationId(1L);

        dummyResponse = new LocationResponse(5L, "HQ", "Main St", "Madrid", 1L);
        dummyRequest = new LocationRequest("HQ", "Main St", "Madrid", 1L);
    }

    @Test
    @DisplayName("Debe crear una ubicación y retornar el LocationResponse")
    void createShouldReturnLocationResponse() {
        // GIVEN
        when(locationRepositoryPort.save(any(LocationDomain.class))).thenReturn(dummyDomain);
        when(locationMapper.toResponse(dummyDomain)).thenReturn(dummyResponse);

        // WHEN
        LocationResponse result = locationService.create(dummyRequest);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.city()).isEqualTo("Madrid");
        verify(locationRepositoryPort).save(any(LocationDomain.class));
        verify(locationMapper).toResponse(dummyDomain);
    }

    @Test
    @DisplayName("Debe encontrar ubicaciones por ID de organización")
    void findByOrganizationIdShouldReturnList() {
        // GIVEN
        Long orgId = 1L;
        when(locationRepositoryPort.findByOrganizationId(orgId)).thenReturn(List.of(dummyDomain));
        when(locationMapper.toResponse(dummyDomain)).thenReturn(dummyResponse);

        // WHEN
        List<LocationResponse> result = locationService.findByOrganizationId(orgId);

        // THEN
        assertThat(result).hasSize(1);
        assertThat(result.get(0).city()).isEqualTo("Madrid");
        verify(locationRepositoryPort).findByOrganizationId(orgId);
        verify(locationMapper).toResponse(dummyDomain);
    }

    @Test
    @DisplayName("Debe retornar una ubicación específica por ID")
    void findByIdShouldReturnLocationResponse() {
        // GIVEN
        when(locationRepositoryPort.findById(5L)).thenReturn(Optional.of(dummyDomain));
        when(locationMapper.toResponse(dummyDomain)).thenReturn(dummyResponse);

        // WHEN
        Optional<LocationResponse> result = locationService.findById(5L);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(5L);
        verify(locationRepositoryPort).findById(5L);
    }

    @Test
    @DisplayName("Debe actualizar una ubicación existente")
    void updateShouldReturnUpdatedLocationResponse() {
        // GIVEN
        when(locationRepositoryPort.findById(5L)).thenReturn(Optional.of(dummyDomain));
        when(locationRepositoryPort.save(any(LocationDomain.class))).thenReturn(dummyDomain);
        when(locationMapper.toResponse(dummyDomain)).thenReturn(dummyResponse);

        // WHEN
        Optional<LocationResponse> result = locationService.update(5L, dummyRequest);

        // THEN
        assertThat(result).isPresent();
        assertThat(result.get().city()).isEqualTo("Madrid");
        verify(locationRepositoryPort).findById(5L);
        verify(locationRepositoryPort).save(dummyDomain);
    }

    @Test
    @DisplayName("Debe eliminar una ubicación y retornar true si existe")
    void deleteShouldInvokeRepositoryAndReturnTrue() {
        // GIVEN
        when(locationRepositoryPort.findById(5L)).thenReturn(Optional.of(dummyDomain));

        // WHEN
        boolean result = locationService.delete(5L);

        // THEN
        assertThat(result).isTrue();
        verify(locationRepositoryPort).findById(5L);
        verify(locationRepositoryPort).deleteById(5L);
    }

    @Test
    @DisplayName("Debe retornar false al intentar eliminar una ubicación que no existe")
    void deleteShouldReturnFalseIfLocationDoesNotExist() {
        // GIVEN
        when(locationRepositoryPort.findById(99L)).thenReturn(Optional.empty());

        // WHEN
        boolean result = locationService.delete(99L);

        // THEN
        assertThat(result).isFalse();
        verify(locationRepositoryPort).findById(99L);
        verify(locationRepositoryPort, never()).deleteById(anyLong());
    }
}