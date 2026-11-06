---
tipo: adr
estado: aceptada
fecha: 2026-11-03
actualizado: 2026-11-03
tags:
  - adr
  - asistencia
  - calendario
  - sesiones
---

# ADR-017 — Cancelación de sesiones y días no lectivos

## Contexto

Una [[Sesión de clase]] solo podía estar `PROGRAMADA`, `ABIERTA` o `CERRADA`. No había forma de representar una clase que no se imparte por ausencia del profesor, excursión, festivo o incidencia, y la generación automática de [[ADR-013 - Generación automática de sesiones desde el horario]] reconocía que un festivo dejaba sesiones pendientes y añadía ruido. Tampoco existía un mínimo de calendario para excluir fechas.

## Decisión

### Sesión cancelada

- Se añade el estado **`CANCELADA`**. Solo se permite la transición `PROGRAMADA → CANCELADA`; una sesión `ABIERTA` o `CERRADA` no se cancela para no borrar asistencias ya registradas.
- La cancelación exige `motivo_cancelacion` de 10–500 caracteres y registra `cancelada_por` y `cancelada_at`. Es una operación auditada.
- Puede cancelar el **profesor titular** de la sesión y cualquier **administrador** de la mancomunidad. El administrador no edita asistencias, pero puede cancelar una sesión `PROGRAMADA` con motivo; la excepción queda auditada.
- Una sesión `CANCELADA` **no tiene filas de asistencia** y no admite abrirlas: `POST /sesiones/{id}/asistencias` responde `409`. Tampoco aparece en la bandeja de pendientes ni cuenta en los contadores.
- `CANCELADA` es terminal. Si la clase se imparte finalmente, se crea una **sesión puntual** nueva; no se reabre la cancelada.
- Si se abrió una sesión por error, no se cancela: se cierra y se deja constancia en `observaciones`, porque ya existen filas de asistencia.

### Días no lectivos

- Se crea la entidad [[Día no lectivo]] (`dia_no_lectivo`) por mancomunidad, curso escolar y **centro opcional**. `centro_id` nulo significa que afecta a toda la mancomunidad; con centro, solo a las asignaturas de ese centro.
- La fecha debe pertenecer al curso y no puede duplicarse para el mismo ámbito. La unicidad con `centro_id` nulo se valida en el servicio porque MySQL permite varios `NULL`.
- Solo la administración gestiona el calendario. En un curso `CERRADO` es de solo lectura.
- El generador diario **omite** las franjas cuya fecha y centro están cubiertos por un día no lectivo.
- Al crear un día no lectivo, las sesiones `PROGRAMADA` de esa fecha y ámbito **sin asistencia** pasan a `CANCELADA` con motivo «Día no lectivo» y autor el usuario que registra el día. Las sesiones `ABIERTA` o `CERRADA` con asistencia no se tocan.
- Si se elimina el día antes de la fecha, el generador volverá a crear las sesiones `PROGRAMADA` en la siguiente ejecución; las ya canceladas no se reactivan.
- El calendario es mínimo: no define horarios, turnos, grupos ni plantillas anuales.

### API

- `POST /sesiones/{id}/cancelar` con `motivo`.
- `GET /dias-no-lectivos`, `POST /dias-no-lectivos` y `DELETE /dias-no-lectivos/{id}` para la administración.

## Consecuencias

### Positivas

- Los festivos, excursiones y ausencias dejan de ensuciar la bandeja de pendientes.
- La cancelación deja autor, fecha y motivo, y no mezcla una clase no impartida con una lista de asistencia.
- El calendario por centro permite mancomunidades con festivos locales distintos.
- El histórico de sesiones sigue siendo completo: una sesión cancelada se conserva y se puede consultar.

### Negativas / compromisos

- Hay una entidad y un CRUD más, y el generador debe consultar el calendario por fecha y centro.
- Crear un día no lectivo después de generar sesiones obliga a cancelarlas en la misma transacción.
- El calendario es manual; no se importan festivos oficiales en la v1.
- La cancelación no borra una sesión ya abierta; esos casos se resuelven cerrando la sesión y anotando la incidencia.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Reutilizar `CERRADA` para las clases no impartidas | Mezcla una clase dada con una no dada y falsea los contadores. |
| Borrar la sesión cuando no se imparte | Pierde la traza y complica la auditoría; además puede tener asistencias. |
| Marcar el festivo en el horario | El horario es semanal y se repite; un festivo es una fecha concreta. |
| Calendario completo con festivos, vacaciones y turnos | Sobredimensionado para la v1; se empieza por días no lectivos por mancomunidad o centro. |
| Cancelar desde `ABIERTA` borrando la lista | Rompe la inmutabilidad del historial de asistencia. |
| No tener calendario y aceptar el ruido | El usuario pidió expresamente resolver los festivos y las sesiones no impartidas. |

## Relacionado

[[Sesión de clase]] · [[Día no lectivo]] · [[Curso escolar]] · [[Centro]] · [[Asistencia]] · [[Historial de asistencia]] · [[Franja horaria]] · [[Modelo de datos]] · [[API REST]] · [[Roadmap]] · [[ADR-013 - Generación automática de sesiones desde el horario]] · [[ADR-008 - Curso escolar como entidad]]
