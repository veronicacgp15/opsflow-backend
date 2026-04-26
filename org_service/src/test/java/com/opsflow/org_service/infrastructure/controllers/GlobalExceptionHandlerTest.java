package com.opsflow.org_service.infrastructure.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("Debe retornar 400 BAD_REQUEST con errores de validación")
    void shouldReturnBadRequest() {
        // GIVEN
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("objectName", "email", "must be a well-formed email address");
        FieldError fieldError2 = new FieldError("objectName", "name", "must not be blank");

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));

        // WHEN
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(ex);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("email", "must be a well-formed email address");
        assertThat(response.getBody()).containsEntry("name", "must not be blank");
    }

    @Test
    @DisplayName("Debe manejar RuntimeException y retornar 500 INTERNAL_SERVER_ERROR con mensaje")
    void shouldReturnInternalServerError() {
        // GIVEN
        RuntimeException ex = new RuntimeException("Error inesperado en el servidor");

        // WHEN
        ResponseEntity<Map<String, String>> response = exceptionHandler.handleRuntimeExceptions(ex);

        // THEN
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).containsEntry("message", "Error inesperado en el servidor");
    }
}
