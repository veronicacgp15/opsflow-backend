package com.opsflow.auth_service.infrastructure.security;

import com.opsflow.auth_service.infrastructure.entities.Permission;
import com.opsflow.auth_service.infrastructure.entities.Role;
import com.opsflow.auth_service.infrastructure.entities.User;
import com.opsflow.auth_service.infrastructure.repositories.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Carga el usuario para Spring Security exponiendo TANTO los nombres de rol (e.g.
 * {@code ROLE_ADMIN}) COMO los codigos de permiso asociados a esos roles (e.g.
 * {@code ORG_DEACTIVATE}) como {@link GrantedAuthority}.
 *
 * <p>De este modo:
 * <ul>
 *   <li>El JWT generado por {@code JwtUtils} viaja con ambos tipos de authority en su claim
 *       {@code roles}.</li>
 *   <li>Los microservicios destino pueden gatear sus endpoints con
 *       {@code @PreAuthorize("hasAuthority('CODIGO_PERMISO')")} y NO solo por rol, lo que
 *       permite mover capacidades entre roles desde el modal de permisos sin tener que tocar
 *       codigo ni reasignar nadie a {@code ROLE_ADMIN}.</li>
 *   <li>Las reglas legacy basadas en rol siguen funcionando porque el rol tambien viaja.</li>
 * </ul>
 *
 * <p>El metodo es {@code @Transactional(readOnly = true)} porque {@code Role.permissions} es
 * {@code FetchType.LAZY}; sin la sesion abierta saltaria {@code LazyInitializationException}
 * al resolver los permisos.
 */
@Service
public class JpaUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public JpaUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Usuario %s no existe", username)));
        return toUserDetails(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("Usuario id=%s no existe", id)));
        return toUserDetails(user);
    }

    private UserDetails toUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                Boolean.TRUE.equals(user.getEnabled()),
                true,
                true,
                true,
                resolveAuthorities(user));
    }

    private Set<GrantedAuthority> resolveAuthorities(User user) {
        return Optional.ofNullable(user.getRoles())
                .orElseGet(List::of)
                .stream()
                .flatMap(JpaUserDetailsService::authoritiesFromRole)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private static Stream<GrantedAuthority> authoritiesFromRole(Role role) {
        Stream<GrantedAuthority> roleAuthority = Optional.ofNullable(role.getName())
                .<GrantedAuthority>map(SimpleGrantedAuthority::new)
                .stream();

        Stream<GrantedAuthority> permissionAuthorities = Optional.ofNullable(role.getPermissions())
                .orElseGet(Set::of)
                .stream()
                .map(Permission::getCode)
                .filter(Objects::nonNull)
                .<GrantedAuthority>map(SimpleGrantedAuthority::new);

        return Stream.concat(roleAuthority, permissionAuthorities);
    }
}
