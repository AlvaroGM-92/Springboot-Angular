-- Segunda mancomunidad de desarrollo para comprobar el aislamiento por tenant.
-- No se le asigna ningún usuario: existe para verificar que las consultas de
-- una mancomunidad nunca devuelven datos de otra.

INSERT INTO mancomunidad (nombre, codigo, tipo, duracion_maxima_franja_min, activo, version, created_at, updated_at)
VALUES ('Mancomunidad Demo B', 'demo-b', 'DANZA', NULL, TRUE, 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6));

INSERT INTO curso_escolar (mancomunidad_id, codigo, fecha_inicio, fecha_fin, estado, version, created_at, updated_at)
SELECT m.id, '2026-2027', '2026-09-01', '2027-06-30', 'ACTIVO', 0, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6)
FROM mancomunidad m
WHERE m.codigo = 'demo-b';
