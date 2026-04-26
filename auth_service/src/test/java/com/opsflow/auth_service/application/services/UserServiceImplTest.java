package com.opsflow.auth_service.application.services;

import com.opsflow.auth_service.application.events.UserRegisteredEvent;
import com.opsflow.auth_service.application.ports.UserEventPublisher;
import com.opsflow.auth_service.domain.models.UserDomain;
import com.opsflow.auth_service.domain.ports.out.UserRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserEventPublisher userEventPublisher;

    @InjectMocks
    private UserServiceImpl userService;

    private UserDomain userDomain;

    @BeforeEach
    void setUp() {
        userDomain = new UserDomain();
        userDomain.setUsername("testuser");
        userDomain.setPassword("password123");
        userDomain.setEmail("test@test.com");
        userDomain.setOrganizationId(1L);
    }

    @Test
    void shouldSavePasswordAndPublishEvent() {
        // Given
        UserDomain savedDomain = new UserDomain();
        savedDomain.setId(1L);
        savedDomain.setUsername("testuser");
        savedDomain.setEmail("test@test.com");
        savedDomain.setOrganizationId(1L);

        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepositoryPort.save(any(UserDomain.class))).thenReturn(savedDomain);

        // When
        UserDomain result = userService.save(userDomain);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepositoryPort, times(1)).save(userDomain);
        verify(userEventPublisher, times(1)).publishUserRegistered(any(UserRegisteredEvent.class));
    }

    @Test
    void shouldFindByUsername() {
        when(userRepositoryPort.findByUsername("testuser")).thenReturn(Optional.of(userDomain));

        Optional<UserDomain> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }
}
