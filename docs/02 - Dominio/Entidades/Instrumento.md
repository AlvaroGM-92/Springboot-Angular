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

# Instrumento

## Propósito

Catálogo de instrumentos musicales de la mancomunidad. Se alimenta del uso: al escribir un instrumento nuevo en la ficha de un alumno se crea, y los existentes se reutilizan mediante sugerencias. Es un **módulo opcional**: solo está activo si alguna asignatura activa de la mancomunidad es instrumental ([[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]]).

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; catálogo propio de cada mancomunidad. |
| `nombre` | VARCHAR(80) | Sí | Único dentro de la mancomunidad, normalizado. |
| `activo` | BOOLEAN | Sí | Por defecto `true`. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:M con [[Alumno]]** a través de `alumno_instrumento`.
- La tabla intermedia guarda `mancomunidad_id`, `alumno_id`, `instrumento_id` y `created_at`.

## Reglas

- El catálogo es **por mancomunidad**: dos mancomunidades pueden tener un instrumento con el mismo nombre sin compartirlo.
- El nombre se normaliza: recortar espacios, colapsar espacios internos y capitalizar.
- No se permiten duplicados por nombre dentro de la misma mancomunidad (comparación sin distinguir mayúsculas): `UNIQUE (mancomunidad_id, nombre)`.
- Un alumno solo puede asociarse a instrumentos de su misma mancomunidad.
- Un alumno puede tener como máximo 10 instrumentos.
- Un nombre de instrumento puede tener como máximo 80 caracteres.
- Al guardar un alumno, los instrumentos nuevos se crean y los existentes se reutilizan en una sola transacción.
- Si un instrumento inactivo se vuelve a usar, se reactiva automáticamente.
- No se borra físicamente: se desactiva o se fusiona con otro.
- No se puede desactivar un instrumento con alumnos activos asociados.
- Al renombrar a un nombre existente se fusionan ambos instrumentos y se reasignan las relaciones.
- El módulo está activo si existe al menos una asignatura activa con `requiere_instrumento = true`; el tipo de la mancomunidad no lo activa por sí solo.
- Si el módulo está inactivo, la interfaz oculta el catálogo y la sección del alumno, y la API rechaza altas, ediciones y consultas de instrumentos con `422`.
- Ocultar el módulo **no borra** instrumentos ni asociaciones; si vuelve a activarse, reaparecen tal cual.
- El catálogo solo se gestiona desde la administración de la mancomunidad; el profesor no lo modifica.

## Dudas

- ¿Se permite que administración cree instrumentos manualmente sin asignarlos? Sí, como comodidad, pero el flujo principal es el alta desde el alumno.
- ¿Se guarda una categoría (cuerda, viento…)? Fuera de alcance inicial.
- ~~¿El módulo está siempre disponible?~~ **Resuelto: no; depende de que exista alguna asignatura activa instrumental.**

## Relacionado

[[Mancomunidad]] · [[Multi-tenencia]] · [[Asignatura]] · [[Alumno]] · [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]] · [[Modelo de datos]] · [[API REST]]
