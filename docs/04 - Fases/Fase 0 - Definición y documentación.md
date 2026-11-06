---
tipo: fase
fase: 0
estado: cerrada
inicio: 2026-10-27
actualizado: 2026-11-06
tags:
  - fase/0
---

# Fase 0 — Definición y documentación

> [!success] Objetivo cumplido
> Dejar el proyecto definido y el sistema documental listo para trabajar por fases, sin escribir todavía código de la aplicación.

> [!success] Fase cerrada
> Los 48 puntos de [[Pendientes]] están resueltos y el usuario ha aprobado el cierre de la Fase 0.

## 1. Contexto

Punto de partida:

- Proyecto nuevo con Spring Boot + Angular.
- Dominio: gestión de mancomunidades de centros de enseñanza de cualquier área, con origen en una mancomunidad de escuelas de música.
- Dos roles de mancomunidad: profesor y administrador; un rol de plataforma: superadministrador.
- Preparado para varias mancomunidades con identidad global de usuario.
- Se quiere documentación viva en Obsidian, dividida en notas enlazadas.
- No se reutiliza el proyecto PHP anterior.

## 2. Decisiones tomadas

| Decisión | Elección | Nota |
| --- | --- | --- |
| Alcance | Proyecto nuevo desde cero | [[Visión y alcance]] |
| Base de datos | MySQL 8.4 LTS | [[ADR-002 - Base de datos MySQL y Flyway]] |
| Autenticación | JWT + refresh token rotatorio | [[ADR-003 - JWT con refresh token]] |
| Horario | Borrador incremental → finalización → solicitud para cambios; lo construyen el profesor titular y el administrador | [[ADR-004 - Horario gestionado por el profesor]] · [[ADR-016 - Horario editable por profesor y administrador]] |
| Asistencia | Sesión de clase + lista masiva; editable siempre con historial inmutable | [[ADR-005 - Asistencia por sesión y lista masiva]] · [[ADR-007 - Historial de cambios de asistencia]] |
| Curso escolar | Entidad ligera con un único curso activo por mancomunidad; cierre con preflight y reapertura administrativa | [[ADR-008 - Curso escolar como entidad]] · [[ADR-024 - Cierre y reapertura de curso escolar]] |
| Copia de horario | Acción explícita al curso siguiente, a `BORRADOR` y omitiendo franjas no válidas | [[ADR-023 - Copia de horario al curso siguiente]] |
| Sustituciones | Periodo por asignatura que no cambia el horario y reasigna las sesiones al sustituto | [[ADR-025 - Sustituciones y cambio de titular]] |
| Baja de matrícula | Conserva el histórico, excluye de listas futuras y permite reactivación | [[ADR-026 - Baja de alumno a mitad de curso]] |
| Multi-tenencia | Base compartida con `mancomunidad_id` e identidad global; selección de mancomunidad al entrar | [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] |
| Administración | Lectura global dentro de su mancomunidad; construye horarios en `BORRADOR`, los finaliza y los reabre con motivo, pero no edita un horario finalizado | [[Roles y permisos]] · [[ADR-016 - Horario editable por profesor y administrador]] |
| Aula | Texto libre en la franja | [[Franja horaria]] |
| Documentación | Obsidian, notas atómicas y ADRs | [[ADR-006 - Documentación en Obsidian]] |
| Recuperación de contraseña | Enlace de un solo uso por correo; temporal con cambio obligatorio | [[ADR-011 - Recuperación de contraseña y correo transaccional]] |
| Verificación de email | Doble confirmación al alta y al cambio; recuperación solo con email verificado | [[ADR-028 - Verificación y cambio de email]] |
| Claves JWT | RS256 con `kid`, JWKS, rotación y secretos fuera del repositorio | [[ADR-029 - Claves JWT, rotación y secretos]] |
| Sesiones | Familias de refresh, detección de reutilización, máximo de 5 y cierre global | [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] |
| Abuso | Rate limiting en MySQL y `Idempotency-Key` en escrituras sensibles | [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-033 - Idempotencia de peticiones sensibles]] |
| MFA | Fuera de la v1 como riesgo aceptado con revisión antes de producción | [[ADR-027 - MFA y riesgo aceptado del superadministrador]] |
| Bootstrap | Primer `SUPERADMIN` por comando auditado, sin contraseña sembrada | [[ADR-034 - Bootstrap del primer superadministrador]] |
| Soporte de plataforma | Cuentas de cualquier mancomunidad, con auditoría y sin datos académicos | [[ADR-012 - Soporte de cuentas del superadministrador]] |
| Áreas de enseñanza | Tipo de mancomunidad informativo; tipo de asignatura y módulo de instrumentos opcional | [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]] |
| Idioma del dominio | Español; sufijos técnicos en inglés | [[Convenciones]] |

## 3. Entregables

### Definición

- [x] [[Visión y alcance]]
- [x] [[Roles y permisos]]
- [x] [[Requisitos funcionales]]
- [x] [[Requisitos no funcionales]]
- [x] [[Privacidad y tratamiento de datos]]
- [x] [[Glosario]]

### Dominio

- [x] [[Modelo de dominio]]
- [x] [[Mancomunidad]]
- [x] [[Usuario]]
- [x] [[UsuarioMancomunidad]]
- [x] [[Centro]]
- [x] [[Asignatura]]
- [x] [[Alumno]]
- [x] [[Instrumento]]
- [x] [[Matrícula]]
- [x] [[Curso escolar]]
- [x] [[Día no lectivo]]
- [x] [[Horario del profesor]]
- [x] [[Historial de horario]]
- [x] [[Franja horaria]]
- [x] [[Sustitución]]
- [x] [[Sesión de clase]]
- [x] [[Asistencia]]
- [x] [[Historial de asistencia]]
- [x] [[Resumen de asistencia]]
- [x] [[Observación]]
- [x] [[Preparación de clase]]
- [x] [[Solicitud de cambio de horario]]

### Arquitectura

- [x] [[Stack tecnológico]]
- [x] [[Arquitectura general]]
- [x] [[Multi-tenencia]]
- [x] [[Seguridad y autenticación]]
- [x] [[ADR-001 - Stack tecnológico]]
- [x] [[ADR-002 - Base de datos MySQL y Flyway]]
- [x] [[ADR-003 - JWT con refresh token]]
- [x] [[ADR-004 - Horario gestionado por el profesor]]
- [x] [[ADR-005 - Asistencia por sesión y lista masiva]]
- [x] [[ADR-006 - Documentación en Obsidian]]
- [x] [[ADR-007 - Historial de cambios de asistencia]]
- [x] [[ADR-008 - Curso escolar como entidad]]
- [x] [[ADR-009 - Multi-tenant con mancomunidad e identidad global]]
- [x] [[ADR-010 - Control horario diferido y PWA]]
- [x] [[ADR-011 - Recuperación de contraseña y correo transaccional]]
- [x] [[ADR-012 - Soporte de cuentas del superadministrador]]
- [x] [[ADR-013 - Generación automática de sesiones desde el horario]]
- [x] [[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]]
- [x] [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]]
- [x] [[ADR-016 - Horario editable por profesor y administrador]]
- [x] [[ADR-017 - Cancelación de sesiones y días no lectivos]]
- [x] [[ADR-018 - Curso escolar en las observaciones]]
- [x] [[ADR-019 - Resumen de asistencia y purga anonimizada]]
- [x] [[ADR-020 - Borrado de franjas y conservación de sesiones]]
- [x] [[ADR-021 - Validación de conflictos de alumno]]
- [x] [[ADR-022 - Conflictos de profesor entre mancomunidades]]
- [x] [[ADR-023 - Copia de horario al curso siguiente]]
- [x] [[ADR-024 - Cierre y reapertura de curso escolar]]
- [x] [[ADR-025 - Sustituciones y cambio de titular]]
- [x] [[ADR-026 - Baja de alumno a mitad de curso]]
- [x] [[ADR-027 - MFA y riesgo aceptado del superadministrador]]
- [x] [[ADR-028 - Verificación y cambio de email]]
- [x] [[ADR-029 - Claves JWT, rotación y secretos]]
- [x] [[ADR-030 - Detección de reutilización de refresh tokens]]
- [x] [[ADR-031 - Límite de sesiones concurrentes y cierre global]]
- [x] [[ADR-032 - Rate limiting de extremos sensibles]]
- [x] [[ADR-033 - Idempotencia de peticiones sensibles]]
- [x] [[ADR-034 - Bootstrap del primer superadministrador]]
- [x] [[ADR-035 - Gestión de la clave de cifrado de copias]]

### Planificación y referencia

- [x] [[Roadmap]]
- [x] [[Modelo de datos]]
- [x] [[API REST]]
- [x] [[Ejemplos de API]]
- [x] [[Convenciones]]
- [x] [[Cómo usar esta documentación]]
- [x] [[Diario de cambios]]
- [x] [[Pendientes]]
- [x] [[Enumeraciones]]
- [x] [[Trazabilidad]]
- [x] Plantillas de fase, entidad, ADR y sesión.

## 4. Fuera de esta fase

- Crear el proyecto Spring Boot.
- Crear el proyecto Angular.
- Levantar MySQL.
- Implementar login o cualquier pantalla.

## 5. Criterios de aceptación

- [x] El alcance y los roles están escritos.
- [x] Los requisitos tienen IDs y fase asignada.
- [x] El modelo de dominio cubre todas las entidades del alcance.
- [x] El modelo contempla multi-tenencia, identidad global y roles por mancomunidad.
- [x] Hay un modelo de datos físico borrador.
- [x] Hay un contrato inicial de API.
- [x] Las decisiones importantes están en ADRs.
- [x] El roadmap permite empezar sin ambigüedades.
- [x] El vault está enlazado y tiene portada, guía y plantillas.
- [x] Revisión y validación del usuario.

## 6. Dudas resueltas en la revisión

1. ~~Confirmar MySQL 8.4 frente a MariaDB 11.4.~~ **Resuelto: MySQL 8.4 LTS.**
2. ~~Definir el flujo de publicación del horario.~~ **Resuelto: borrador → finalizado → solicitud de cambio.**
3. ~~Confirmar que el administrador es solo lectura sobre docencia.~~ **Resuelto: solo lectura sobre observaciones, asistencias, historial y preparaciones; desde [[ADR-016 - Horario editable por profesor y administrador]] también construye y finaliza horarios en `BORRADOR` y los reabre con motivo.**
4. ~~Decidir si el curso escolar será entidad o valor.~~ **Resuelto: entidad ligera con un único curso activo por mancomunidad.**
5. ~~Decidir si el aula será catálogo.~~ **Resuelto: texto libre.**
6. ~~Definir la ventana de edición de asistencias.~~ **Resuelto: siempre editable, con historial inmutable visible solo para administración.**
7. ~~Decidir cómo crecer a varias mancomunidades.~~ **Resuelto: base compartida con `mancomunidad_id`, identidad global y selector de mancomunidad; panel de plataforma en Fase 11.**
8. ~~Decidir si la administración puede reabrir un horario finalizado y en qué condiciones (Fase 6).~~ **Resuelto: solo el administrador, únicamente un horario `FINALIZADO`, con motivo obligatorio, auditoría en [[Historial de horario]] y cancelación de solicitudes pendientes.**
9. ~~Definir la política de conservación y purga del historial de asistencia (Fase 8/10).~~ **Resuelto: mientras la matrícula esté activa y 2 años desde la baja; después se purga y solo quedan contadores anonimizados en [[Resumen de asistencia]] ([[ADR-019 - Resumen de asistencia y purga anonimizada]]).**
10. ~~Definir el alcance exacto de las métricas del panel de plataforma (Fase 11).~~ **Resuelto: centros, usuarios y alumnos activos bajo demanda, sin tiempo real.**
11. ~~Decidir si el control horario del profesorado entra en la v1 y cómo se distribuye en móvil.~~ **Resuelto: fuera de la v1; Fases 12–13, con hora del servidor, verificación por confianza y auditoría, y distribución como PWA instalable.** Véase [[Control horario]].
12. ~~Decidir cómo recupera un usuario su contraseña.~~ **Resuelto: enlace de un solo uso por correo, de 60 minutos; la contraseña temporal del reset caduca en 24 horas y obliga a cambiarla.** Véase [[ADR-011 - Recuperación de contraseña y correo transaccional]].
13. ~~Decidir si el superadministrador puede tocar cuentas de cualquier mancomunidad.~~ **Resuelto: sí en el plano de cuentas (búsqueda, reset, activación, desactivación y rol), con motivo y auditoría; no en el académico ni con impersonación en la v1.** Véase [[ADR-012 - Soporte de cuentas del superadministrador]].
14. ~~Decidir la duración mínima y máxima de una franja.~~ **Resuelto: sin mínimo; máximo opcional por mancomunidad, nulo = sin máximo.** Véase [[Franja horaria]] y [[Mancomunidad]].
15. ~~Decidir si la sesión de clase se genera automáticamente o la crea el profesor.~~ **Resuelto: una tarea diaria genera una sesión `PROGRAMADA` por franja; la lista se carga al abrirla y, si no se pasa, queda pendiente.** Véase [[ADR-013 - Generación automática de sesiones desde el horario]].
16. ~~Definir la política de bloqueo y desbloqueo de cuentas.~~ **Resuelto: 5 fallos por cuenta o 20 por IP en 15 minutos bloquean 15 minutos; el bloqueo no revoca sesiones y solo el `SUPERADMIN` lo levanta antes, con motivo y auditoría.** Véase [[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]].
17. ~~Definir el eje de la rejilla semanal y quién la consulta.~~ **Resuelto: rejilla por profesor (editable en `BORRADOR` por el profesor titular o un administrador) y por grupo (solo lectura, con todas sus asignaturas); el administrador ve todo y el profesor solo los grupos donde imparte.** Véase [[Horario del profesor]] y [[API REST]].
18. ~~Decidir si el código de una mancomunidad puede cambiar o reutilizarse.~~ **Resuelto: inmutable desde el alta, nunca reutilizado y sin papel en la resolución del tenant, URLs o subdominios.** Véase [[Mancomunidad]].
19. ~~Decidir el alcance de las FK compuestas.~~ **Resuelto: no se usan; FKs simples y validación de la misma mancomunidad en el servicio, con tests de aislamiento.** Véase [[Modelo de datos]] y [[Seguridad y autenticación]].
20. ~~Decidir el bloqueo optimista y cómo se expone en la API.~~ **Resuelto: `version` en toda entidad mutable con `updated_at` y en las respuestas; las actualizaciones lo envían y un desajuste devuelve `409`. Sin ETags HTTP en la v1.** Véase [[Modelo de datos]] y [[API REST]].
21. ~~Decidir la estrategia de índices.~~ **Resuelto: se crean los índices de FK y filtros ya documentados; la revisión con `EXPLAIN` y los nuevos índices se harán en la Fase 10.** Véase [[Modelo de datos]] y [[Roadmap]].
22. ~~Definir la estrategia de copias de seguridad y exportación por mancomunidad.~~ **Resuelto: copia completa diaria cifrada con 30 días y 12 mensuales, sin binlogs; restauración trimestral en staging y exportación por script filtrado por tenant.** Véase [[Copias de seguridad y exportación]].
23. ~~Decidir si el modelo sirve solo para escuelas de música.~~ **Resuelto: sirve para cualquier área; mancomunidad y asignatura tienen un tipo de una lista cerrada, el de la mancomunidad es informativo y solo las asignaturas musicales instrumentales activan el módulo de instrumentos, que se oculta sin borrar datos.** Véase [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]].
24. ~~Decidir si el administrador puede crear el horario de un profesor.~~ **Resuelto: sí; el profesor titular y cualquier administrador de la mancomunidad pueden crear, editar y finalizar horarios en `BORRADOR`, con autoría registrada y sin motivo salvo en la reapertura.** Véase [[ADR-016 - Horario editable por profesor y administrador]].
25. ~~Decidir cómo se representa una sesión que no se imparte.~~ **Resuelto: estado `CANCELADA`, solo desde `PROGRAMADA`, con motivo de 10–500 caracteres y autor; no tiene lista ni se reabre.** Véase [[ADR-017 - Cancelación de sesiones y días no lectivos]].
26. ~~Decidir si hay calendario de festivos y días no lectivos.~~ **Resuelto: sí, por curso escolar y ámbito de mancomunidad o centro; el generador los omite y cancela las sesiones `PROGRAMADA` sin asistencia.** Véase [[Día no lectivo]] y [[ADR-017 - Cancelación de sesiones y días no lectivos]].
27. ~~Decidir si una observación necesita curso escolar.~~ **Resuelto: `curso_escolar_id` obligatorio, con la fecha dentro del curso; la API puede inferirlo.** Véase [[ADR-018 - Curso escolar en las observaciones]].
28. ~~Decidir qué queda tras la purga de asistencias.~~ **Resuelto: [[Resumen de asistencia]] con `alumno_hash`, asignatura, curso y contadores; sin datos personales directos.** Véase [[ADR-019 - Resumen de asistencia y purga anonimizada]].
29. ~~Decidir qué ocurre con las sesiones al borrar una franja.~~ **Resuelto: las `PROGRAMADA` sin asistencia se eliminan; las que tienen lista se conservan con `franja_id` nulo y su instantánea.** Véase [[ADR-020 - Borrado de franjas y conservación de sesiones]].
30. ~~Decidir si se valida el conflicto de alumno entre grupos distintos.~~ **Resuelto: sí, al crear o editar franjas, al finalizar y al matricular o reactivar.** Véase [[ADR-021 - Validación de conflictos de alumno]].
31. ~~Decidir si se validan solapamientos de un profesor entre mancomunidades.~~ **Resuelto: no en la v1; la consulta cruzada rompería el aislamiento y se documenta como limitación aceptada.** Véase [[ADR-022 - Conflictos de profesor entre mancomunidades]].
32. ~~Decidir si el horario se arrastra al curso siguiente.~~ **Resuelto: con una copia explícita a `BORRADOR`, solo de franjas válidas, sin sesiones ni solicitudes y con previsualización.** Véase [[ADR-023 - Copia de horario al curso siguiente]].
33. ~~Decidir qué pasa al cerrar y reabrir un curso.~~ **Resuelto: el cierre cancela las sesiones `PROGRAMADA` sin lista y congela horarios y matrículas sin borrar nada; la reapertura es administrativa, con motivo, y no resucita sesiones.** Véase [[ADR-024 - Cierre y reapertura de curso escolar]].
34. ~~Decidir cómo se refleja un cambio de profesor a mitad de curso.~~ **Resuelto: con una sustitución por asignatura y periodo; no cambia el horario y solo reasigna las sesiones del periodo.** Véase [[ADR-025 - Sustituciones y cambio de titular]].
35. ~~Decidir el efecto de la baja de un alumno.~~ **Resuelto: conserva asistencias e historial, sale de las listas futuras y la reactivación lo devuelve a ellas.** Véase [[ADR-026 - Baja de alumno a mitad de curso]].
36. ~~Decidir si la MFA entra en la v1.~~ **Resuelto: no, ni para el `SUPERADMIN`; riesgo aceptado con controles compensatorios y revisión/firma antes de producción.** Véase [[ADR-027 - MFA y riesgo aceptado del superadministrador]].
37. ~~Definir la verificación y el cambio de email.~~ **Resuelto: verificación de 24 horas al alta, cambio con doble confirmación y recuperación solo sobre email verificado; el administrador de tenant no puede cambiar emails.** Véase [[ADR-028 - Verificación y cambio de email]].
38. ~~Decidir cómo se firman y rotan las claves JWT.~~ **Resuelto: RS256 con `kid`, JWKS, rotación cada 90 días con 24 horas de solapamiento y rotación de emergencia.** Véase [[ADR-029 - Claves JWT, rotación y secretos]].
39. ~~Definir qué hacer si se reutiliza un refresh ya rotado.~~ **Resuelto: revocar toda la familia, incrementar `token_version`, auditar y responder `401`, sin periodo de gracia.** Véase [[ADR-030 - Detección de reutilización de refresh tokens]].
40. ~~Decidir un límite de sesiones y una acción de cierre global.~~ **Resuelto: 5 familias activas, lista de sesiones y `logout-all` con contraseña.** Véase [[ADR-031 - Límite de sesiones concurrentes y cierre global]].
41. ~~Definir el rate limiting fuera del login.~~ **Resuelto: cubos por email/IP/familia/usuario/actor en `limite_peticion`, con `429` y `Retry-After`.** Véase [[ADR-032 - Rate limiting de extremos sensibles]].
42. ~~Decidir cómo evitar duplicados en reintentos de `POST`.~~ **Resuelto: `Idempotency-Key` obligatoria en altas y acciones sensibles, con tabla `idempotencia` de 24 horas.** Véase [[ADR-033 - Idempotencia de peticiones sensibles]].
43. ~~Definir el alta inicial del primer `SUPERADMIN`.~~ **Resuelto: comando `--bootstrap-superadmin` con secreto montado o consola, sin contraseña sembrada ni endpoint.** Véase [[ADR-034 - Bootstrap del primer superadministrador]].
44. ~~Aclarar si el reset del `SUPERADMIN` permite entrar como el afectado.~~ **Resuelto: no; la temporal la genera el servidor, el actor no la elige ni la recibe y no existe impersonación en la v1.** Véase [[ADR-012 - Soporte de cuentas del superadministrador]].

## 7. Siguiente fase

**Fase 1 — Entorno y esqueleto**, cuando se cierre esta fase. Objetivo: tener backend, frontend, MySQL, el esquema multi-tenant base y una pantalla mínima funcionando, con documentación actualizada.

## Relacionado

[[Roadmap]] · [[2026-10-27]] · [[2026-10-28]] · [[2026-10-29]] · [[2026-11-02]] · [[2026-11-03]] · [[2026-11-04]] · [[Pendientes]] · [[Stack tecnológico]] · [[Convenciones]] · [[Multi-tenencia]] · [[Control horario]] · [[Historial de horario]] · [[Historial de asistencia]] · [[Resumen de asistencia]] · [[Día no lectivo]] · [[Sustitución]] · [[ADR-004 - Horario gestionado por el profesor]] · [[ADR-008 - Curso escolar como entidad]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-010 - Control horario diferido y PWA]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-013 - Generación automática de sesiones desde el horario]] · [[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]] · [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]] · [[ADR-016 - Horario editable por profesor y administrador]] · [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[ADR-018 - Curso escolar en las observaciones]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]] · [[ADR-020 - Borrado de franjas y conservación de sesiones]] · [[ADR-021 - Validación de conflictos de alumno]] · [[ADR-022 - Conflictos de profesor entre mancomunidades]] · [[ADR-023 - Copia de horario al curso siguiente]] · [[ADR-024 - Cierre y reapertura de curso escolar]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[ADR-026 - Baja de alumno a mitad de curso]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-033 - Idempotencia de peticiones sensibles]] · [[ADR-034 - Bootstrap del primer superadministrador]]
