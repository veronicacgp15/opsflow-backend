package com.opsflow.auth_service.domain.constants;

public final class AuthConstants {

    public static final String USER_REGISTERED_SUCCESSFULLY = "User registered successfully!";
    public static final String REFRESH_TOKEN_IS_NOT_IN_DATABASE = "Refresh token is not in database!";
    public static final String USUARIO_O_CONTRASENA_INCORRECTOS = "Usuario o contraseña incorrectos.";
    public static final String ROL_NO_PUEDE_ESTAR_VACIO = "El nombre del rol no puede estar vacío.";
    public static final String BEARER = "Bearer";

    private AuthConstants() {
    }

    public static final String OPSFLOW_EXCHANGE = "opsflow.exchange";
    public static final String AUTH_USER_REGISTERED_QUEUE = "auth.user.registered";
    public static final String AUTH_USER_ROUTING_KEY = "auth.user.#";

    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_USER = "ROLE_USER";

    public static final String AUTH_EXCHANGE = "auth-exchange";
    public static final String USER_REGISTERED_QUEUE = "user-registered-queue";
    public static final String USER_REGISTERED_ROUTING_KEY = "user.registered";

    public static final String ERROR_USERNAME_IS_ALREADY_TAKEN = "Error: Username is already taken!";

    public static final Long DEFAULT_ORGANIZATION = 1L;
}
