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

# Centro

## Propósito

Centro de enseñanza que forma parte de la mancomunidad. Agrupa asignaturas y alumnos; su área depende de las asignaturas que oferte, no de una restricción del centro.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant al que pertenece el centro. |
| `nombre` | VARCHAR(150) | Sí | Único dentro de la mancomunidad. |
| `direccion` | VARCHAR(255) | No | Texto libre. |
| `telefono` | VARCHAR(30) | No | Formato flexible. |
| `email` | VARCHAR(150) | No | Formato válido si se informa. |
| `activo` | BOOLEAN | Sí | Por defecto `true`. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **1:N con [[Asignatura]]**.
- **1:N con [[Alumno]]**.

## Reglas

- El nombre es único dentro de la mancomunidad: `UNIQUE (mancomunidad_id, nombre)`.
- El centro y sus datos solo son visibles desde su mancomunidad.
- Un centro no puede relacionarse con asignaturas o alumnos de otra mancomunidad.
- No se puede desactivar un centro con asignaturas activas, alumnos activos o usuarios vinculados.
- Los centros inactivos no aparecen en nuevas asignaciones.
- El histórico de un centro inactivo se conserva y sigue siendo consultable.

## Dudas

- ¿Debe existir un centro «sede» por defecto? No en la v1.
- ¿Se guarda el CIF u otro dato fiscal? Fuera de alcance.

## Relacionado

[[Mancomunidad]] · [[Multi-tenencia]] · [[Visión y alcance]] · [[Modelo de datos]] · [[API REST]]
