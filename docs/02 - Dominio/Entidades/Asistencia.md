---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-04
tags:
  - dominio
  - entidad
  - asistencia
  - auditoria
  - multi-tenant
---

# Asistencia

## Propósito

Registra el estado de un alumno en una [[Sesión de clase]]: presente, ausente, ausencia justificada o retraso. Permite pasar lista de forma masiva con una sola pantalla, editarla en cualquier momento y conservar la traza de los cambios.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant de la asistencia. |
| `sesion_id` | BIGINT | Sí | FK a [[Sesión de clase]] de la misma mancomunidad. |
| `alumno_id` | BIGINT | Sí | FK a [[Alumno]] de la misma mancomunidad. |
| `estado` | VARCHAR(25) | Sí | `PRESENTE`, `AUSENTE`, `AUSENCIA_JUSTIFICADA` o `RETRASO`. |
| `minutos_retraso` | SMALLINT | No | Solo si `estado = RETRASO`; 1–180. |
| `motivo` | VARCHAR(255) | No | Obligatorio si la ausencia está justificada. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Sesión de clase]]**.
- **N:1 con [[Alumno]]**.
- **1:N con [[Historial de asistencia]]**.

## Reglas

- Único por `sesion_id + alumno_id`.
- Sesión y alumno deben pertenecer a la misma mancomunidad.
- La asistencia y su historial solo son accesibles desde la mancomunidad activa.
- El alumno debe tener matrícula `ACTIVA` en la asignatura y curso en la fecha de la sesión para entrar en la lista; si causa baja después, sus filas ya creadas se conservan y no se borran ([[ADR-026 - Baja de alumno a mitad de curso]]).
- Solo el profesor asignado a la sesión puede crear o modificar asistencias: el titular o, durante una sustitución activa, el sustituto ([[ADR-025 - Sustituciones y cambio de titular]]).
- Se guardan todos los alumnos de la sesión, incluidos los presentes, para dejar constancia de que se pasó lista.
- Una sesión `PROGRAMADA` no tiene filas de asistencia; se crean al abrirla ([[ADR-013 - Generación automática de sesiones desde el horario]]).
- Una sesión `CANCELADA` no tiene filas de asistencia y no admite crearlas; si la clase se imparte, se usa una sesión puntual nueva ([[ADR-017 - Cancelación de sesiones y días no lectivos]]).
- El estado por defecto al abrir la sesión es `PRESENTE`.
- `AUSENCIA_JUSTIFICADA` exige motivo.
- `RETRASO` puede llevar minutos y motivo opcional.
- Los contadores de faltas y retrasos se calculan por alumno, asignatura y curso.
- **La asistencia se puede editar en cualquier momento**, no hay ventana de bloqueo.
- Cada modificación genera una fila en [[Historial de asistencia]] con el antes, el después, el autor y la fecha.
- El profesor **no ve** el historial; la administración sí.
- No se borran asistencias: se corrigen y queda registro, salvo la purga por retención.
- Si un alumno causa baja, su asistencia y su historial se conservan mientras la matrícula esté activa y 2 años desde la baja; después se purgan y los contadores quedan en [[Resumen de asistencia]] con `alumno_hash` ([[ADR-019 - Resumen de asistencia y purga anonimizada]]).
- La baja no elimina filas de sesiones ya abiertas ni cambia su estado; solo afecta a las listas que se abran después ([[ADR-026 - Baja de alumno a mitad de curso]]).

## Dudas

- ~~¿Quién puede justificar: profesor o administrador?~~ **Resuelto: solo el profesor asignado a la sesión (titular o sustituto); el administrador no edita asistencias.**
- ¿Se descuentan los retrasos de algún cómputo? No en la v1.
- ¿Se puede eliminar una asistencia creada por error? No; se corrige y queda en el historial.

## Relacionado

[[Mancomunidad]] · [[Multi-tenencia]] · [[Sesión de clase]] · [[Alumno]] · [[Matrícula]] · [[Historial de asistencia]] · [[Resumen de asistencia]] · [[Observación]] · [[Sustitución]] · [[Modelo de datos]] · [[ADR-007 - Historial de cambios de asistencia]] · [[ADR-013 - Generación automática de sesiones desde el horario]] · [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[ADR-026 - Baja de alumno a mitad de curso]]
