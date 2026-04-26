package com.opsflow.auth_service.application.services;

import com.opsflow.auth_service.application.events.UserRegisteredEvent;
import com.opsflow.auth_service.application.ports.UserEventPublisher;
import com.opsflow.auth_service.domain.models.UserDomain;
import com.opsflow.auth_service.domain.ports.out.UserRepositoryPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepositoryPort userRepositoryPort;
    private final PasswordEncoder passwordEncoder;
    private final UserEventPublisher userEventPublisher;

    public UserServiceImpl(UserRepositoryPort userRepositoryPort,
                           PasswordEncoder passwordEncoder,
                           UserEventPublisher userEventPublisher) {
        this.userRepositoryPort = userRepositoryPort;
        this.passwordEncoder = passwordEncoder;
        this.userEventPublisher = userEventPublisher;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDomain> findAll() {
        return userRepositoryPort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDomain> findById(Long id) {
        return userRepositoryPort.findById(id);
    }

    @Override
    @Transactional
    public UserDomain save(UserDomain userDomain) {
        userDomain.setPassword(passwordEncoder.encode(userDomain.getPassword()));
        UserDomain savedUserDomain = userRepositoryPort.save(userDomain);
        
        var event = new UserRegisteredEvent(
            savedUserDomain.getId(),
            savedUserDomain.getUsername(),
            savedUserDomain.getEmail(),
            savedUserDomain.getOrganizationId()
        );
        userEventPublisher.publishUserRegistered(event);
        
        return savedUserDomain;
    }

    @Override
    @Transactional
    public Optional<UserDomain> update(Long id, UserDomain userDomain) {
        return userRepositoryPort.update(id, userDomain);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userRepositoryPort.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDomain> findByUsername(String username) {
        return userRepositoryPort.findByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDomain> findByOrganizationId(Long organizationId) {
        return userRepositoryPort.findAll().stream()
                .filter(user -> organizationId.equals(user.getOrganizationId()))
                .toList();
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        userRepositoryPort.findById(userId).ifPresent(user -> {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepositoryPort.update(userId, user);
        });
    }

    @Override
    @Transactional
    public void deactivateAccount(Long userId) {
        userRepositoryPort.findById(userId).ifPresent(user -> {
            user.setEnabled(false);
            userRepositoryPort.update(userId, user);
        });
    }

    @Override
    @Transactional
    public Optional<UserDomain> updateRoles(Long userId, List<String> roles) {
        return userRepositoryPort.findById(userId).map(user -> {
            user.setRoles(roles);
            return userRepositoryPort.update(userId, user).orElse(user);
        });
    }
}
