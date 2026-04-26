-- =====================================================================
-- 1. INSERTAR TIPOS DE DOCUMENTOS (Los catálogos primero)
-- =====================================================================
INSERT INTO document_types (id, name, description)
VALUES (1, 'Acuerdo de Confidencialidad', 'Documento legal (NDA) para la protección de información sensible')
ON CONFLICT (id) DO NOTHING;

INSERT INTO document_types (id, name, description)
VALUES (2, 'Factura Comercial', 'Registro detallado de transacciones de venta y servicios prestados')
ON CONFLICT (id) DO NOTHING;

INSERT INTO document_types (id, name, description)
VALUES (3, 'Política de Privacidad', 'Lineamientos sobre el tratamiento y protección de datos personales')
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- 2. INSERTAR DOCUMENTOS (La cabecera del documento)
-- =====================================================================
INSERT INTO documents (id, name, status, expiration_date, document_type_id, user_id, organization_id, target_entity_type, target_entity_id)
VALUES (1, 'Contrato de Mantenimiento Anual', 'ACTIVE', '2025-12-31', 1, 2, 1, 'CLIENT', 101)
ON CONFLICT (id) DO NOTHING;

INSERT INTO documents (id, name, status, expiration_date, document_type_id, user_id, organization_id, target_entity_type, target_entity_id)
VALUES (2, 'Anexo Técnico NDA', 'ACTIVE', NULL, 1, 2, 1, 'PROVIDER', 500)
ON CONFLICT (id) DO NOTHING;

INSERT INTO documents (id, name, status, expiration_date, document_type_id, user_id, organization_id, target_entity_type, target_entity_id)
VALUES (3, 'Factura de Implementación Q1', 'ACTIVE', '2024-03-30', 2, 2, 1, 'CLIENT', 101)
ON CONFLICT (id) DO NOTHING;

-- =====================================================================
-- 3. INSERTAR VERSIONES DE DOCUMENTOS (El detalle del archivo)
-- =====================================================================
INSERT INTO document_versions (id, document_id, version_number, file_url, file_size, uploaded_by_user_id, created_at)
VALUES (1, 1, 1, 'https://s3.opsflow.com/organization-1/docs/contrato_mantenimiento_v1.pdf', 1572864, 2, '2024-05-20 09:00:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO document_versions (id, document_id, version_number, file_url, file_size, uploaded_by_user_id, created_at)
VALUES (2, 2, 1, 'https://s3.opsflow.com/organization-1/docs/anexo_nda_v1.pdf', 524288, 2, '2024-05-21 14:20:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO document_versions (id, document_id, version_number, file_url, file_size, uploaded_by_user_id, created_at)
VALUES (3, 3, 1, 'https://s3.opsflow.com/organization-1/docs/factura_q1_final.pdf', 204800, 2, '2024-01-15 11:00:00')
ON CONFLICT (id) DO NOTHING;
