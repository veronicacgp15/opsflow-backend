package com.opsflow.org_service.infrastructure.adapters;

import com.opsflow.org_service.domain.models.OrganizationDomain;
import com.opsflow.org_service.infrastructure.entities.Organization;
import com.opsflow.org_service.infrastructure.mappers.OrganizationMapper;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationPersistenceAdapterTest {

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private OrganizationMapper organizationMapper;

    @InjectMocks
    private OrganizationPersistenceAdapter adapter;

    private OrganizationDomain dummyDomain;
    private Organization dummyEntity;

    @BeforeEach
    void setUp() {
        dummyDomain = new OrganizationDomain();
        dummyDomain.setId(10L);
        dummyDomain.setName("Test");

        dummyEntity = new Organization();
        dummyEntity.setId(10L);
        dummyEntity.setName("Test");
    }

    @Test
    @DisplayName("Debe guardar la organización correctamente")
    void saveShouldPersistOrganization() {
        when(organizationMapper.toEntity(dummyDomain)).thenReturn(dummyEntity);
        when(organizationRepository.save(dummyEntity)).thenReturn(dummyEntity);
        when(organizationMapper.toDomain(dummyEntity)).thenReturn(dummyDomain);

        OrganizationDomain result = adapter.save(dummyDomain);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(organizationRepository).save(dummyEntity);
    }

    @Test
    @DisplayName("Debe encontrar organización por ID")
    void findByIdShouldReturnDomain() {
        when(organizationRepository.findById(10L)).thenReturn(Optional.of(dummyEntity));
        when(organizationMapper.toDomain(dummyEntity)).thenReturn(dummyDomain);

        Optional<OrganizationDomain> result = adapter.findById(10L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test");
    }

    @Test
    @DisplayName("Debe encontrar todas las organizaciones")
    void findAllShouldReturnList() {
        when(organizationRepository.findAll()).thenReturn(List.of(dummyEntity));
        when(organizationMapper.toDomain(dummyEntity)).thenReturn(dummyDomain);

        List<OrganizationDomain> result = adapter.findAll();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Debe encontrar organización por TaxId")
    void findByTaxIdShouldReturnDomain() {
        when(organizationRepository.findByTaxId("A123")).thenReturn(Optional.of(dummyEntity));
        when(organizationMapper.toDomain(dummyEntity)).thenReturn(dummyDomain);

        Optional<OrganizationDomain> result = adapter.findByTaxId("A123");

        assertThat(result).isPresent();
    }

    @Test
    @DisplayName("Debe eliminar por ID")
    void deleteByIdShouldCallRepository() {
        doNothing().when(organizationRepository).deleteById(10L);

        adapter.deleteById(10L);

        verify(organizationRepository).deleteById(10L);
    }
}
