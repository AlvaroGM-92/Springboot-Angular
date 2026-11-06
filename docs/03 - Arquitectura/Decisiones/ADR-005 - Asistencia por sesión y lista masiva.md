---
tipo: adr
estado: aceptada
fecha: 2026-10-27
actualizado: 2026-10-27
tags:
  - adr
  - asistencia
---

# ADR-005 — Asistencia por sesión y lista masiva

## Contexto

El profesor necesita marcar faltas y retrasos de varios alumnos a la vez, no uno a uno. Además, la administración quiere consultar el histórico por alumno y asignatura.

## Decisión

- Se introduce **Sesión de clase** como contenedor de una clase concreta (asignatura + fecha + franja opcional).
- Al abrir la sesión se cargan todos los alumnos matriculados con estado `PRESENTE`.
- El profesor modifica en una sola pantalla los estados y guarda **toda la lista en una operación**.
- Se guarda una fila por alumno y sesión, incluidos los presentes.
- La asistencia se puede **editar en cualquier momento**, sin ventana de bloqueo.
- Cada cambio queda registrado en el [[Historial de asistencia]], visible solo para la administración (ver [[ADR-007 - Historial de cambios de asistencia]]).
- La administración consulta asistencias en modo lectura y ve contadores; no las edita.

## Consecuencias

### Positivas

- Pasar lista es rápido y refleja la realidad de una clase.
- Se evita el registro individual y los duplicados.
- Los contadores y el histórico son fiables.
- La sesión puede vincularse a una preparación de clase.
- Se pueden corregir errores sin perder la traza.

### Negativas / compromisos

- Una tabla y un concepto más que mantener.
- Se almacenan muchas filas (una por alumno y sesión).
- Cada edición añade filas al historial y hay que prever su conservación.
- El modelo debe impedir asistencias de alumnos no matriculados.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Registrar solo faltas y retrasos | No deja constancia de que se pasó lista y complica los contadores. |
| Una fila por incidencia sin sesión | Duplicados y difícil de auditar. |
| Lista en memoria sin persistir presentes | Se pierde el histórico y la trazabilidad. |

## Relacionado

[[Sesión de clase]] · [[Asistencia]] · [[Historial de asistencia]] · [[Preparación de clase]] · [[Requisitos funcionales]] · [[ADR-007 - Historial de cambios de asistencia]] · [[ADR-013 - Generación automática de sesiones desde el horario]]
