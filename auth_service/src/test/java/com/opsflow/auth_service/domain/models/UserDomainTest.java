package com.opsflow.auth_service.domain.models;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserDomainTest {

    @Test
    @DisplayName("Debe asegurar que el estado inicial del dominio sea correcto")
    void shouldHaveCorrectInitialState() {
        // WHEN
        UserDomain user = new UserDomain();

        // THEN
        assertThat(user.getRoles())
                .as("La lista de roles debe estar inicializada y vacía por defecto")
                .isNotNull()
                .isEmpty();

        assertThat(user.getEnabled())
                .as("El estado enabled debería ser null o falso según tu lógica de negocio")
                .isNull();
    }

    @Test
    @DisplayName("Debe asignar y recuperar todos los campos correctamente")
    void shouldSetAndGetAllFields() {
        // GIVEN
        UserDomain user = new UserDomain();
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");

        // WHEN
        user.setId(1L);
        user.setName("John");
        user.setLastname("Doe");
        user.setUsername("jdoe");
        user.setPassword("secret123");
        user.setEmail("jdoe@opsflow.com");
        user.setEnabled(true);
        user.setOrganizationId(100L);
        user.setRoles(roles);

        // THEN
        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getName()).isEqualTo("John");
        assertThat(user.getLastname()).isEqualTo("Doe");
        assertThat(user.getUsername()).isEqualTo("jdoe");
        assertThat(user.getPassword()).isEqualTo("secret123");
        assertThat(user.getEmail()).isEqualTo("jdoe@opsflow.com");
        assertThat(user.getEnabled()).isTrue();
        assertThat(user.getOrganizationId()).isEqualTo(100L);
        assertThat(user.getRoles()).hasSize(2).containsAll(roles);
    }
}
