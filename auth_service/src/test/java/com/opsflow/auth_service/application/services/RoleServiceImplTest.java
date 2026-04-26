package com.opsflow.auth_service.application.services;

import com.opsflow.auth_service.infrastructure.entities.Role;
import com.opsflow.auth_service.infrastructure.repositories.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.opsflow.auth_service.domain.constants.AuthConstants.ROL_NO_PUEDE_ESTAR_VACIO;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    private Role existingRole;

    @BeforeEach
    void setUp() {
        existingRole = new Role("ROLE_EXISTING");
        existingRole.setId(1L);
    }

    @Test
    void shouldSaveCreateNewRole() {
        // Given
        String inputRoleName = " role_new ";
        String cleanedRoleName = "ROLE_NEW";
        Role newRole = new Role(cleanedRoleName);
        newRole.setId(2L);

        when(roleRepository.findByName(cleanedRoleName)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);

        // When
        Role result = roleService.save(inputRoleName);

        // Then
        assertNotNull(result);
        assertEquals(cleanedRoleName, result.getName());
        verify(roleRepository, times(1)).findByName(cleanedRoleName);
        verify(roleRepository, times(1)).save(any(Role.class));
    }

    @Test
    void shouldSaveThrowRuntimeException() {
        // Given
        String duplicateRoleName = "ROLE_EXISTING";
        when(roleRepository.findByName(duplicateRoleName)).thenReturn(Optional.of(existingRole));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            roleService.save(duplicateRoleName);
        });
        
        assertEquals("El rol '" + duplicateRoleName + "' ya existe en el sistema.", exception.getMessage());
        verify(roleRepository, times(1)).findByName(duplicateRoleName);
        verify(roleRepository, never()).save(any(Role.class));
    }

    @Test
    void shouldWhenRoleNameIsNull() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roleService.save(null);
        });
        assertEquals(ROL_NO_PUEDE_ESTAR_VACIO, exception.getMessage());
    }

    @Test
    void shouldRoleNameIsEmpty() {
        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            roleService.save("   ");
        });
        assertEquals(ROL_NO_PUEDE_ESTAR_VACIO, exception.getMessage());
    }

    @Test
    void shouldFindByName() {
        // Given
        String roleNameInput = "role_existing ";
        String cleanedRoleName = "ROLE_EXISTING";
        when(roleRepository.findByName(cleanedRoleName)).thenReturn(Optional.of(existingRole));

        // When
        Optional<Role> result = roleService.findByName(roleNameInput);

        // Then
        assertTrue(result.isPresent());
        assertEquals(cleanedRoleName, result.get().getName());
        verify(roleRepository, times(1)).findByName(cleanedRoleName);
    }

    @Test
    void shouldFindByNameRoleDoesNotExist() {
        // Given
        String nonExistentRoleName = "NON_EXISTENT";
        when(roleRepository.findByName(nonExistentRoleName)).thenReturn(Optional.empty());

        // When
        Optional<Role> result = roleService.findByName(nonExistentRoleName);

        // Then
        assertFalse(result.isPresent());
    }
}
