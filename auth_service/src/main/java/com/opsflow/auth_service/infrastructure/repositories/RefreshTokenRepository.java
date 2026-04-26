package com.opsflow.auth_service.infrastructure.repositories;

import com.opsflow.auth_service.infrastructure.entities.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    Optional<RefreshToken> findByUsername(String username);
}
