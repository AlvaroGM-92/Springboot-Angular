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

# Historial de asistencia

## Propósito

Guarda cada cambio realizado sobre una [[Asistencia]] para que la administración pueda auditar quién cambió qué y cuándo. El profesor puede editar la asistencia en cualquier momento, pero **no ve este historial**; solo lo consulta la administración.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant del historial. |
| `asistencia_id` | BIGINT | Sí | FK a [[Asistencia]] de la misma mancomunidad. |
| `estado_anterior` | VARCHAR(25) | No | Nulo en la creación inicial. |
| `estado_nuevo` | VARCHAR(25) | Sí | Estado resultante. |
| `minutos_retraso_anterior` | SMALLINT | No | Valor anterior, si aplica. |
| `minutos_retraso_nuevo` | SMALLINT | No | Valor nuevo, si aplica. |
| `motivo_anterior` | VARCHAR(255) | No | Motivo anterior. |
| `motivo_nuevo` | VARCHAR(255) | No | Motivo nuevo. |
| `cambiado_por` | BIGINT | Sí | FK a [[Usuario]] que realiza el cambio. |
| `cambiado_at` | DATETIME | Sí | Momento del cambio. |
| `origen` | VARCHAR(20) | Sí | `CREACION`, `EDICION` o `CORRECCION`. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Asistencia]]**.
- **N:1 con [[Usuario]]** (autor del cambio).

## Reglas

- Se crea una fila por cada modificación de estado, minutos o motivo.
- El historial y la asistencia deben pertenecer a la misma mancomunidad.
- Solo se consulta desde la mancomunidad activa; el aislamiento se aplica también a la administración.
- La creación inicial de la asistencia también puede registrarse con `origen = CREACION`.
- El historial es **inmutable**: no se edita ni se borra desde la aplicación, salvo la purga programada por retención.
- Solo el administrador puede consultarlo.
- El profesor no ve el historial en su interfaz.
- El profesor puede editar la asistencia en cualquier momento; el cambio queda registrado.
- La administración no puede modificar la asistencia, solo consultar el historial.
- **Retención:** el historial se conserva mientras la matrícula del alumno en la asignatura esté `ACTIVA` y durante **2 años** desde su baja (`matricula.baja_at`). La fecha programada se guarda en `matricula.purga_at`.
- Pasado el plazo, un proceso programado por mancomunidad agrega los contadores en [[Resumen de asistencia]] con `alumno_hash`, purga el historial y las asistencias asociadas y marca `matricula.purgada_at`. Solo quedan los contadores anonimizados por alumno, asignatura y curso ([[ADR-019 - Resumen de asistencia y purga anonimizada]]).
- La purga no borra sesiones, matrículas, observaciones ni preparaciones.
- La reactivación de la matrícula antes del plazo cancela la purga y conserva la traza.
- **Supresión frente a retención:** si se solicita borrar un historial sujeto a retención legal (asistencia, jornada, auditoría), no se borra; se responde con el motivo, el plazo legal y, cuando sea posible, se anonimizan los datos personales sin perder la traza agregada. La decisión se registra en la auditoría si interviene el soporte.

## Consulta

- Desde la ficha del alumno, la administración puede ver la evolución de sus asistencias.
- Desde una sesión, puede ver los cambios realizados en cada alumno.
- Los filtros previstos son alumno, asignatura, sesión, profesor y rango de fechas.

## Dudas

- ~~¿Se registra también la creación inicial o solo las ediciones?~~ **Resuelto: se registra la creación, la edición y la corrección, para tener la traza completa.**
- ¿Se guarda el valor completo anterior o solo los campos modificados? Se guardan los campos relevantes.
- ~~¿Se puede purgar el historial por antigüedad?~~ **Resuelto: se conserva mientras la matrícula está activa y 2 años desde la baja; después se purga y los contadores quedan en [[Resumen de asistencia]] con `alumno_hash`.**
- ~~¿Qué queda exactamente después de la purga?~~ **Resuelto: contadores anonimizados por alumno, asignatura y curso; no se guardan motivos ni datos personales.** Véase [[ADR-019 - Resumen de asistencia y purga anonimizada]].
- ~~¿Qué se responde si se pide borrar un dato que hay que conservar?~~ **Resuelto: no se borra; se informa del motivo y del plazo legal y se anonimiza lo posible, registrando la decisión en la auditoría.** Véase [[Privacidad y tratamiento de datos]].

## Relacionado

[[Mancomunidad]] · [[Multi-tenencia]] · [[Asistencia]] · [[Sesión de clase]] · [[Matrícula]] · [[Resumen de asistencia]] · [[Usuario]] · [[Modelo de datos]] · [[ADR-007 - Historial de cambios de asistencia]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]]
