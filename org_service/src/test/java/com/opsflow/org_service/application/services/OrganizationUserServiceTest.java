package com.opsflow.org_service.application.services;

import com.opsflow.org_service.infrastructure.entities.Organization;
import com.opsflow.org_service.infrastructure.entities.OrganizationUser;
import com.opsflow.org_service.infrastructure.repositories.OrganizationRepository;
import com.opsflow.org_service.infrastructure.repositories.OrganizationUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganizationUserServiceTest {

    @Mock
    private OrganizationUserRepository organizationUserRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @InjectMocks
    private OrganizationUserService organizationUserService;

    @Test
    @DisplayName("Debe asociar un usuario a una organización exitosamente")
    void associateUserToOrganizationShouldSaveRelation() {
        // GIVEN
        Long userId = 10L;
        Long orgId = 1L;

        Organization org = new Organization();
        org.setId(orgId);

        when(organizationRepository.findById(orgId)).thenReturn(Optional.of(org));

        // WHEN
        organizationUserService.associateUserToOrganization(userId, orgId);

        // THEN
        verify(organizationRepository).findById(orgId);

        ArgumentCaptor<OrganizationUser> orgUserCaptor = ArgumentCaptor.forClass(OrganizationUser.class);
        verify(organizationUserRepository).save(orgUserCaptor.capture());

        OrganizationUser capturedUser = orgUserCaptor.getValue();
        assertThat(capturedUser.getUserId()).isEqualTo(userId);
        assertThat(capturedUser.getOrganization().getId()).isEqualTo(orgId);
        assertThat(capturedUser.getRole()).isEqualTo("MEMBER");
    }

    @Test
    @DisplayName("Debe lanzar excepción si la organización no existe")
    void associateUserToOrganizationShouldThrowExceptionWhenOrgNotFound() {
        // GIVEN
        Long userId = 10L;
        Long orgId = 99L;

        when(organizationRepository.findById(orgId)).thenReturn(Optional.empty());

        // WHEN / THEN
        assertThatThrownBy(() -> organizationUserService.associateUserToOrganization(userId, orgId))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(String.valueOf(orgId));

        verify(organizationRepository).findById(orgId);
        verify(organizationUserRepository, never()).save(any(OrganizationUser.class));
    }
}