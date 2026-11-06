---
tipo: adr
estado: aceptada
fecha: 2026-10-27
actualizado: 2026-11-04
tags:
  - adr
  - asistencia
  - auditoria
---

# ADR-007 — Historial de cambios de asistencia

## Contexto

El profesor debe poder corregir la asistencia **en cualquier momento**, sin ventanas de bloqueo. Esa flexibilidad es necesaria en el día a día (errores al pasar lista, justificaciones que llegan después, retrasos comunicados más tarde), pero abre la puerta a cambios sin traza. La administración necesita poder auditar qué se cambió, cuándo y quién lo hizo, sin que el profesor vea ese historial.

## Decisión

- La [[Asistencia]] es **editable siempre** por el profesor asignado a la sesión: el titular o, durante una sustitución activa, el sustituto ([[ADR-025 - Sustituciones y cambio de titular]]).
- Cada cambio (creación, edición o corrección) genera una fila **inmutable** en [[Historial de asistencia]] con:
  - valores anterior y nuevo de estado, minutos de retraso y motivo;
  - autor del cambio (`cambiado_por`);
  - fecha y hora (`cambiado_at`);
  - origen (`CREACION`, `EDICION`, `CORRECCION`).
- El historial es **solo inserción y consulta**: no se actualiza ni se borra desde la aplicación.
- **Solo el administrador** puede consultar el historial.
- El profesor **no ve** el historial en su interfaz; sigue viendo y editando la asistencia actual.
- La administración **no puede editar** asistencias; solo consultarlas y ver su historial.
- El registro se escribe en la misma transacción que el cambio de asistencia, para que no existan modificaciones sin traza.
- **Retención:** el historial se conserva mientras la matrícula del alumno esté activa y **2 años** desde su baja. Después se purga de forma programada, conservando solo contadores agregados y anonimizados.

## Consecuencias

### Positivas

- Se elimina la disyuntiva entre «bloquear la edición» y «perder la traza».
- La administración puede reconstruir la evolución de una asistencia.
- El profesor trabaja sin fricción y sin ver información que no necesita.
- La auditoría es específica del dominio, más legible que un log genérico.

### Negativas / compromisos

- Cada edición añade una fila; hay que prever el crecimiento y ejecutar la purga por retención.
- Hay que garantizar que ningún camino de código actualice la asistencia sin escribir el historial.
- La tabla es inmutable: una corrección del propio historial requeriría una intervención excepcional fuera de la aplicación.
- La purga debe ejecutarse por mancomunidad y dejar contadores agregados antes de borrar.
- La consulta del historial añade pantallas y filtros solo para administración.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Bloquear la edición tras X horas | Impide corregir errores reales y obliga a excepciones manuales. |
| Guardar solo la última modificación en la asistencia | Se pierde la traza y no se sabe qué cambió. |
| Usar un log de aplicación genérico | Difícil de consultar por alumno o sesión y mezclado con ruido técnico. |
| Permitir al profesor ver el historial | El requisito indica que no debe verlo; además añade complejidad de interfaz. |
| Permitir al administrador editar la asistencia | Rompe la separación de responsabilidades y complica la autoría del cambio. |

> [!info] Actualización 2026-11-03
> La retención y purga se concretan en [[ADR-019 - Resumen de asistencia y purga anonimizada]]: las asistencias e historial se purgan a los 2 años de la baja y solo quedan los contadores anonimizados de [[Resumen de asistencia]]. La cancelación de una sesión no borra ni altera asistencias ya registradas ([[ADR-017 - Cancelación de sesiones y días no lectivos]]).

## Relacionado

[[Asistencia]] · [[Historial de asistencia]] · [[Resumen de asistencia]] · [[Matrícula]] · [[Sesión de clase]] · [[Roles y permisos]] · [[Modelo de datos]] · [[ADR-004 - Horario gestionado por el profesor]] · [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]]
