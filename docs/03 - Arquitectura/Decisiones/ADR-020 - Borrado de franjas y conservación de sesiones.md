---
tipo: adr
estado: aceptada
fecha: 2026-11-03
actualizado: 2026-11-03
tags:
  - adr
  - horarios
  - sesiones
  - integridad
---

# ADR-020 — Borrado de franjas y conservación de sesiones

## Contexto

[[Sesión de clase]] referencia la [[Franja horaria]] que la generó mediante `franja_id`, una FK simple y opcional. La regla de [[ADR-013 - Generación automática de sesiones desde el horario]] dice que, si una franja desaparece, su sesión `PROGRAMADA` sin asistencia se elimina y la que ya tiene lista se conserva. Sin embargo, no se había decidido qué hace la FK al borrar la franja: bloquear el borrado dejaría franjas imposibles de eliminar y `CASCADE` borraría sesiones con asistencia.

## Decisión

- `sesion_clase.franja_id` es una FK **`ON DELETE SET NULL`**. La sesión nunca se borra por cascada.
- La sesión guarda su propio `curso_escolar_id`, `asignatura_id`, `profesor_id`, `fecha`, `hora_inicio` y `hora_fin`. Esos campos son la **instantánea** de la clase y no dependen de que la franja siga existiendo.
- Al borrar una franja, el servicio ejecuta este orden dentro de la transacción:
  1. Elimina las sesiones `PROGRAMADA` de esa franja que **no tengan ninguna fila de asistencia**.
  2. Conserva las sesiones que ya tienen asistencia, estén `ABIERTA`, `CERRADA` o con histórico.
  3. Borra la franja; las sesiones conservadas quedan con `franja_id = NULL` por la FK.
- La eliminación de una franja solo ocurre en un horario `BORRADOR`. Si el horario está `FINALIZADO`, primero hay que reabrirlo con motivo, lo que ya cancela las solicitudes `PENDIENTE`.
- `solicitud_cambio_horario.franja_id` también es `ON DELETE SET NULL`. Las solicitudes resueltas conservan su snapshot propuesto (`asignatura_id`, día y horas propuestas) aunque la franja original ya no exista. Las solicitudes pendientes no llegan a este caso porque la reapertura las cancela.
- No se borran sesiones con asistencia ni su historial; la sesión huérfana sigue siendo consultable y se distingue por `franja_id` nulo.
- La interfaz muestra la sesión como «puntual» o «sin franja» cuando `franja_id` es nulo.

## Consecuencias

### Positivas

- Se pueden borrar franjas sin bloquear el horario ni perder clases ya impartidas.
- Las sesiones conservan todo lo necesario para pasar lista y auditar aunque la franja desaparezca.
- No hay borrados en cascada de datos académicos.
- El histórico de solicitudes sigue siendo legible.

### Negativas / compromisos

- Hay que ejecutar el borrado de sesiones vacías en el servicio, no solo confiar en la FK.
- Las sesiones huérfanas requieren una marca visual y no se pueden regenerar desde una franja.
- La FK `ON DELETE SET NULL` no distingue por sí sola entre «sesión puntual» y «sesión cuya franja se borró»; ambas tienen `franja_id` nulo.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| `ON DELETE CASCADE` | Borraría sesiones con asistencia e historial. |
| `ON DELETE RESTRICT` | Impide borrar franjas con sesiones y obliga a borrar sesiones a mano. |
| Borrar siempre las sesiones de la franja | Pierde clases ya impartidas y su historial. |
| Copiar la franja entera en la sesión | Duplica más datos de los necesarios; la sesión ya tiene la instantánea mínima. |

## Relacionado

[[Franja horaria]] · [[Sesión de clase]] · [[Horario del profesor]] · [[Solicitud de cambio de horario]] · [[Asistencia]] · [[Modelo de datos]] · [[ADR-013 - Generación automática de sesiones desde el horario]] · [[ADR-016 - Horario editable por profesor y administrador]]
