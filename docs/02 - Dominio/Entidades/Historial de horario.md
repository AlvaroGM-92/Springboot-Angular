---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-04
tags:
  - dominio
  - entidad
  - horarios
  - auditoria
  - multi-tenant
---

# Historial de horario

## Propósito

Registra los cambios de estado de un [[Horario del profesor]]: su creación, cada finalización y cada reapertura administrativa. Permite saber quién creó, finalizó o reabrió un horario, cuándo y por qué, sin mezclar esa traza con las franjas ni con las solicitudes.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant del historial. |
| `horario_profesor_id` | BIGINT | Sí | FK a [[Horario del profesor]] de la misma mancomunidad. |
| `origen_horario_id` | BIGINT | No | FK a [[Horario del profesor]] del curso anterior; obligatorio en `COPIA`, nulo en el resto. |
| `accion` | VARCHAR(20) | Sí | `CREACION`, `FINALIZACION`, `REAPERTURA` o `COPIA`. |
| `estado_anterior` | VARCHAR(20) | No | Nulo en la creación. |
| `estado_nuevo` | VARCHAR(20) | Sí | `BORRADOR` o `FINALIZADO`. |
| `motivo` | VARCHAR(500) | No | Obligatorio en `REAPERTURA`; entre 10 y 500 caracteres. |
| `realizado_por` | BIGINT | Sí | FK a [[Usuario]] que provoca el cambio. |
| `realizado_at` | DATETIME | Sí | Momento del cambio. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Horario del profesor]]**.
- **N:1 con [[Usuario]]** (autor del cambio).

## Reglas

- El historial y el horario deben pertenecer a la misma mancomunidad.
- Es **inmutable**: solo inserción y consulta; no se edita ni se borra desde la aplicación.
- Cada transición de estado escribe exactamente una fila en la misma transacción que el cambio.
- `CREACION` se registra al crear el horario con la primera franja; `estado_anterior` es nulo y `realizado_por` es el profesor titular o el administrador que la creó.
- `FINALIZACION` la puede provocar el profesor titular o un administrador de la mancomunidad.
- `REAPERTURA` solo la provoca un administrador, únicamente sobre un horario `FINALIZADO` y con motivo.
- `COPIA` se registra al crear un horario nuevo desde otro curso, con `origen_horario_id` y el autor; `estado_anterior` es nulo y `estado_nuevo` es `BORRADOR` ([[ADR-023 - Copia de horario al curso siguiente]]).
- La edición de franjas en `BORRADOR` no genera filas aquí: su autoría vive en `creado_por` y `actualizado_por` de [[Franja horaria]] y del propio horario.
- Un horario puede finalizarse y reabrirse varias veces; todas las transiciones quedan registradas.
- Solo el administrador consulta la traza completa. El profesor ve el estado actual y el motivo de la última reapertura.

## Consulta

- Desde el detalle de un horario, la administración ve la secuencia de estados, autores, fechas y motivos.
- Los filtros previstos son profesor, curso escolar, acción y rango de fechas.
- La vista del profesor muestra el estado actual, `finalizado_at` y, si procede, la fecha y el motivo de la última reapertura.

## Dudas

- ¿Debe el profesor recibir una notificación al reabrirse su horario? En la v1 se muestra en la interfaz; no hay notificaciones externas.
- ¿Se registra cada solicitud aprobada que modifica una franja? No: eso queda en [[Solicitud de cambio de horario]]; este historial cubre solo los estados del horario.

## Relacionado

[[Mancomunidad]] · [[Multi-tenencia]] · [[Horario del profesor]] · [[Franja horaria]] · [[Solicitud de cambio de horario]] · [[Usuario]] · [[ADR-004 - Horario gestionado por el profesor]] · [[ADR-016 - Horario editable por profesor y administrador]] · [[ADR-023 - Copia de horario al curso siguiente]] · [[Modelo de datos]]
