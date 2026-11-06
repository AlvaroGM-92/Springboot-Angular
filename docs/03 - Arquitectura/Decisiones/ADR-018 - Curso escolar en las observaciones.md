---
tipo: adr
estado: aceptada
fecha: 2026-11-03
actualizado: 2026-11-03
tags:
  - adr
  - dominio
  - observaciones
  - curso
---

# ADR-018 — Curso escolar en las observaciones

## Contexto

Una [[Observación]] guardaba alumno, profesor, asignatura opcional y fecha, pero no el [[Curso escolar]]. La fecha permite inferir el curso casi siempre, pero dos cursos pueden solaparse en el calendario o una observación histórica puede quedar fuera de cualquier rango. Sin una referencia explícita, los informes por curso pueden mezclar observaciones y la purga o el cierre de curso no tienen una dimensión fiable.

## Decisión

- Se añade `observacion.curso_escolar_id` como FK **obligatoria** a [[Curso escolar]], de la misma mancomunidad.
- La `fecha` de la observación debe estar dentro del rango `fecha_inicio`–`fecha_fin` del curso.
- En la creación por API, `cursoEscolarId` es **opcional**: si no llega, el servicio resuelve el curso de la mancomunidad que contiene la fecha. Si no hay ninguno o hay más de uno, responde `422` y exige indicarlo.
- Si el cliente envía `cursoEscolarId`, se valida que exista, que sea de la misma mancomunidad y que contenga la fecha.
- Las observaciones de cursos `CERRADO` se conservan y se pueden consultar; no se crean nuevas en ellos salvo importación histórica.
- Los listados y la ficha del alumno aceptan el filtro `cursoEscolarId` y los informes agrupan por ese campo.
- La migración inicial rellena el curso de las observaciones existentes por la fecha; si alguna queda ambigua, se revisa antes de aplicar la restricción `NOT NULL`.
- El resto de reglas de [[Observación]] no cambia: el profesor crea y edita las suyas, la administración solo consulta.

## Consecuencias

### Positivas

- Los informes por curso son fiables y no dependen de interpretar fechas.
- La observación queda ligada a la misma dimensión temporal que matrículas, horarios y preparaciones.
- El cierre de curso y la consulta histórica tienen una clave estable.
- La API puede inferir el curso en el caso normal, sin obligar al cliente a enviarlo siempre.

### Negativas / compromisos

- Una columna y una FK más, con su índice.
- Hay que resolver y validar el curso en el servicio, y migrar las observaciones existentes.
- Una fecha fuera de todos los cursos exige intervención; no se acepta una observación «sin curso».

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Inferir el curso siempre por la fecha | Falla con cursos solapados o fechas fuera de rango y no deja una FK consultable. |
| Guardar solo el código `2026-2027` | Duplica información y pierde la integridad con la entidad de curso. |
| No asociar curso y filtrar por rango de fechas | Mezcla observaciones en informes y complica la consulta histórica. |
| Hacer el curso opcional | Permite observaciones huérfanas y no resuelve el problema de los informes. |

## Relacionado

[[Observación]] · [[Curso escolar]] · [[Alumno]] · [[Asignatura]] · [[Modelo de datos]] · [[API REST]] · [[Requisitos funcionales]] · [[ADR-008 - Curso escolar como entidad]]
