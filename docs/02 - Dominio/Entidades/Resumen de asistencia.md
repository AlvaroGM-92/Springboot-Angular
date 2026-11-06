---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-03
tags:
  - dominio
  - entidad
  - asistencia
  - retencion
  - multi-tenant
---

# Resumen de asistencia

## Propósito

Conserva los contadores de asistencia de un alumno por asignatura y curso cuando la purga por retención elimina las asistencias y su historial. Es la única traza que queda tras la purga y está **anonimizada**: no guarda `alumno_id`, solo un hash estable. Decisión registrada en [[ADR-019 - Resumen de asistencia y purga anonimizada]].

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant del resumen. |
| `alumno_hash` | CHAR(64) | Sí | SHA-256 de `mancomunidad_id:alumno_id:secreto`; no permite recuperar el identificador. |
| `asignatura_id` | BIGINT | Sí | FK a [[Asignatura]] de la misma mancomunidad. |
| `curso_escolar_id` | BIGINT | Sí | FK a [[Curso escolar]] de la misma mancomunidad. |
| `sesiones` | INT | Sí | Filas de asistencia computadas. |
| `presentes` | INT | Sí | Asistencias en estado `PRESENTE`. |
| `ausentes` | INT | Sí | Ausencias sin justificar. |
| `ausencias_justificadas` | INT | Sí | Ausencias justificadas. |
| `retrasos` | INT | Sí | Llegadas tarde. |
| `minutos_retraso` | INT | Sí | Suma de minutos de retraso. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Asignatura]]**.
- **N:1 con [[Curso escolar]]**.

No tiene FK a [[Alumno]]: la relación es pseudónima mediante `alumno_hash`.

## Reglas

- Se crea o actualiza **solo** en la purga por retención, a partir de las filas de [[Asistencia]] e [[Historial de asistencia]] que se van a borrar.
- La clave lógica es `(mancomunidad_id, alumno_hash, asignatura_id, curso_escolar_id)`. Si la matrícula se reactiva después de una purga y vuelve a causar baja, los contadores se acumulan con `INSERT ... ON DUPLICATE KEY UPDATE`.
- `sesiones` es la suma de `presentes + ausentes + ausencias_justificadas + retrasos`; las sesiones `CANCELADA` no cuentan.
- No contiene datos personales directos ni permite reconstruir la identidad sin el secreto del servidor.
- Es de solo lectura desde la aplicación; no se expone escritura en la API.
- Los contadores en vivo de matrículas activas se calculan desde `asistencia`; el resumen solo se consulta cuando la matrícula ya fue purgada.
- El resumen no se purga con la misma regla: se conserva como histórico estadístico anonimizado.
- La asignatura y el curso deben pertenecer a la misma mancomunidad.

## Dudas

- ¿Se conserva el `alumno_hash` para siempre? Sí, mientras exista el resumen; no se puede revertir y no identifica por sí solo.
- ¿Se incluyen los presentes? Sí, para que el total de sesiones cuadre.

## Relacionado

[[Mancomunidad]] · [[Asignatura]] · [[Curso escolar]] · [[Alumno]] · [[Asistencia]] · [[Historial de asistencia]] · [[Matrícula]] · [[Modelo de datos]] · [[ADR-007 - Historial de cambios de asistencia]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]]
