---
tipo: adr
estado: aceptada
fecha: 2026-11-03
actualizado: 2026-11-03
tags:
  - adr
  - horarios
  - conflictos
  - alumnos
---

# ADR-021 — Validación de conflictos de alumno

## Contexto

El horario validaba solapamientos del mismo profesor (`HOR-04`) y del mismo grupo (`HOR-05`), pero un alumno puede matricularse en asignaturas de **grupos distintos**. Dos asignaturas de grupos diferentes pueden tener franjas solapadas y el alumno tendría dos clases a la misma hora. El grupo no basta para detectarlo porque el conflicto es del alumno, no del grupo.

## Decisión

- Se define **conflicto de alumno** como dos franjas del mismo curso escolar, el mismo día y con intervalos de tiempo solapados que pertenecen a asignaturas distintas en las que al menos un alumno tiene una matrícula `ACTIVA`.
- Los intervalos se comparan como `[hora_inicio, hora_fin)`: si una franja termina justo cuando empieza la otra, no hay solape.
- La validación se ejecuta:
  - al crear o editar una franja, dentro de la transacción;
  - al finalizar el horario, como red de seguridad sobre todas sus franjas;
  - al crear o reactivar una matrícula, contra las franjas de horarios `FINALIZADO` del mismo curso. Si el horario está en `BORRADOR`, la comprobación se hará al finalizarlo.
- Si hay conflicto, la operación responde `422` con `conflictos`: alumno, asignatura, día, hora de inicio, hora fin y el horario afectado. No se guarda parcialmente.
- Solo se consideran matrículas `ACTIVA` y del mismo `curso_escolar_id`; alumnos, asignaturas y franjas deben ser de la misma mancomunidad.
- La consulta se optimiza obteniendo primero los alumnos de la asignatura y buscando después franjas solapadas de sus otras asignaturas. Se añaden índices por curso, día y hora, y por matrícula.
- No se validan conflictos de aula: el aula es texto libre y no un recurso reservable en la v1.
- Los conflictos de profesor entre mancomunidades se tratan en [[ADR-022 - Conflictos de profesor entre mancomunidades]].

## Consecuencias

### Positivas

- Se evita que un alumno tenga dos clases simultáneas aunque esté en grupos distintos.
- El error es explicable y muestra el conflicto concreto, no solo «hay un solapamiento».
- La validación al matricular cierra el caso de altas posteriores a la finalización del horario.
- La regla se prueba con dos mancomunidades y con alumnos de varios grupos.

### Negativas / compromisos

- La consulta de conflicto es más costosa que la de profesor o grupo porque recorre matrículas y franjas de varias asignaturas.
- Una matrícula nueva puede bloquearse por un horario ya finalizado; la administración debe resolverlo con una solicitud o una reapertura.
- El mensaje de error necesita datos adicionales (`conflictos`) y no cabe en el formato de error genérico.
- No cubre conflictos de aula ni de disponibilidad del profesor entre tenants.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Validar solo por grupo | No detecta alumnos que cursan asignaturas de grupos distintos. |
| Validar solo al finalizar | Permite guardar borradores imposibles y el error aparece tarde. |
| Avisar sin bloquear | El horario podría publicarse con un conflicto real y el alumno tendría dos clases. |
| Comprobar en el frontend | El backend es la única defensa fiable y el frontend no conoce todas las matrículas. |
| Incluir el aula como recurso | El aula es texto libre en la v1 y no tiene catálogo ni reserva. |

## Relacionado

[[Franja horaria]] · [[Horario del profesor]] · [[Matrícula]] · [[Alumno]] · [[Asignatura]] · [[Curso escolar]] · [[Modelo de datos]] · [[API REST]] · [[Requisitos funcionales]] · [[ADR-022 - Conflictos de profesor entre mancomunidades]]
