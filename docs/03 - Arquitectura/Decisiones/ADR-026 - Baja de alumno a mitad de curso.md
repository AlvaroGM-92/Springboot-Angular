---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - alumnos
  - matricula
  - asistencia
---

# ADR-026 — Baja de alumno a mitad de curso

## Contexto

[[Matrícula]] ya tenía los estados `ACTIVA` y `BAJA`, y la baja programaba `purga_at` a dos años, pero no estaba definido qué ocurría con las listas de clase: si el alumno desaparecía de las sesiones ya abiertas, si se borraban sus asistencias, si las sesiones `PROGRAMADA` dejaban de generarse o si una reactivación lo devolvía a las listas. Sin esa regla, cada implementación podía tratar la baja de forma distinta y se corría el riesgo de borrar historial o de seguir cargando a un alumno que ya no está matriculado.

## Decisión

- La baja es un **cambio de estado**, nunca un borrado: la fila de [[Matrícula]] se conserva, `estado` pasa a `BAJA`, se registra `baja_at` y se calcula `purga_at = baja_at + 2 años`. `purgada_at` queda nulo hasta la purga.
- **Las sesiones ya abiertas o cerradas no se tocan.** Sus filas de [[Asistencia]] y su [[Historial de asistencia]] se conservan y se pueden seguir consultando y corrigiendo según los permisos existentes.
- **Las listas se calculan al abrir la sesión**, no al generarla. Una sesión `PROGRAMADA` no tiene filas, así que la baja no requiere limpiar nada.
- Al abrir una sesión cuya `fecha` sea igual o posterior a la fecha local de `baja_at` en `Europe/Madrid`, el alumno con matrícula `BAJA` **no se carga**.
- Si la baja se produce después de abrir la sesión del mismo día, las filas ya creadas se mantienen; la exclusión aplica a las aperturas siguientes.
- La baja no elimina la sesión ni cambia su estado, y no afecta a los contadores de las sesiones en las que el alumno ya tenía fila.
- **Reactivar** devuelve la matrícula a `ACTIVA`, limpia `baja_at`, `purga_at` y `purgada_at`, y vuelve a incluir al alumno en las listas de las sesiones que se abran a partir de ese momento.
- La reactivación **no añade filas** a sesiones ya abiertas: esas listas son una fotografía del momento de apertura y se conservan. Si hace falta reflejar una asistencia en una sesión ya abierta, se usa el flujo de corrección de asistencia solo cuando la fila ya existe; en la v1 no se añaden alumnos sueltos a una lista abierta.
- La validación de conflictos de alumno de [[ADR-021 - Validación de conflictos de alumno]] se aplica a la reactivación desde la fecha de reactivación en adelante, contra los horarios `FINALIZADO` del mismo curso.
- Desactivar un [[Alumno]] (`activo = false`) es una decisión distinta de dar de baja una matrícula: no se permite desactivar mientras tenga matrículas `ACTIVA`; primero se dan de baja. Así ninguna lista futura depende de un alumno inactivo.
- La baja no altera el estado del [[Curso escolar]] ni impide cerrar el curso. La purga sigue dependiendo de `purga_at`, no del estado del curso ni de la sesión.

## Consecuencias

### Positivas

- El histórico de asistencia nunca se pierde por una baja.
- Las listas futuras son coherentes con la situación administrativa del alumno.
- La reactivación es reversible y no duplica matrículas ni filas de asistencia.
- La purga sigue teniendo una fecha clara y no depende de las sesiones.

### Negativas / compromisos

- Una sesión abierta antes de la baja conserva al alumno aunque ya no esté matriculado; es deliberado para no reescribir el pasado.
- No se puede añadir manualmente un alumno a una sesión ya abierta en la v1.
- La baja y la desactivación del alumno son operaciones separadas y la segunda exige la primera.
- La fecha de corte se calcula en zona horaria local, no en UTC, para no excluir por unas horas de diferencia.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Borrar al alumno de las sesiones futuras y de las abiertas | Destruye el historial y falsea lo ocurrido. |
| Eliminar físicamente la matrícula | Rompe la retención y la trazabilidad. |
| Excluir al alumno de todas las sesiones, incluidas las cerradas | Reabre listas ya impartidas y contradice la inmutabilidad del historial. |
| Cargar siempre a todos los alumnos y marcar la baja como estado de asistencia | Mezcla matrícula con asistencia y ensucia los contadores. |
| Impedir la reactivación en el mismo curso | Obliga a crear matrículas nuevas y duplica datos. |
| Mantener al alumno en listas futuras hasta que se desactive la cuenta | No refleja la realidad administrativa de la baja. |

## Relacionado

[[Matrícula]] · [[Alumno]] · [[Asignatura]] · [[Curso escolar]] · [[Sesión de clase]] · [[Asistencia]] · [[Historial de asistencia]] · [[Resumen de asistencia]] · [[Modelo de datos]] · [[API REST]] · [[Roadmap]] · [[ADR-007 - Historial de cambios de asistencia]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]] · [[ADR-021 - Validación de conflictos de alumno]]
