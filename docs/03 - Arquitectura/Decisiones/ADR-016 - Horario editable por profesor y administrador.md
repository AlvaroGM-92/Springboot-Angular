---
tipo: adr
estado: aceptada
fecha: 2026-11-02
actualizado: 2026-11-02
tags:
  - adr
  - horarios
  - roles
---

# ADR-016 — Horario editable por el profesor y por el administrador

## Contexto

[[ADR-004 - Horario gestionado por el profesor]] reservaba la creación y edición del horario al profesor titular y dejaba al administrador en solo lectura, salvo la reapertura. Esa regla encaja con mancomunidades donde cada docente monta su propia semana, pero no con otras donde la administración centraliza la planificación porque conoce antes los centros, las aulas y las asignaciones.

El usuario confirmó que cada mancomunidad funciona de forma distinta y pidió soportar **las dos formas**: que el profesor pueda crear su horario y que el administrador también pueda crearlo, sin obligar a elegir un modo por mancomunidad.

## Decisión

- El [[Horario del profesor]] sigue siendo **uno por profesor y curso escolar**, con estado `BORRADOR` o `FINALIZADO`.
- Mientras está en `BORRADOR`, pueden crear, editar y eliminar sus franjas:
  - el **profesor titular** del horario;
  - cualquier **administrador de la misma mancomunidad**.
- Ambos pueden **guardar de forma parcial** y **finalizar** el horario.
- El administrador puede crear el horario de cualquier profesor con pertenencia activa `PROFESOR` en su mancomunidad. No puede tocar horarios de otra mancomunidad: responden `404`.
- El profesor solo puede operar sobre su propio horario.
- Una vez `FINALIZADO`, la edición directa se bloquea para ambos. Los cambios siguen el cauce existente:
  - el profesor crea una [[Solicitud de cambio de horario]];
  - el administrador la resuelve o reabre el horario con motivo y auditoría.
- `horario_profesor` y `franja_horaria` incorporan `creado_por` y `actualizado_por`. El [[Historial de horario]] ya registra el autor de cada creación, finalización y reapertura.
- La edición normal del administrador **no exige motivo**: basta el autor y la fecha. El motivo de 10–500 caracteres se reserva para la reapertura, como hasta ahora.
- Las validaciones de solapamientos, horas, asignatura y curso son las mismas para los dos roles y se ejecutan en el backend dentro de una transacción.

## Consecuencias

### Positivas

- Una mancomunidad puede dejar que cada profesor monte su horario o centralizarlo en administración, sin configuración adicional.
- El administrador puede corregir la planificación sin esperar a que el profesor la cree.
- La autoría queda registrada en el horario, en cada franja y en el historial de estados.
- El ciclo `BORRADOR → FINALIZADO` se mantiene: no se pierde el punto de control ni la auditoría.
- El profesor conserva la posibilidad de editar su horario mientras esté en borrador.

### Negativas / compromisos

- Hay que comprobar permisos por rol y propiedad en cada operación de escritura.
- Dos personas pueden editar el mismo horario; el bloqueo optimista con `version` y el `409` evitan pisarse.
- La interfaz debe mostrar quién modificó por última vez y dejar claro si el horario lo está montando el centro o el profesor.
- El administrador acumula una excepción más al solo lectura académico; se documenta en [[Roles y permisos]] y en [[Seguridad y autenticación]].
- Las pruebas deben cubrir las dos rutas de autoría y el aislamiento entre mancomunidades.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Solo el profesor edita, como en ADR-004 | No cubre mancomunidades que centralizan la planificación. |
| Solo el administrador edita | Quita autonomía a las mancomunidades donde el profesor planifica. |
| Un modo configurable por mancomunidad | Añade configuración y estados intermedios; permitir ambos roles siempre es más simple y cubre los dos casos. |
| Edición directa del administrador sobre horarios finalizados | Rompe el bloqueo y la trazabilidad de las solicitudes; se mantiene la reapertura con motivo. |
| Pedir motivo en cada edición administrativa | Burocratiza una tarea rutinaria; el autor y la fecha bastan, y el motivo se reserva para la reapertura. |

## Relacionado

[[Horario del profesor]] · [[Franja horaria]] · [[Historial de horario]] · [[Solicitud de cambio de horario]] · [[Roles y permisos]] · [[Seguridad y autenticación]] · [[Modelo de dominio]] · [[Modelo de datos]] · [[API REST]] · [[Requisitos funcionales]] · [[Roadmap]] · [[ADR-004 - Horario gestionado por el profesor]]
