package com.opsflow.org_service.infrastructure.adapters;

import com.opsflow.org_service.domain.models.LocationDomain;
import com.opsflow.org_service.infrastructure.entities.Location;
import com.opsflow.org_service.infrastructure.entities.Organization;
import com.opsflow.org_service.infrastructure.mappers.LocationMapper;
import com.opsflow.org_service.infrastructure.repositories.LocationRepository;
import com.opsflow.org_service.infrastructure.repositories.OrganizationRepository;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationPersistenceAdapterTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationPersistenceAdapter adapter;

    private LocationDomain dummyDomain;
    private Location dummyEntity;
    private Organization dummyOrg;

    @BeforeEach
    void setUp() {
        dummyDomain = new LocationDomain();
        dummyDomain.setId(5L);
        dummyDomain.setOrganizationId(1L);

        dummyEntity = new Location();
        dummyEntity.setId(5L);

        dummyOrg = new Organization();
        dummyOrg.setId(1L);
    }

    @Test
    @DisplayName("Debe guardar la ubicación verificando la organización")
    void saveShouldPersistLocationIfOrgExists() {
        when(locationMapper.toEntity(dummyDomain)).thenReturn(dummyEntity);
        when(organizationRepository.findById(1L)).thenReturn(Optional.of(dummyOrg));
        when(locationRepository.save(dummyEntity)).thenReturn(dummyEntity);
        when(locationMapper.toDomain(dummyEntity)).thenReturn(dummyDomain);

        LocationDomain result = adapter.save(dummyDomain);

        assertThat(result).isNotNull();
        verify(organizationRepository).findById(1L);
        verify(locationRepository).save(dummyEntity);
    }

    @Test
    @DisplayName("Debe lanzar excepción si la organización no existe al guardar")
    void saveShouldThrowExceptionIfOrgNotFound() {
        when(locationMapper.toEntity(dummyDomain)).thenReturn(dummyEntity);
        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adapter.save(dummyDomain))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Organización no encontrada");

        verify(locationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Debe encontrar ubicación por ID")
    void findByIdShouldReturnDomain() {
        when(locationRepository.findById(5L)).thenReturn(Optional.of(dummyEntity));
        when(locationMapper.toDomain(dummyEntity)).thenReturn(dummyDomain);

        Optional<LocationDomain> result = adapter.findById(5L);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("Debe encontrar todas las ubicaciones")
    void findAllShouldReturnList() {
        when(locationRepository.findAll()).thenReturn(List.of(dummyEntity));
        when(locationMapper.toDomain(dummyEntity)).thenReturn(dummyDomain);

        List<LocationDomain> result = adapter.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Debe encontrar ubicaciones por ID de organización")
    void findByOrganizationIdShouldReturnList() {
        when(locationRepository.findByOrganizationId(1L)).thenReturn(List.of(dummyEntity));
        when(locationMapper.toDomain(dummyEntity)).thenReturn(dummyDomain);

        List<LocationDomain> result = adapter.findByOrganizationId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Debe eliminar por ID")
    void deleteByIdShouldCallRepository() {
        doNothing().when(locationRepository).deleteById(5L);

        adapter.deleteById(5L);

        verify(locationRepository).deleteById(5L);
    }
}
