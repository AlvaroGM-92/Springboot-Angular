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

# Matrícula

## Propósito

Vincula un [[Alumno]] con una [[Asignatura]] durante un [[Curso escolar]]. Permite saber qué alumnos pertenecen a cada asignatura y conservar el histórico.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant de la matrícula. |
| `alumno_id` | BIGINT | Sí | FK a [[Alumno]] de la misma mancomunidad. |
| `asignatura_id` | BIGINT | Sí | FK a [[Asignatura]] activa de la misma mancomunidad. |
| `curso_escolar_id` | BIGINT | Sí | FK a [[Curso escolar]] de la misma mancomunidad. |
| `estado` | VARCHAR(20) | Sí | `ACTIVA` o `BAJA`. |
| `baja_at` | DATETIME | No | Fecha de la baja; nula mientras está activa y se limpia si se reactiva. |
| `purga_at` | DATETIME | No | Fecha programada de purga: `baja_at + 2 años`; nula mientras está activa. |
| `purgada_at` | DATETIME | No | Momento en que se ejecutó la purga; nulo si no se ha purgado. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Alumno]]**.
- **N:1 con [[Asignatura]]**.
- **N:1 con [[Curso escolar]]**.

## Reglas

- No puede haber dos matrículas del mismo alumno en la misma asignatura y curso.
- Alumno, asignatura y curso escolar deben pertenecer a la misma mancomunidad.
- Solo se puede matricular en asignaturas activas del mismo centro que el alumno.
- Solo se puede matricular en el curso escolar activo; los cursos anteriores son históricos.
- La baja cambia el estado a `BAJA` y registra `baja_at`; no borra la fila.
- Al dar de baja se calcula `purga_at = baja_at + 2 años`; al reactivar se limpian `purga_at` y `purgada_at`.
- La baja **no borra** asistencias, historial ni filas de sesiones ya abiertas; solo excluye al alumno de las listas que se abran a partir de la fecha de baja ([[ADR-026 - Baja de alumno a mitad de curso]]).
- La reactivación vuelve a `ACTIVA` y limpia `baja_at`, `purga_at` y `purgada_at`.
- La reactivación vuelve a incluir al alumno en las listas de sesiones que se abran a partir de ese momento; no añade filas a sesiones ya abiertas.
- La retención del historial de asistencia se calcula desde `baja_at` y se materializa en `purga_at`: se conserva mientras la matrícula está activa y 2 años después de la baja. Véase [[Historial de asistencia]] y [[ADR-019 - Resumen de asistencia y purga anonimizada]].
- Una matrícula o reactivación no puede generar un **conflicto de alumno** con una franja de un horario `FINALIZADO` del mismo curso; si el horario está en `BORRADOR`, el conflicto se valida al finalizarlo ([[ADR-021 - Validación de conflictos de alumno]]).
- Un alumno con matrículas activas no puede cambiar de centro.
- Las asignaturas inactivas no se ofrecen para nuevas matrículas, pero conservan las existentes.

## Dudas

- ~~¿Se permite volver a matricular una baja en el mismo curso?~~ **Resuelto: se reactiva la matrícula existente, que vuelve a `ACTIVA`; no se crea otra.**
- ¿Una matrícula puede pertenecer a un curso no activo? Solo por importación histórica; no desde la interfaz.
- ~~¿Cómo se sabe cuándo toca purgar?~~ **Resuelto: con `purga_at` y `purgada_at`; la reactivación los limpia y la purga los usa para no repetir.** Véase [[ADR-019 - Resumen de asistencia y purga anonimizada]].
- ~~¿Qué pasa con las listas y las sesiones ya abiertas al dar de baja?~~ **Resuelto: las sesiones ya abiertas conservan sus filas y el alumno se excluye de las listas futuras; no se borra nada.** Véase [[ADR-026 - Baja de alumno a mitad de curso]].

## Relacionado

[[Mancomunidad]] · [[Multi-tenencia]] · [[Alumno]] · [[Asignatura]] · [[Curso escolar]] · [[Historial de asistencia]] · [[Resumen de asistencia]] · [[Modelo de datos]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]] · [[ADR-021 - Validación de conflictos de alumno]] · [[ADR-026 - Baja de alumno a mitad de curso]]
