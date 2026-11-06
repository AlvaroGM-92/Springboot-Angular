---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-03
tags:
  - dominio
  - entidad
  - multi-tenant
---

# Observación

## Propósito

Anotación que un profesor registra sobre un alumno: conducta, rendimiento académico, asistencia u otra incidencia. La administración puede consultarlas, pero no crearlas ni modificarlas.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant de la observación. |
| `alumno_id` | BIGINT | Sí | FK a [[Alumno]] de la misma mancomunidad. |
| `profesor_id` | BIGINT | Sí | FK a [[Usuario]] con pertenencia activa `PROFESOR` en esa mancomunidad. |
| `asignatura_id` | BIGINT | No | FK a [[Asignatura]] de la misma mancomunidad; contexto opcional. |
| `curso_escolar_id` | BIGINT | Sí | FK a [[Curso escolar]] de la misma mancomunidad; contiene la `fecha`. |
| `tipo` | VARCHAR(20) | Sí | `CONDUCTA`, `ACADEMICA`, `ASISTENCIA` u `OTRA`. |
| `texto` | TEXT | Sí | 1–2000 caracteres. |
| `fecha` | DATE | Sí | No puede ser futura. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Alumno]]**.
- **N:1 con [[Usuario]]**.
- **N:1 con [[Asignatura]]** (opcional).
- **N:1 con [[Curso escolar]]**.

## Reglas

- Alumno, profesor, asignatura y curso deben pertenecer a la misma mancomunidad.
- La `fecha` debe estar dentro del rango `fecha_inicio`–`fecha_fin` del [[Curso escolar]].
- En la creación, el curso se puede omitir en la API: el servicio lo resuelve por la fecha. Si no hay un curso único que la contenga, responde `422` y exige `cursoEscolarId` ([[ADR-018 - Curso escolar en las observaciones]]).
- Solo el profesor autor puede editar o eliminar su observación.
- El profesor solo puede observar alumnos de sus asignaturas.
- Si se indica asignatura, debe ser una que imparta y en la que esté matriculado el alumno.
- La administración solo consulta.
- Se muestran en la ficha del alumno ordenadas por fecha descendente y se pueden filtrar por curso escolar.
- Las observaciones de cursos `CERRADO` se conservan y solo se consultan.
- No se borran físicamente si se quieren conservar como histórico; en la v1 se permite eliminar la propia.

## Dudas

- ~~¿Cómo se agrupan las observaciones por curso si solo tienen fecha?~~ **Resuelto: con `curso_escolar_id` obligatorio; la API puede inferirlo de la fecha.** Véase [[ADR-018 - Curso escolar en las observaciones]].
- ¿Debe haber un tipo «seguimiento» adicional? Se puede añadir en una fase posterior.
- ¿Se permite adjuntar archivos? Fuera de alcance.

## Relacionado

[[Mancomunidad]] · [[Multi-tenencia]] · [[Alumno]] · [[Usuario]] · [[Asignatura]] · [[Curso escolar]] · [[Modelo de datos]] · [[API REST]] · [[ADR-018 - Curso escolar en las observaciones]]
