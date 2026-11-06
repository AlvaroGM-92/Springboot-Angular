-- Datos de desarrollo. Solo se cargan con los perfiles dev y test.
-- Credenciales de demo (no usar en producción):
--   admin@mancomunidad.local / Temporal1234
-- La cuenta se crea con cambio de contraseña obligatorio.

INSERT INTO mancomunidad (nombre, codigo, tipo, duracion_maxima_franja_min, activo, version, created_at, updated_at)
VALUES ('Mancomunidad Demo', 'demo', 'MUSICA', NULL, TRUE, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

INSERT INTO usuario (nombre, apellidos, email, password_hash, debe_cambiar_password, password_temporal_expira_at,
                     email_verificado, email_verificado_at, token_version, admin_plataforma, activo, ultimo_acceso,
                     version, created_at, updated_at)
VALUES ('Administrador', 'Demo', 'admin@mancomunidad.local',
        '$2a$12$1JRTIsaLxmTSVPCvJBpz0OYl2jZzsZYln6d/MfESzhW8lo92VsoxG',
        TRUE, DATE_ADD(UTC_TIMESTAMP(6), INTERVAL 24 HOUR),
        TRUE, UTC_TIMESTAMP(6), 0, FALSE, TRUE, NULL,
        0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

INSERT INTO curso_escolar (mancomunidad_id, codigo, fecha_inicio, fecha_fin, estado, version, created_at, updated_at)
SELECT m.id, '2026-2027', '2026-09-01', '2027-06-30', 'ACTIVO', 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM mancomunidad m
WHERE m.codigo = 'demo';

INSERT INTO usuario_mancomunidad (usuario_id, mancomunidad_id, rol, token_version, activo, version, created_at, updated_at)
SELECT u.id, m.id, 'ADMINISTRADOR', 0, TRUE, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM usuario u
         JOIN mancomunidad m ON m.codigo = 'demo'
WHERE u.email = 'admin@mancomunidad.local';
