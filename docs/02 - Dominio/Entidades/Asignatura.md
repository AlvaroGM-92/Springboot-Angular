---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-04
tags:
  - dominio
  - entidad
  - multi-tenant
---

# Asignatura

## Propósito

Materia impartida en un centro, con un nivel y un grupo. Es la unidad a la que se matriculan los alumnos y a la que se asocian profesores y franjas horarias. Su tipo puede ser de música, danza, artes escénicas, idiomas, deportes u otro, con independencia del tipo de la mancomunidad.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant de la asignatura. |
| `centro_id` | BIGINT | Sí | FK a [[Centro]] activo de la misma mancomunidad. |
| `nombre` | VARCHAR(150) | Sí | Ej. «Lenguaje musical» o «Inglés B2». |
| `curso` | VARCHAR(50) | Sí | Nivel educativo. |
| `grupo` | VARCHAR(20) | Sí | Ej. «A», «B». |
| `tipo` | VARCHAR(30) | Sí | `MUSICA`, `DANZA`, `ARTES_ESCENICAS`, `IDIOMAS`, `DEPORTES` u `OTRO`; no depende del tipo de la mancomunidad. |
| `requiere_instrumento` | BOOLEAN | Sí | Por defecto `false`; solo puede ser `true` si `tipo = MUSICA`. |
| `activo` | BOOLEAN | Sí | Por defecto `true`. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Centro]]**.
- **N:M con [[Usuario]]** (profesores) a través de `asignatura_profesor`.
- **1:N con [[Matrícula]]**.
- **1:N con [[Franja horaria]]**.
- **1:N con [[Preparación de clase]]**.
- **1:N con [[Sustitución]]**.

## Reglas

- No puede existir duplicado de `mancomunidad + centro + nombre + curso + grupo`.
- El centro debe pertenecer a la misma mancomunidad que la asignatura.
- Solo los usuarios con una pertenencia activa `PROFESOR` en esa mancomunidad pueden asignarse a la asignatura.
- No se puede desactivar una asignatura con matrículas activas.
- Un profesor solo puede crear franjas y preparaciones de asignaturas que imparte.
- Una asignatura inactiva conserva su histórico y no se ofrece para nuevas matrículas.
- El `tipo` de la asignatura es obligatorio y se elige de la lista cerrada compartida con la mancomunidad.
- `requiere_instrumento` solo puede ser `true` cuando `tipo = MUSICA`; una asignatura musical no instrumental, como «Lenguaje musical» o «Coro», lo deja en `false`.
- El módulo de instrumentos de la mancomunidad se activa si existe al menos una asignatura activa con `requiere_instrumento = true`; si no, se oculta en la interfaz y la API rechaza operaciones de instrumentos con `422` ([[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]]).
- Activar o desactivar una asignatura instrumental puede cambiar la visibilidad del módulo; el frontend debe refrescar `GET /configuracion`.
- Una asignatura puede tener sustituciones puntuales de su profesor titular, con periodo y motivo; la sustitución no cambia la titularidad ni el horario ([[ADR-025 - Sustituciones y cambio de titular]]).
- No se puede crear una sustitución para una asignatura inactiva ni para un curso cerrado.

## Dudas

- ¿Un profesor puede impartir una asignatura en varios centros? Sí, mediante asignaturas distintas.
- ¿El «curso» es el nivel (1º, 2º) o el curso escolar? Aquí es el **nivel**; el curso escolar se referencia desde matrículas, horarios y preparaciones.
- ~~¿El tipo de la mancomunidad condiciona el de la asignatura?~~ **Resuelto: no; cada asignatura elige el suyo y la mancomunidad puede mezclar áreas.**

## Relacionado

[[Mancomunidad]] · [[Multi-tenencia]] · [[Centro]] · [[Usuario]] · [[Matrícula]] · [[Franja horaria]] · [[Instrumento]] · [[Sustitución]] · [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[Modelo de datos]]
