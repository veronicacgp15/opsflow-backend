package com.opsflow.auth_service.application.services;

import com.opsflow.auth_service.application.services.impl.RefreshTokenServiceImpl;
import com.opsflow.auth_service.infrastructure.entities.RefreshToken;
import com.opsflow.auth_service.infrastructure.repositories.RefreshTokenRepository;
import com.opsflow.auth_service.infrastructure.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService; // Usamos la implementación para InjectMocks

    private final Long EXPIRATION_MS = 3600000L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenDurationMs", EXPIRATION_MS);
    }

    @Test
    @DisplayName("Debe crear un nuevo RefreshToken y eliminar el anterior si existía")
    void shouldCreateRefreshTokenAndCleanupExistingOne() {
        // GIVEN
        String username = "admin_user";
        RefreshToken existingToken = new RefreshToken();

        when(refreshTokenRepository.findByUsername(username)).thenReturn(Optional.of(existingToken));
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArguments()[0]);

        // WHEN
        RefreshToken result = refreshTokenService.createRefreshToken(username);

        // THEN
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(username);
        assertThat(result.getExpiryDate()).isAfter(Instant.now());

        // Verificamos que se llamó a la eliminación del token previo
        verify(refreshTokenRepository).delete(existingToken);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el token ha expirado")
    void shouldThrowExceptionWhenTokenIsExpired() {
        // GIVEN
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setExpiryDate(Instant.now().minusSeconds(60));

        // WHEN & THEN
        assertThatThrownBy(() -> refreshTokenService.verifyExpiration(expiredToken))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Refresh token was expired");

        verify(refreshTokenRepository).delete(expiredToken);
    }

    @Test
    @DisplayName("Debe retornar el token si no ha expirado")
    void shouldReturnTokenWhenNotExpired() {
        // GIVEN
        RefreshToken validToken = new RefreshToken();
        validToken.setExpiryDate(Instant.now().plusSeconds(3600));

        // WHEN
        RefreshToken result = refreshTokenService.verifyExpiration(validToken);

        // THEN
        assertThat(result).isEqualTo(validToken);
        verify(refreshTokenRepository, never()).delete(any());
    }
}
