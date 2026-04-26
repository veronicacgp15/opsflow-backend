package com.opsflow.auth_service.infrastructure.mappers;

import com.opsflow.auth_service.domain.models.UserDomain;
import com.opsflow.auth_service.domain.ports.out.RoleRepositoryPort;
import com.opsflow.auth_service.infrastructure.UserMapper;
import com.opsflow.auth_service.infrastructure.entities.Role;
import com.opsflow.auth_service.infrastructure.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMapperTest {

    @Mock
    private RoleRepositoryPort roleRepositoryPort;

    @InjectMocks
    private UserMapper userMapper;

    private User userEntity;
    private UserDomain userDomain;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        adminRole = new Role("ROLE_ADMIN");
        adminRole.setId(1L);

        userEntity = new User();
        userEntity.setId(1L);
        userEntity.setUsername("admin");
        userEntity.setEmail("admin@test.com");
        userEntity.setPassword("hash");
        userEntity.setRoles(List.of(adminRole));

        userDomain = new UserDomain();
        userDomain.setId(1L);
        userDomain.setUsername("admin");
        userDomain.setEmail("admin@test.com");
        userDomain.setPassword("plain");
        userDomain.setRoles(List.of("ROLE_ADMIN"));
    }

    @Test
    void toDomain_shouldMapAllFieldsCorrectly() {
        UserDomain result = userMapper.toDomain(userEntity);

        assertNotNull(result);
        assertEquals(userEntity.getId(), result.getId());
        assertEquals(userEntity.getUsername(), result.getUsername());
        assertEquals(userEntity.getEmail(), result.getEmail());
        assertTrue(result.getRoles().contains("ROLE_ADMIN"));
    }

    @Test
    void toEntity_shouldMapAllFieldsCorrectly() {
        when(roleRepositoryPort.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        User result = userMapper.toEntity(userDomain);

        assertNotNull(result);
        assertEquals(userDomain.getUsername(), result.getUsername());
        assertEquals(userDomain.getEmail(), result.getEmail());
        assertEquals(1, result.getRoles().size());
        assertEquals("ROLE_ADMIN", result.getRoles().get(0).getName());
    }

    @Test
    void toDomain_shouldReturnNull_whenEntityIsNull() {
        assertNull(userMapper.toDomain(null));
    }

    @Test
    void toEntity_shouldReturnNull_whenDomainIsNull() {
        assertNull(userMapper.toEntity(null));
    }
}
