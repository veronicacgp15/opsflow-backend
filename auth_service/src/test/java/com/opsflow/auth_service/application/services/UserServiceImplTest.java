package com.opsflow.auth_service.application.services;

import com.opsflow.auth_service.application.events.UserRegisteredEvent;
import com.opsflow.auth_service.application.ports.UserEventPublisher;
import com.opsflow.auth_service.domain.models.UserDomain;
import com.opsflow.auth_service.domain.ports.out.UserRepositoryPort;
import com.opsflow.auth_service.infrastructure.UserMapper;
import com.opsflow.auth_service.infrastructure.entities.User;
import com.opsflow.auth_service.infrastructure.entities.VerificationToken;
import com.opsflow.auth_service.infrastructure.repositories.VerificationTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepositoryPort userRepositoryPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserEventPublisher userEventPublisher;

    @Mock
    private VerificationTokenRepository tokenRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private UserMapper userMapper;

    private UserServiceImpl userService;

    private UserDomain userDomain;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepositoryPort,
                passwordEncoder,
                userEventPublisher,
                tokenRepository,
                emailService,
                userMapper
        );

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

        User userEntity = new User();
        userEntity.setId(1L);
        userEntity.setEmail("test@test.com");

        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepositoryPort.save(any(UserDomain.class))).thenReturn(savedDomain);
        when(userMapper.toEntity(savedDomain)).thenReturn(userEntity);
        when(tokenRepository.save(any(VerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        UserDomain result = userService.save(userDomain);

        // Then
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepositoryPort, times(1)).save(userDomain);
        verify(userEventPublisher, times(1)).publishUserRegistered(any(UserRegisteredEvent.class));
        verify(tokenRepository, times(1)).save(any(VerificationToken.class));
        verify(emailService, times(1)).sendVerificationEmail(eq("test@test.com"), anyString());
    }

    @Test
    void shouldFindByUsername() {
        when(userRepositoryPort.findByUsername("testuser")).thenReturn(Optional.of(userDomain));

        Optional<UserDomain> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
    }
}
