package com.opsflow.org_service.infrastructure.controllers;

import com.opsflow.org_service.application.dtos.LocationResponse;
import com.opsflow.org_service.application.dtos.MessageResponse;
import com.opsflow.org_service.application.dtos.request.LocationRequest;
import com.opsflow.org_service.domain.ports.in.LocationServicePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationControllerTest {

    @Mock
    private LocationServicePort locationServicePort;

    @InjectMocks
    private LocationController locationController;

    private LocationRequest dummyRequest;
    private LocationResponse dummyResponse;

    @BeforeEach
    void setUp() {
        dummyRequest = new LocationRequest("Central", "Calle", "Madrid", 1L);
        dummyResponse = new LocationResponse(5L, "Central", "Calle", "Madrid", 1L);
    }

    @Test
    @DisplayName("Debe crear una sede y retornar 201 CREATED")
    void createShouldReturnCreated() {
        when(locationServicePort.create(any(LocationRequest.class))).thenReturn(dummyResponse);

        ResponseEntity<LocationResponse> response = locationController.create(dummyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dummyResponse);
        verify(locationServicePort).create(dummyRequest);
    }

    @Test
    @DisplayName("Debe retornar lista de sedes con 200 OK")
    void findAllShouldReturnOk() {
        when(locationServicePort.findAll()).thenReturn(List.of(dummyResponse));

        ResponseEntity<List<LocationResponse>> response = locationController.findAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(locationServicePort).findAll();
    }

    @Test
    @DisplayName("Debe retornar sedes por organización con 200 OK")
    void findByOrganizationShouldReturnOk() {
        when(locationServicePort.findByOrganizationId(1L)).thenReturn(List.of(dummyResponse));

        ResponseEntity<List<LocationResponse>> response = locationController.findByOrganization(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
        verify(locationServicePort).findByOrganizationId(1L);
    }

    @Test
    @DisplayName("Debe retornar sede por ID con 200 OK")
    void findByIdShouldReturnOkIfPresent() {
        when(locationServicePort.findById(5L)).thenReturn(Optional.of(dummyResponse));

        ResponseEntity<LocationResponse> response = locationController.findById(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(dummyResponse);
    }

    @Test
    @DisplayName("Debe actualizar una sede y retornar 201 CREATED")
    void updateShouldReturnCreatedIfPresent() {
        when(locationServicePort.update(eq(5L), any(LocationRequest.class))).thenReturn(Optional.of(dummyResponse));

        ResponseEntity<LocationResponse> response = locationController.update(5L, dummyRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(dummyResponse);
    }

    @Test
    @DisplayName("Debe retornar 200 OK si la sede se elimina exitosamente")
    void deleteShouldReturnOkIfDeleted() {
        when(locationServicePort.delete(5L)).thenReturn(true);

        ResponseEntity<MessageResponse> response = locationController.delete(5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().message()).contains("exitosamente");
    }

    @Test
    @DisplayName("Debe retornar 404 NOT FOUND si la sede no se puede eliminar")
    void deleteShouldReturnNotFoundIfNotDeleted() {
        when(locationServicePort.delete(99L)).thenReturn(false);

        ResponseEntity<MessageResponse> response = locationController.delete(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
