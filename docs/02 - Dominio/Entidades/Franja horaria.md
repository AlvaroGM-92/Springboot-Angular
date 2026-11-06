---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-03
tags:
  - dominio
  - entidad
  - horarios
  - multi-tenant
---

# Franja horaria

## Propósito

Bloque de clase semanal: qué asignatura se imparte, qué día, en qué horario y en qué aula. Cada franja pertenece al [[Horario del profesor]] de un curso escolar.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant de la franja. |
| `horario_profesor_id` | BIGINT | Sí | FK a [[Horario del profesor]] de la misma mancomunidad. |
| `asignatura_id` | BIGINT | Sí | FK a [[Asignatura]] que imparte el profesor, en la misma mancomunidad. |
| `dia_semana` | TINYINT | Sí | 1 = lunes … 7 = domingo. |
| `hora_inicio` | TIME | Sí | Formato `HH:mm`. |
| `hora_fin` | TIME | Sí | Posterior a `hora_inicio`. |
| `aula` | VARCHAR(100) | No | Texto libre. |
| `creado_por` | BIGINT | Sí | FK a [[Usuario]] que creó la franja (profesor titular o administrador). |
| `actualizado_por` | BIGINT | Sí | FK a [[Usuario]] de la última modificación. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Horario del profesor]]** (que aporta profesor y curso).
- **N:1 con [[Asignatura]]**.
- **1:N con [[Sesión de clase]]**.
- **1:N con [[Solicitud de cambio de horario]]**.

## Reglas

- La franja, el horario, la asignatura y el curso deben pertenecer a la misma mancomunidad.
- La hora de fin debe ser posterior a la de inicio.
- No hay duración mínima. La mancomunidad puede definir un máximo opcional; si existe, la franja no puede superarlo.
- No puede haber solapamiento de franjas del mismo profesor en el mismo curso escolar.
- No puede haber solapamiento del mismo grupo (centro + curso + grupo) en el mismo curso escolar.
- No puede haber una franja que solape con otra de distinta asignatura si comparten al menos un alumno con matrícula `ACTIVA` en el mismo curso; es el **conflicto de alumno** ([[ADR-021 - Validación de conflictos de alumno]]).
- El conflicto de alumno se valida al crear o editar la franja y al finalizar el horario. Una matrícula o reactivación también se rechaza si genera el conflicto contra un horario `FINALIZADO`.
- La asignatura debe estar activa y asignada al profesor.
- Mientras el horario está en `BORRADOR`, pueden crear, editar y borrar franjas el profesor titular y cualquier administrador de la misma mancomunidad.
- Una vez el horario está `FINALIZADO`, los cambios se canalizan por [[Solicitud de cambio de horario]] o por una reapertura administrativa con motivo.
- El administrador puede editar franjas en `BORRADOR`, pero no directamente sobre un horario finalizado.
- `creado_por` y `actualizado_por` registran la autoría de cada alta y modificación; no se exige motivo en la edición normal.
- Los solapamientos se validan en el servicio dentro de una transacción.
- Al borrar una franja, sus sesiones `PROGRAMADA` sin asistencia se eliminan y las que ya tienen lista se conservan con `franja_id` nulo ([[ADR-020 - Borrado de franjas y conservación de sesiones]]).
- No se validan solapamientos del mismo profesor **entre mancomunidades distintas**: la identidad es global, pero el horario está aislado por tenant y esa comprobación no se hace en la v1 ([[ADR-022 - Conflictos de profesor entre mancomunidades]]).
- Las franjas de asignaturas o profesores inactivos se conservan como histórico y no se editan.

## Estado

La franja no tiene estado propio: hereda el de su [[Horario del profesor]] (`BORRADOR` o `FINALIZADO`). Así se evita que una parte del horario esté finalizada y otra en borrador.

## Dudas

- ~~¿Se permiten clases de más de X horas?~~ **Resuelto: sin mínimo; el máximo es opcional por mancomunidad y, si no se define, solo se valida que el fin sea posterior al inicio.**
- ¿Se permite el domingo? Sí: el requisito indica de lunes a domingo.
- ¿El aula debe ser catálogo? Confirmado: **texto libre** en la v1.
- ~~¿Se valida que un alumno no tenga dos clases a la misma hora?~~ **Resuelto: sí, aunque las asignaturas sean de grupos distintos; se valida al crear o editar la franja, al finalizar y al matricular.** Véase [[ADR-021 - Validación de conflictos de alumno]].
- ~~¿Se valida el solapamiento de un profesor en dos mancomunidades?~~ **Resuelto: no en la v1; el aislamiento impide consultar horarios de otro tenant.** Véase [[ADR-022 - Conflictos de profesor entre mancomunidades]].
- ~~¿Qué pasa con las sesiones si se borra la franja?~~ **Resuelto: las `PROGRAMADA` sin asistencia se eliminan y las que tienen lista se conservan con `franja_id` nulo.** Véase [[ADR-020 - Borrado de franjas y conservación de sesiones]].

## Relacionado

[[Mancomunidad]] · [[Multi-tenencia]] · [[Horario del profesor]] · [[Asignatura]] · [[Usuario]] · [[Sesión de clase]] · [[Solicitud de cambio de horario]] · [[Matrícula]] · [[ADR-016 - Horario editable por profesor y administrador]] · [[ADR-020 - Borrado de franjas y conservación de sesiones]] · [[ADR-021 - Validación de conflictos de alumno]] · [[ADR-022 - Conflictos de profesor entre mancomunidades]] · [[Modelo de datos]]
