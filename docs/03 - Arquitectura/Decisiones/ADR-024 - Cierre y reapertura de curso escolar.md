---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - curso
  - horarios
  - asistencia
---

# ADR-024 — Cierre y reapertura de curso escolar

## Contexto

[[Curso escolar]] tenía estados `PLANIFICACION`, `ACTIVO` y `CERRADO`, pero no estaba definido qué ocurría al cerrar: si se borraban horarios, qué pasaba con las sesiones `PROGRAMADA` que nunca se impartieron, si las matrículas seguían activas o si la asistencia histórica se podía corregir. Tampoco estaba claro qué se podía consultar después ni cómo reabrir un curso sin dejar datos incoherentes.

## Decisión

### Cierre

- `POST /cursos-escolares/{id}/cerrar` cierra el curso y **no borra ningún dato**: horarios, franjas, matrículas, sesiones, asistencias, historial, observaciones, preparaciones y días no lectivos se conservan.
- Antes de cerrar se ejecuta un **preflight**:
  - todas las sesiones `PROGRAMADA` sin asistencia pasan a `CANCELADA` con motivo «Cierre del curso» y autor el administrador que cierra;
  - las sesiones `ABIERTA` o `CERRADA` no se tocan;
  - los horarios conservan su estado (`BORRADOR` o `FINALIZADO`), pero quedan **congelados** por el curso cerrado;
  - las matrículas conservan su estado (`ACTIVA` o `BAJA`) como fotografía histórica y quedan congeladas.
- La respuesta incluye `sesionesCanceladas` y `horariosCongelados`. Si el curso ya está `CERRADO`, responde `409`; si está en `PLANIFICACION`, también.
- En un curso `CERRADO`:
  - no se crean ni editan matrículas, horarios, franjas, preparaciones, observaciones ni días no lectivos;
  - no se generan sesiones nuevas;
  - no se abren sesiones nuevas;
  - **sí se pueden corregir asistencias de sesiones ya existentes**, porque el historial debe poder rectificarse y cada cambio queda auditado ([[ADR-007 - Historial de cambios de asistencia]]). Esta es la única escritura permitida y no crea sesiones.
- El curso activo sigue siendo único por mancomunidad: si se cierra el activo, la mancomunidad queda sin curso activo hasta que se active otro y la aplicación muestra el error de configuración ya previsto.
- La retención y la purga no cambian: se calculan por matrícula, no por estado del curso.

### Reapertura

- `POST /cursos-escolares/{id}/reabrir` solo lo puede hacer un administrador y solo sobre un curso `CERRADO`.
- Si ya hay otro curso `ACTIVO`, la reapertura lo cierra en la misma transacción aplicando el mismo preflight. Si el cierre del activo falla, no se reabre nada.
- Al reabrir, el curso vuelve a `ACTIVO` y se descongelan sus datos; los horarios recuperan su estado y vuelven a ser editables si están en `BORRADOR`.
- Las sesiones canceladas en el cierre **no se reactivan**: si la clase se recupera, se crea una sesión puntual nueva.
- El generador diario vuelve a crear sesiones desde la fecha de reapertura, no retroactivamente.
- La reapertura exige un motivo de 10–500 caracteres y se registra en la auditoría administrativa.

### Relación con el curso siguiente

- Activar un curso distinto cierra el anterior en la misma transacción y aplica el preflight.
- La copia de horarios es una acción aparte ([[ADR-023 - Copia de horario al curso siguiente]]): el cierre no copia nada automáticamente.

## Consecuencias

### Positivas

- El cierre deja de ser un cambio de estado sin consecuencias y se convierte en un proceso explícito y auditable.
- No se pierde el trabajo de un horario en `BORRADOR`: queda congelado y puede retomarse si el curso se reabre.
- Las sesiones que nunca se impartieron no ensucian los contadores.
- La asistencia histórica sigue siendo corregible, que es la esencia de [[ADR-007 - Historial de cambios de asistencia]].
- Reabrir un curso no resucita sesiones canceladas ni duplica clases.

### Negativas / compromisos

- Un curso cerrado no es estrictamente de solo lectura: la asistencia se puede corregir.
- El preflight recorre sesiones y horarios, y la reapertura puede cerrar otro curso en la misma transacción.
- Las matrículas `ACTIVA` de un curso cerrado siguen contando como activas a efectos de retención, aunque no se puedan modificar.
- El administrador debe recordar que la copia de horarios es manual.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Borrar horarios y sesiones al cerrar | Pierde el histórico y contradice la retención. |
| Dejar las sesiones `PROGRAMADA` como pendientes para siempre | Genera ruido y falsea los contadores de un curso ya cerrado. |
| Bloquear toda escritura, incluida la asistencia | Impide corregir errores históricos y choca con ADR-007. |
| Copiar horarios automáticamente al cerrar | Mezcla dos decisiones y arrastra datos obsoletos. |
| Reabrir sin cerrar el curso activo | Rompe la regla de un único curso activo por mancomunidad. |
| Reactivar las sesiones canceladas al reabrir | Puede duplicar clases y reabrir listas ya cerradas. |

## Relacionado

[[Curso escolar]] · [[Horario del profesor]] · [[Sesión de clase]] · [[Matrícula]] · [[Preparación de clase]] · [[Observación]] · [[Día no lectivo]] · [[Asistencia]] · [[Historial de asistencia]] · [[Modelo de datos]] · [[API REST]] · [[Roadmap]] · [[ADR-007 - Historial de cambios de asistencia]] · [[ADR-008 - Curso escolar como entidad]] · [[ADR-023 - Copia de horario al curso siguiente]]
