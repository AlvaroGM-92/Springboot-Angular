---
tipo: adr
estado: aceptada
fecha: 2026-10-29
actualizado: 2026-11-04
tags:
  - adr
  - asistencia
  - horarios
---

# ADR-013 — Generación automática de sesiones desde el horario

## Contexto

La asistencia se registra sobre una [[Sesión de clase]], que hasta ahora se creaba a mano al pasar lista. Ese flujo funciona, pero deja dos problemas:

- Si el profesor olvida abrir la sesión, la clase no existe y la lista se pierde hasta que alguien recuerde crearla.
- La administración no puede ver qué clases están pendientes de lista, porque no hay sesión que consultar.

El usuario decidió que las sesiones se generen **automáticamente desde las franjas del horario**. La decisión encaja con [[ADR-005 - Asistencia por sesión y lista masiva]], pero obliga a separar dos momentos: la sesión puede existir antes de que el profesor pase lista, y no debe marcar a nadie como presente sin intervención humana.

## Decisión

- Una **tarea programada** crea cada madrugada, a las 00:05 de la zona horaria configurada (Europe/Madrid en la v1), una sesión por cada franja del día del curso escolar activo.
- La sesión nace en estado **`PROGRAMADA`** y **sin filas de asistencia**.
- La generación es **idempotente**: la clave `(franja_id, fecha)` impide duplicados y la tarea puede repetirse sin efecto.
- Cuando el profesor **abre** la sesión, pasa a `ABIERTA` y se cargan como `PRESENTE` todos los alumnos con matrícula `ACTIVA` en esa fecha, en la misma transacción.
- Si el profesor **no pasa lista**, la sesión sigue `PROGRAMADA` y aparece como pendiente; puede abrirse cualquier día posterior, sin límite de antigüedad.
- Las **sesiones puntuales sin franja** se siguen creando a mano y nacen `ABIERTA`, cargando también la lista.
- Si una solicitud aprobada **modifica una franja**, se actualizan las sesiones `PROGRAMADA` que aún no tienen asistencia. Las sesiones `ABIERTA` o `CERRADA` y las que ya tienen filas de asistencia no se tocan.
- Si una franja **desaparece** y su sesión sigue `PROGRAMADA` sin asistencia, la sesión se elimina; si ya tiene lista, se conserva como histórico.
- La generación no crea sesiones para cursos cerrados, asignaturas inactivas ni profesores sin pertenencia activa. Si la fecha cae dentro de una [[Sustitución]] `ACTIVA`, la sesión se asigna al sustituto y guarda `sustitucion_id`.

## Consecuencias

### Positivas

- El profesor encuentra el día ya montado y solo tiene que pasar lista.
- La administración ve las clases pendientes sin esperar a que se creen a mano.
- Nadie queda marcado como presente por el simple hecho de que se genere la sesión.
- Los cambios de horario aprobados se reflejan en las sesiones vacías del mismo día.
- La tarea es idempotente y tolera reintentos.

### Negativas / compromisos

- Aparece una tarea programada que depende de la zona horaria y del calendario del servidor.
- Sin calendario de festivos en la v1, un día festivo genera sesiones que quedan pendientes y añaden ruido.
- Hay que distinguir `PROGRAMADA` de `ABIERTA` en la interfaz y en los contadores.
- La actualización de sesiones vacías añade lógica al aprobar una solicitud de cambio.
- Se almacena una sesión por franja y día aunque finalmente no haya clase.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Crear la sesión a mano al pasar lista | Ya existe; el olvido deja clases sin lista y sin visibilidad de pendientes. |
| Generar al abrir el panel del día | Depende de que alguien entre y produce resultados distintos según el momento de la primera consulta. |
| Generar la semana o el mes completo | Multiplica el ruido, complica los cambios de horario y llena la base de sesiones que quizá no se usen. |
| Generar la sesión y cargar la lista como presente | Marca a todos como presentes sin que el profesor haya pasado lista. |
| No tener sesión hasta que haya asistencia | Impide mostrar pendientes y obliga a crear la sesión en el mismo instante de pasar lista. |

> [!info] Actualización 2026-11-03
> La generación omite los [[Día no lectivo]] y las sesiones `PROGRAMADA` pueden cancelarse con motivo y autor ([[ADR-017 - Cancelación de sesiones y días no lectivos]]). Toda sesión guarda `curso_escolar_id` ([[ADR-019 - Resumen de asistencia y purga anonimizada]]) y, al borrar su franja, conserva su instantánea con `franja_id` nulo ([[ADR-020 - Borrado de franjas y conservación de sesiones]]).

> [!info] Actualización 2026-11-04
> La lista de alumnos se calcula al abrir y solo incluye matrículas `ACTIVA` en la fecha ([[ADR-026 - Baja de alumno a mitad de curso]]). Si la fecha cae en una sustitución activa, la sesión se asigna al sustituto ([[ADR-025 - Sustituciones y cambio de titular]]). En un curso cerrado no se generan sesiones nuevas ([[ADR-024 - Cierre y reapertura de curso escolar]]).

## Relacionado

[[Sesión de clase]] · [[Asistencia]] · [[Franja horaria]] · [[Horario del profesor]] · [[Curso escolar]] · [[Día no lectivo]] · [[Sustitución]] · [[Matrícula]] · [[Requisitos funcionales]] · [[Modelo de datos]] · [[Roadmap]] · [[ADR-005 - Asistencia por sesión y lista masiva]] · [[ADR-007 - Historial de cambios de asistencia]] · [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]] · [[ADR-020 - Borrado de franjas y conservación de sesiones]] · [[ADR-024 - Cierre y reapertura de curso escolar]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[ADR-026 - Baja de alumno a mitad de curso]]
