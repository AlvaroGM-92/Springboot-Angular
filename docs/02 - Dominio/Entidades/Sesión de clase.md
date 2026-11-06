---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-04
tags:
  - dominio
  - entidad
  - asistencia
  - multi-tenant
---

# Sesión de clase

## Propósito

Representa un encuentro concreto de una asignatura en una fecha. Es la unidad sobre la que se pasa lista y se registra la asistencia de todos los alumnos a la vez.

Puede generarse automáticamente desde una franja del horario o crearse a mano para una clase puntual sin franja. La generación automática se decide en [[ADR-013 - Generación automática de sesiones desde el horario]]. Una sesión que no se imparte se marca `CANCELADA` con motivo y autor, sin lista de asistencia; decisión en [[ADR-017 - Cancelación de sesiones y días no lectivos]].

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant de la sesión. |
| `curso_escolar_id` | BIGINT | Sí | FK a [[Curso escolar]] de la misma mancomunidad; las generadas lo heredan de la franja y las puntuales usan el curso activo o el indicado. |
| `franja_id` | BIGINT | No | FK a [[Franja horaria]] de la misma mancomunidad, `ON DELETE SET NULL`; puede ser una sesión puntual o una sesión cuya franja se borró. |
| `asignatura_id` | BIGINT | Sí | FK a [[Asignatura]] de la misma mancomunidad. |
| `profesor_id` | BIGINT | Sí | FK a [[Usuario]]; profesor que imparte realmente la sesión (titular o sustituto). |
| `sustitucion_id` | BIGINT | No | FK a [[Sustitución]] de la misma mancomunidad; nulo si la imparte el titular. |
| `fecha` | DATE | Sí | Fecha de la clase. |
| `hora_inicio` | TIME | Sí | Hora real de la sesión. |
| `hora_fin` | TIME | Sí | Posterior a la hora de inicio. |
| `estado` | VARCHAR(20) | Sí | `PROGRAMADA`, `ABIERTA`, `CERRADA` o `CANCELADA`. |
| `observaciones` | TEXT | No | Notas de la sesión. |
| `cancelada_at` | DATETIME | No | Nulo salvo en `CANCELADA`; momento de la cancelación. |
| `cancelada_por` | BIGINT | No | FK a [[Usuario]] que cancela; obligatorio si `CANCELADA`. |
| `motivo_cancelacion` | VARCHAR(500) | No | 10–500 caracteres; obligatorio si `CANCELADA`. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Curso escolar]]**.
- **N:1 con [[Franja horaria]]** (opcional; `ON DELETE SET NULL`).
- **N:1 con [[Asignatura]]**.
- **N:1 con [[Usuario]]** (profesor y, si aplica, autor de la cancelación).
- **N:1 con [[Sustitución]]** (opcional).
- **1:N con [[Asistencia]]**.
- **1:N con [[Preparación de clase]]** (opcional).

## Reglas

- La sesión, el curso, la franja, la asignatura y el profesor deben pertenecer a la misma mancomunidad.
- Una tarea programada crea cada madrugada una sesión `PROGRAMADA` por cada franja del día del curso activo; no se generan para cursos cerrados, asignaturas inactivas, profesores sin pertenencia activa ni días no lectivos ([[Día no lectivo]]).
- La generación es idempotente: `(franja_id, fecha)` evita duplicados.
- La sesión `PROGRAMADA` no tiene filas de asistencia; la `CANCELADA` tampoco y no admite crearlas.
- Solo el profesor asignado a la sesión puede abrir, editar o cerrar una sesión: el titular o, dentro de una sustitución activa, el sustituto. El profesor titular y cualquier administrador de la mancomunidad pueden cancelar una sesión `PROGRAMADA` con motivo; la cancelación administrativa queda auditada.
- Al abrir una sesión `PROGRAMADA` pasa a `ABIERTA` y se cargan como `PRESENTE` todos los alumnos con matrícula `ACTIVA` en la asignatura y curso. Los alumnos con matrícula `BAJA` cuya fecha de baja sea igual o anterior a la fecha de la sesión no se cargan ([[ADR-026 - Baja de alumno a mitad de curso]]).
- Una sesión ya abierta conserva sus filas aunque el alumno cause baja después; no se eliminan asistencias ni historial.
- Si la fecha cae dentro de una [[Sustitución]] `ACTIVA`, la sesión nace o se reasigna con `profesor_id` del sustituto y `sustitucion_id` informado. Las sesiones ya abiertas o cerradas no cambian de profesor ([[ADR-025 - Sustituciones y cambio de titular]]).
- Una sesión puntual sin franja se crea a mano, nace `ABIERTA` y carga la lista de la misma forma.
- Si no se pasa lista, la sesión sigue `PROGRAMADA` y se puede abrir en cualquier fecha posterior.
- Una sesión `CANCELADA` solo puede venir de `PROGRAMADA`, no tiene asistencia y es terminal. Si la clase se imparte finalmente, se crea una sesión puntual nueva.
- Si una solicitud aprobada modifica una franja, se actualizan las sesiones `PROGRAMADA` sin asistencia; las que ya tienen lista no se tocan.
- Si una franja desaparece, sus sesiones `PROGRAMADA` sin asistencia se eliminan; las que ya tienen lista se conservan con `franja_id` nulo y mantienen su propia instantánea de curso, asignatura, profesor, fecha y horas ([[ADR-020 - Borrado de franjas y conservación de sesiones]]).
- La sesión debe pertenecer a una asignatura que imparte el profesor titular; el sustituto debe estar cubriendo esa asignatura en la fecha.
- Si se vincula a una franja, esta debe ser del horario del titular, de la misma asignatura y curso.
- No puede haber dos sesiones para la misma franja y fecha.
- Para sesiones puntuales, no puede repetirse profesor + fecha + hora de inicio.
- La sesión puede reabrirse o editarse en cualquier momento por su profesor; el estado `ABIERTA`/`CERRADA` es organizativo.
- Cada cambio de asistencia queda en [[Historial de asistencia]].
- Las sesiones se conservan como histórico; no se borran físicamente si tienen asistencias.
- En un curso `CERRADO` no se crean ni abren sesiones nuevas, pero las asistencias de sesiones existentes se pueden corregir ([[ADR-024 - Cierre y reapertura de curso escolar]]).

## Dudas

- ~~¿Se genera automáticamente una sesión por cada franja del día?~~ **Resuelto: sí, cada madrugada, en estado `PROGRAMADA` y sin asistencia; la lista se carga al abrirla.** Véase [[ADR-013 - Generación automática de sesiones desde el horario]].
- ~~¿Cómo se representa una clase que no se imparte?~~ **Resuelto: estado `CANCELADA`, solo desde `PROGRAMADA`, con motivo, autor y sin lista.** Véase [[ADR-017 - Cancelación de sesiones y días no lectivos]].
- ~~¿Qué pasa si se borra la franja de una sesión con lista?~~ **Resuelto: la sesión se conserva y `franja_id` queda nulo; las `PROGRAMADA` sin lista se eliminan.** Véase [[ADR-020 - Borrado de franjas y conservación de sesiones]].
- ~~¿Qué alumno se carga al abrir una sesión si alguno causó baja?~~ **Resuelto: solo los de matrícula `ACTIVA` en esa fecha; las sesiones ya abiertas conservan sus filas.** Véase [[ADR-026 - Baja de alumno a mitad de curso]].
- ~~¿Quién pasa lista si el titular está sustituido?~~ **Resuelto: el sustituto asignado a la sesión; el titular la ve en solo lectura.** Véase [[ADR-025 - Sustituciones y cambio de titular]].
- ¿Se puede pasar lista sin sesión? No: la sesión es el contenedor de la lista.
- ¿La preparación se vincula a la sesión o solo a asignatura + fecha? Se permite lo segundo y, si existe sesión, se enlaza.

## Relacionado

[[Mancomunidad]] · [[Curso escolar]] · [[Multi-tenencia]] · [[Día no lectivo]] · [[Franja horaria]] · [[Asistencia]] · [[Historial de asistencia]] · [[Preparación de clase]] · [[Sustitución]] · [[Alumno]] · [[Matrícula]] · [[Modelo de datos]] · [[ADR-013 - Generación automática de sesiones desde el horario]] · [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[ADR-020 - Borrado de franjas y conservación de sesiones]] · [[ADR-024 - Cierre y reapertura de curso escolar]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[ADR-026 - Baja de alumno a mitad de curso]]
