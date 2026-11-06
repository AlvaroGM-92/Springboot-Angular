---
tipo: adr
estado: aceptada
fecha: 2026-10-27
actualizado: 2026-11-03
tags:
  - adr
  - dominio
  - curso
---

# ADR-008 — Curso escolar como entidad

## Contexto

El [[Curso escolar]] es la dimensión temporal de matrículas, horarios y preparaciones. Se podía modelar como un valor textual (`2026-2027`) repetido en cada tabla o como una entidad propia. El proyecto necesita histórico por curso, horarios que se finalizan y una única referencia temporal clara.

## Decisión

- Se crea la tabla `curso_escolar` con `id`, `mancomunidad_id`, `codigo`, `fecha_inicio`, `fecha_fin` y `estado` (`PLANIFICACION`, `ACTIVO`, `CERRADO`).
- `matricula`, `horario_profesor` y `preparacion_clase` referencian el curso mediante `curso_escolar_id`.
- Solo puede existir **un curso `ACTIVO` por mancomunidad**; se refuerza con una columna generada que combina `mancomunidad_id` y `estado`, y un índice único, además de la validación en el servicio.
- El curso activo se obtiene de la base de datos para la mancomunidad activa, no se calcula en cada petición.
- Los cursos cerrados son de solo lectura.
- La migración inicial siembra el curso `2026-2027` como `ACTIVO` para la mancomunidad de ejemplo.
- La pantalla administrativa de cursos puede posponerse a la Fase 3; en la Fase 1 basta con la tabla y el dato inicial.

## Consecuencias

### Positivas

- Integridad referencial: no existen cursos inventados por un error tipográfico.
- Un único curso activo **por mancomunidad** y un ciclo de vida explícito.
- Fechas reales, estado y metadatos disponibles para informes y futuro calendario.
- El histórico por curso es fiable y se puede consultar sin ambigüedad.
- Evita migrar tres tablas más adelante.

### Negativas / compromisos

- Una tabla más, un CRUD y algunos joins.
- Hay que definir y probar las transiciones de estado y el bloqueo de cursos cerrados.
- La administración necesita una pantalla específica, aunque puede llegar después del esqueleto.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Valor textual `VARCHAR(9)` en cada tabla | Sin integridad, sin estado, sin fechas reales y con migración costosa si luego se necesita entidad. |
| Calcular el curso activo solo en código | No permite planificar el siguiente curso ni cerrar el actual con garantías. |
| Tabla con calendario completo y festivos | Sobredimensionado para la v1; se puede ampliar después. |

> [!info] Actualización 2026-11-03
> El curso escolar pasa a ser obligatorio también en [[Sesión de clase]] y [[Observación]], y se añade el calendario de [[Día no lectivo]]. Decisiones en [[ADR-017 - Cancelación de sesiones y días no lectivos]], [[ADR-018 - Curso escolar en las observaciones]] y [[ADR-019 - Resumen de asistencia y purga anonimizada]].

## Relacionado

[[Curso escolar]] · [[Mancomunidad]] · [[Multi-tenencia]] · [[Matrícula]] · [[Horario del profesor]] · [[Preparación de clase]] · [[Sesión de clase]] · [[Observación]] · [[Día no lectivo]] · [[Resumen de asistencia]] · [[Modelo de datos]] · [[Fase 0 - Definición y documentación]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[ADR-018 - Curso escolar en las observaciones]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]]
