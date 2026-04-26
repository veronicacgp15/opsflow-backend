-- Crear esquemas para los microservicios
CREATE SCHEMA IF NOT EXISTS msc_auth;
CREATE SCHEMA IF NOT EXISTS msc_org;

-- Opcional: Otorgar permisos si fuera necesario (por defecto el usuario postgres tiene acceso a todo)
-- GRANT ALL PRIVILEGES ON SCHEMA msc_auth TO postgres;
-- GRANT ALL PRIVILEGES ON SCHEMA msc_org TO postgres;
