---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-03
tags:
  - dominio
  - entidad
  - calendario
  - multi-tenant
---

# Día no lectivo

## Propósito

Marca una fecha sin actividad lectiva para toda la mancomunidad o para uno de sus centros. El generador de sesiones la consulta para no crear clases y, si ya existían sesiones `PROGRAMADA` sin asistencia, las cancela. Decisión registrada en [[ADR-017 - Cancelación de sesiones y días no lectivos]].

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant del calendario. |
| `curso_escolar_id` | BIGINT | Sí | FK a [[Curso escolar]] de la misma mancomunidad. |
| `fecha` | DATE | Sí | Debe estar entre `fecha_inicio` y `fecha_fin` del curso. |
| `centro_id` | BIGINT | No | FK a [[Centro]] de la misma mancomunidad; nulo = toda la mancomunidad. |
| `nombre` | VARCHAR(120) | Sí | Motivo legible: «Festivo local», «Vacaciones de Navidad»… |
| `creado_por` | BIGINT | Sí | FK a [[Usuario]] que registra el día. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Curso escolar]]**.
- **N:1 con [[Centro]]** (opcional; nulo = toda la mancomunidad).

## Reglas

- Un día no lectivo global (`centro_id` nulo) afecta a todos los centros de la mancomunidad; uno con centro solo afecta a las asignaturas de ese centro.
- No puede haber dos registros con la misma `mancomunidad_id`, `curso_escolar_id`, `fecha` y `centro_id`. La unicidad con `centro_id` nulo se valida en el servicio, porque MySQL permite varios `NULL`.
- Si ya existe un día global para una fecha, no se crean días por centro para esa misma fecha: son redundantes.
- La fecha debe pertenecer al curso escolar indicado.
- Solo la administración crea, edita y elimina días no lectivos.
- Al crear un día no lectivo, las sesiones `PROGRAMADA` de esa fecha y ámbito que aún no tienen asistencia pasan a `CANCELADA` con motivo «Día no lectivo». Las sesiones `ABIERTA` o `CERRADA` con asistencia no se tocan.
- Si se elimina un día no lectivo antes de la fecha, el generador volverá a crear las sesiones `PROGRAMADA` en la siguiente ejecución. Las sesiones ya canceladas no se reactivan automáticamente.
- En un curso `CERRADO` el calendario es de solo lectura: no se añaden ni se eliminan días.
- El calendario no sustituye al horario: no define franjas ni horas, solo excluye fechas.

## Dudas

- ¿Se admiten festivos por turno o por grupo? No en la v1; el ámbito es mancomunidad o centro.
- ¿Se importa el calendario oficial? No en la v1; se registra a mano.

## Relacionado

[[Mancomunidad]] · [[Curso escolar]] · [[Centro]] · [[Sesión de clase]] · [[Modelo de datos]] · [[API REST]] · [[ADR-017 - Cancelación de sesiones y días no lectivos]]
