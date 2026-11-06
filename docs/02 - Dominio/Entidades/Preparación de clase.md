---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-03
tags:
  - dominio
  - entidad
  - docencia
  - multi-tenant
---

# Preparación de clase

## Propósito

Planificación docente de una clase concreta. Permite al profesor preparar objetivos, contenidos, recursos y observaciones, y a la administración supervisar la planificación en modo lectura.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant de la preparación. |
| `profesor_id` | BIGINT | Sí | FK a [[Usuario]]; autor con pertenencia activa en esa mancomunidad. |
| `asignatura_id` | BIGINT | Sí | FK a [[Asignatura]] que imparte, en la misma mancomunidad. |
| `curso_escolar_id` | BIGINT | Sí | FK a [[Curso escolar]] de la misma mancomunidad. |
| `sesion_id` | BIGINT | No | FK a [[Sesión de clase]] de la misma mancomunidad; vínculo opcional. |
| `fecha` | DATE | Sí | Puede ser futura. |
| `titulo` | VARCHAR(150) | Sí | Tema de la clase. |
| `objetivos` | TEXT | No | Qué se quiere conseguir. |
| `contenido` | TEXT | Sí | Contenido y actividades. |
| `recursos` | TEXT | No | Materiales necesarios. |
| `observaciones` | TEXT | No | Notas del profesor. |
| `estado` | VARCHAR(20) | Sí | `BORRADOR`, `PREPARADA` o `IMPARTIDA`. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Usuario]]**.
- **N:1 con [[Asignatura]]**.
- **N:1 con [[Curso escolar]]**.
- **N:1 con [[Sesión de clase]]** (opcional).

## Reglas

- Profesor, asignatura, curso y sesión deben pertenecer a la misma mancomunidad.
- El profesor solo prepara asignaturas que imparte.
- Si se vincula a una sesión, esta debe ser suya y de la misma asignatura.
- Solo se puede eliminar una preparación en estado `BORRADOR`.
- Las preparaciones `PREPARADA` e `IMPARTIDA` se conservan como histórico.
- La administración puede ver todas, incluidas las de borrador, pero no editarlas.
- No puede haber dos preparaciones para la misma sesión.
- El estado `IMPARTIDA` solo tiene sentido con fecha pasada o presente.

## Dudas

- ¿Se puede duplicar una preparación para otra fecha? Podría ser una mejora posterior.
- ¿Se adjuntan archivos? Fuera de alcance.
- ¿Se permite preparar sin sesión? Sí, con asignatura + fecha.

## Relacionado

[[Mancomunidad]] · [[Multi-tenencia]] · [[Sesión de clase]] · [[Asignatura]] · [[Usuario]] · [[Modelo de datos]]
