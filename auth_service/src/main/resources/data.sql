-- 1. Insertar Roles básicos (Aseguramos que existan)
INSERT INTO roles (id, name) VALUES (1, 'ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (id, name) VALUES (2, 'ROLE_MANAGER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (id, name) VALUES (3, 'ROLE_USER') ON CONFLICT (name) DO NOTHING;

-- 2. Insertar Usuarios (Password: 123456 cifrado con BCrypt real)
-- Hash para '123456': $2a$10$vY3vN/T.O4fP6uI7t/M6zO7V/P6uI7t/M6zO7V.P6uI7t/M6zO7V.
INSERT INTO users (id, name, lastname, username, password, email, enabled, organization_id)
VALUES (1, 'Admin', 'System', 'admin', '$2a$10$y8CKYPZrqRNwDxXdzxrsgOefhm5Hm9PoV.GbXDi.JvlO1YyXfw1s2', 'admin@opsflow.com', true, 1)
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (id, name, lastname, username, password, email, enabled, organization_id)
VALUES (2, 'Manager', 'ManagerSystem', 'manager', '$2a$10$y8CKYPZrqRNwDxXdzxrsgOefhm5Hm9PoV.GbXDi.JvlO1YyXfw1s2', 'manager@opsflow.com', true, 1)
ON CONFLICT (username) DO NOTHING;

INSERT INTO users (id, name, lastname, username, password, email, enabled, organization_id)
VALUES (3, 'User', 'UserPrueba', 'user', '$2a$10$y8CKYPZrqRNwDxXdzxrsgOefhm5Hm9PoV.GbXDi.JvlO1YyXfw1s2', 'user@opsflow.com', true, 1)
ON CONFLICT (username) DO NOTHING;

-- 3. Asociar Roles a Usuarios (Relación Muchos a Muchos)
-- Admin -> ROLE_ADMIN
INSERT INTO users_to_roles (user_id, role_id) VALUES (1, 1) ON CONFLICT DO NOTHING;
-- Manager -> ROLE_MANAGER
INSERT INTO users_to_roles (user_id, role_id) VALUES (2, 2) ON CONFLICT DO NOTHING;
-- User -> ROLE_USER
INSERT INTO users_to_roles (user_id, role_id) VALUES (3, 3) ON CONFLICT DO NOTHING;

-- Resetear el contador de las secuencias para evitar conflictos con registros manuales futuros
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('roles', 'id'), (SELECT MAX(id) FROM roles));
