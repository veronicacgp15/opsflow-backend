-- 1. Organización por defecto
INSERT INTO organizations (name, tax_id, address, email, phone, active, created_at)
VALUES ('Default Organization', '0000000000', 'Default Address', 'default@opsflow.com', '0000000000', true, NOW())
ON CONFLICT (tax_id) DO NOTHING;

INSERT INTO organizations (name, tax_id, address, email, phone, active, created_at)
VALUES ('Internacional', '1010101010', 'Mundo', 'internacional@opsflow.com', '0000000000', true, NOW())
ON CONFLICT (tax_id) DO NOTHING;

INSERT INTO organizations (name, tax_id, address, email, phone, active, created_at)
VALUES ('Nacional', '2020202020', 'Nacion', 'nacional@opsflow.com', '0000000000', true, NOW())
ON CONFLICT (tax_id) DO NOTHING;
