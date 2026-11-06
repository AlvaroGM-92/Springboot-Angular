---
tipo: adr
estado: aceptada
fecha: 2026-11-02
actualizado: 2026-11-02
tags:
  - adr
  - dominio
  - asignaturas
  - instrumentos
---

# ADR-015 — Tipos de enseñanza y módulo de instrumentos opcional

## Contexto

El proyecto nació describiendo mancomunidades de escuelas de música, y el modelo lo reflejaba en dos puntos: un catálogo de **instrumentos** por mancomunidad y una lista de instrumentos en la ficha del **alumno**. Ese diseño encaja con una escuela de música, pero no con una mancomunidad de idiomas, danza, deporte u otras enseñanzas.

El usuario confirmó que las mancomunidades pueden estar orientadas a **cualquier área** y que una misma mancomunidad puede **mezclar áreas**. Por tanto, el tipo no puede ser una restricción global ni el módulo de instrumentos puede estar siempre presente.

## Decisión

- Se añade `Mancomunidad.tipo`, una lista cerrada: `MUSICA`, `DANZA`, `ARTES_ESCENICAS`, `IDIOMAS`, `DEPORTES` y `OTRO`.
- El tipo de mancomunidad es **informativo**: etiqueta la mancomunidad y propone valores por defecto, pero **no limita** las asignaturas. Una mancomunidad de música puede ofertar inglés.
- Se añade `Asignatura.tipo`, con la misma lista cerrada y obligatorio.
- Se añade `Asignatura.requiere_instrumento` (booleano, por defecto `false`). Solo puede ser `true` cuando `tipo = MUSICA`; una asignatura de música no instrumental, como «Lenguaje musical», lo deja en `false`.
- El **módulo de instrumentos** está activo si existe al menos una asignatura activa con `requiere_instrumento = true`.
- Si el módulo está inactivo:
  - se ocultan el catálogo de instrumentos y la sección de instrumentos de la ficha del alumno;
  - la API rechaza operaciones de instrumentos con `422`;
  - **no se borran** los instrumentos ni las asociaciones existentes.
- Los instrumentos siguen asociados al **alumno**, no a la matrícula. El catálogo sigue siendo por mancomunidad.
- El tipo de mancomunidad se elige al crearla y solo un `SUPERADMIN` puede cambiarlo, con motivo y auditoría, desde el panel de plataforma. Cambiarlo no altera las asignaturas existentes.
- La API expone `GET /configuracion` con `tipoMancomunidad`, `duracionMaximaFranjaMin` y `modulos.instrumentos`. El frontend lo carga al iniciar sesión y lo refresca cuando cambian las asignaturas.
- La lista de tipos es cerrada y se amplía por migración; no es texto libre.

## Consecuencias

### Positivas

- El producto deja de estar atado a la música sin perder el caso musical, que sigue siendo el principal.
- Una mancomunidad mixta puede tener piano, lenguaje musical e inglés sin módulos duplicados.
- El módulo de instrumentos desaparece de las mancomunidades que no lo necesitan, sin ensuciar la interfaz.
- Los datos existentes se conservan si el módulo se oculta y reaparecen si vuelve a activarse.
- El tipo de mancomunidad sirve para etiquetas y valores por defecto sin convertirse en una jaula.

### Negativas / compromisos

- Hay dos campos nuevos en `asignatura` y uno en `mancomunidad`.
- La visibilidad del módulo depende de una consulta adicional (`EXISTS` sobre asignaturas activas instrumentales).
- El frontend debe refrescar la configuración cuando se activa o desactiva la última asignatura instrumental.
- El tipo de asignatura y el indicador pueden desincronizarse si no se validan juntos; la regla `tipo = MUSICA` lo evita.
- La lista cerrada exige una migración para añadir áreas nuevas.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Solo tipo de mancomunidad | Una mancomunidad mixta no podría tener asignaturas de varias áreas, o el módulo se activaría para todas. |
| Solo un interruptor de módulo | No describe el área y no permite decidir por asignatura. |
| Instrumento en la matrícula | Más preciso para escuelas de música, pero cambia el modelo actual y complica la ficha del alumno; se pospone. |
| Texto libre para el tipo | Dificulta filtros, etiquetas y validaciones; una lista cerrada con `OTRO` cubre la v1. |
| Renombrar «instrumento» por «especialidad» | Generaliza el vocabulario, pero pierde claridad en el caso musical y obliga a retraducir toda la interfaz. |

## Relacionado

[[Mancomunidad]] · [[Asignatura]] · [[Instrumento]] · [[Alumno]] · [[Modelo de dominio]] · [[Modelo de datos]] · [[Requisitos funcionales]] · [[Roles y permisos]] · [[API REST]] · [[Roadmap]] · [[Glosario]]
