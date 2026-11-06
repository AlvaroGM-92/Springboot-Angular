---
tipo: moc
estado: activo
fase: 0
actualizado: 2026-11-06
tags:
  - moc
  - proyecto
---

# Gestión de Mancomunidad · Spring Boot + Angular

> [!info] Documentación viva
> Esta carpeta es la fuente de verdad del proyecto. Los requisitos, las decisiones y el progreso viven en notas enlazadas. El código se escribirá **después** de definir cada fase.

**Estado actual:** Fase 0 cerrada ✅ · Fase 1 en curso 🟡 (1.1 y 1.2 completadas) · **Siguiente:** Fase 1.3 — TenantContext y filtro de aislamiento.

## Ruta de lectura

1. [[Cómo usar esta documentación]] — normas del vault y flujo de trabajo.
2. [[Visión y alcance]] — qué construimos y qué queda fuera.
3. [[Roles y permisos]] — quién puede hacer qué.
4. [[Requisitos funcionales]] — el contrato de la v1.
5. [[Roadmap]] — el orden de construcción.

## Mapa del proyecto

| Área | Notas |
| --- | --- |
| **Definición** | [[Visión y alcance]] · [[Roles y permisos]] · [[Requisitos funcionales]] · [[Requisitos no funcionales]] · [[Privacidad y tratamiento de datos]] · [[Pendientes]] |
| **Producto** | [[Mapa de pantallas]] · [[Manual de usuario]] · [[Plan de pruebas manuales y datos de demo]] |
| **Dominio** | [[Modelo de dominio]] · [[Mancomunidad]] · [[Usuario]] · [[UsuarioMancomunidad]] · [[Centro]] · [[Asignatura]] · [[Alumno]] · [[Instrumento]] · [[Matrícula]] · [[Horario del profesor]] · [[Historial de horario]] · [[Franja horaria]] · [[Sustitución]] · [[Sesión de clase]] · [[Asistencia]] · [[Historial de asistencia]] · [[Resumen de asistencia]] · [[Día no lectivo]] · [[Observación]] · [[Preparación de clase]] · [[Solicitud de cambio de horario]] · [[Curso escolar]] |
| **Arquitectura** | [[Stack tecnológico]] · [[Arquitectura general]] · [[Multi-tenencia]] · [[Seguridad y autenticación]] · [[Control horario]] |
| **Decisiones** | [[ADR-001 - Stack tecnológico]] · [[ADR-002 - Base de datos MySQL y Flyway]] · [[ADR-003 - JWT con refresh token]] · [[ADR-004 - Horario gestionado por el profesor]] · [[ADR-005 - Asistencia por sesión y lista masiva]] · [[ADR-006 - Documentación en Obsidian]] · [[ADR-007 - Historial de cambios de asistencia]] · [[ADR-008 - Curso escolar como entidad]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-010 - Control horario diferido y PWA]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-013 - Generación automática de sesiones desde el horario]] · [[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]] · [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]] · [[ADR-016 - Horario editable por profesor y administrador]] · [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[ADR-018 - Curso escolar en las observaciones]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]] · [[ADR-020 - Borrado de franjas y conservación de sesiones]] · [[ADR-021 - Validación de conflictos de alumno]] · [[ADR-022 - Conflictos de profesor entre mancomunidades]] · [[ADR-023 - Copia de horario al curso siguiente]] · [[ADR-024 - Cierre y reapertura de curso escolar]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[ADR-026 - Baja de alumno a mitad de curso]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-033 - Idempotencia de peticiones sensibles]] · [[ADR-034 - Bootstrap del primer superadministrador]] · [[ADR-035 - Gestión de la clave de cifrado de copias]] |
| **Planificación** | [[Roadmap]] · [[Fase 0 - Definición y documentación]] · [[Fase 1 - Entorno y esqueleto]] · [[Lienzos de fases]] |
| **Referencia** | [[Trazabilidad]] · [[Enumeraciones]] · [[Modelo de datos]] · [[API REST]] · [[Ejemplos de API]] · [[Convenciones]] · [[Copias de seguridad y exportación]] |
| **Seguimiento** | [[Diario de cambios]] · [[2026-10-27]] · [[2026-10-28]] · [[2026-10-29]] · [[2026-11-02]] · [[2026-11-03]] · [[2026-11-04]] · [[2026-11-06]] |
| **Plantillas** | [[Plantilla - Fase]] · [[Plantilla - Entidad]] · [[Plantilla - ADR]] · [[Plantilla - Sesión de diario]] |

## Estado de las fases

| Fase | Estado |
| --- | --- |
| 0 — Definición y documentación | ✅ Cerrada |
| 1 — Entorno y esqueleto | 🟡 En curso (1.1 y 1.2 completadas) |
| 2 — Autenticación y seguridad base | ⬜ Pendiente |
| 3 — Usuarios y centros | ⬜ Pendiente |
| 4 — Asignaturas y profesorado | ⬜ Pendiente |
| 5 — Alumnos, instrumentos y matrículas | ⬜ Pendiente |
| 6 — Horarios y solicitudes | ⬜ Pendiente |
| 7 — Panel del profesor | ⬜ Pendiente |
| 8 — Observaciones, asistencia y calendario | ⬜ Pendiente |
| 9 — Preparación de clases | ⬜ Pendiente |
| 10 — Calidad, pruebas y despliegue | ⬜ Pendiente |
| 11 — Panel de plataforma | ⬜ Pendiente |
| 12 — Control horario | ⬜ Pendiente (posterior a la v1) |
| 13 — PWA y modo sin conexión | ⬜ Pendiente (posterior a la v1) |

## Decisiones clave ya tomadas

- **Proyecto nuevo desde cero.** No se reutiliza el código ni los datos del proyecto PHP anterior.
- **Backend:** Java 21 + Spring Boot 4.1 + Maven.
- **Frontend:** Angular 22 + Angular Material.
- **Base de datos:** MySQL 8.4 LTS + Flyway.
- **Autenticación:** JWT de acceso + refresh token rotatorio; recuperación de contraseña por enlace de un solo uso, cambio obligatorio tras una contraseña temporal y bloqueo temporal tras 5 fallos por cuenta o 20 por IP.
- **Seguridad de sesión:** access tokens RS256 con `kid` y JWKS, rotación de clave, familias de refresh con detección de reutilización, `tokenVersion` para revocación rápida, máximo de 5 sesiones, cierre global, rate limiting e idempotencia (`limite_peticion` e `idempotencia` en MySQL). La MFA queda fuera de la v1 como riesgo aceptado con revisión antes de producción ([[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-033 - Idempotencia de peticiones sensibles]]).
- **Cuentas y email:** la recuperación exige email verificado; el alta envía una verificación de 24 horas y el cambio de dirección exige confirmar la nueva. El administrador de mancomunidad no puede cambiar emails; solo el titular o el soporte, con doble confirmación ([[ADR-028 - Verificación y cambio de email]]). El primer superadministrador se crea por comando de bootstrap, sin contraseña sembrada ([[ADR-034 - Bootstrap del primer superadministrador]]).
- **Horario:** el profesor titular y el administrador pueden construir y finalizar el horario de un profesor mientras está en `BORRADOR`, para cubrir las dos formas de trabajar de las mancomunidades. Tras finalizarlo, los cambios se solicitan o se abren con motivo y auditoría; nadie edita franjas finalizadas. Hay rejilla por profesor (editable en borrador) y por grupo (solo lectura, con todas sus asignaturas). Se valida el conflicto de alumno entre grupos; los solapamientos de profesor entre mancomunidades no se comprueban en la v1 ([[ADR-021 - Validación de conflictos de alumno]] · [[ADR-022 - Conflictos de profesor entre mancomunidades]]).
- **Asistencia:** lista masiva por sesión de clase, editable siempre, con historial inmutable visible solo para administración; las sesiones del día se generan desde el horario y quedan pendientes si no se pasa lista. Una clase no impartida se cancela con motivo y autor, y los festivos se registran como días no lectivos. Se conserva mientras la matrícula está activa y 2 años desde la baja; después solo quedan contadores anonimizados ([[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]]).
- **Franja:** sin duración mínima; máximo opcional por mancomunidad, nulo = sin máximo.
- **Curso escolar:** entidad ligera con un único curso activo **por mancomunidad**. Al cerrar no se borra nada: se cancelan las sesiones `PROGRAMADA` sin asistencia, se congelan horarios y matrículas y solo se puede corregir la asistencia existente; la reapertura es administrativa, con motivo, y no resucita sesiones canceladas ([[ADR-024 - Cierre y reapertura de curso escolar]]).
- **Copia y sustituciones:** el horario se puede copiar explícitamente al curso siguiente a `BORRADOR`, omitiendo franjas no válidas y sin arrastrar sesiones ([[ADR-023 - Copia de horario al curso siguiente]]). Un cambio de titular se modela como sustitución por asignatura y periodo; no cambia el horario y solo reasigna las sesiones del periodo ([[ADR-025 - Sustituciones y cambio de titular]]).
- **Baja de alumno:** la matrícula pasa a `BAJA` sin borrar asistencias ni historial; el alumno sale de las listas futuras y la reactivación lo devuelve a ellas ([[ADR-026 - Baja de alumno a mitad de curso]]).
- **Multi-tenencia:** base de datos compartida con `mancomunidad_id`, identidad global de usuario y selector de mancomunidad al iniciar sesión. El código de la mancomunidad es inmutable, no se reutiliza y no decide el tenant.
- **Áreas de enseñanza:** mancomunidad y asignatura tienen un tipo de una lista cerrada e informativa; la mancomunidad puede mezclar áreas y solo las asignaturas musicales instrumentales activan el módulo de instrumentos, que se oculta sin borrar datos si no se usa ([[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]]).
- **Aislamiento:** FKs simples y validación de la misma mancomunidad en el servicio, cubierta con tests de dos tenants.
- **Concurrencia:** `version` en las entidades editables; una versión desactualizada devuelve `409`.
- **Copias:** `mysqldump` diario cifrado, 30 días + 12 mensuales, sin binlogs; restauración trimestral en staging y exportación por script filtrado por tenant.
- **Plataforma:** rol `SUPERADMIN` para crear, suspender y reactivar mancomunidades y para dar soporte a cuentas de cualquier mancomunidad, sin acceso a datos académicos.
- **Administración:** lectura global sobre observaciones, asistencias y preparaciones **dentro de su mancomunidad**; construye el horario de cualquier profesor en `BORRADOR` y lo reabre con motivo, pero no edita un horario finalizado.
- **Aula:** texto libre en la franja, no catálogo.
- **Control horario:** fuera de la v1; fichaje con hora del servidor, auditoría y distribución como PWA, en Fases 12–13.
- **Idioma del dominio:** español en código, base de datos y API; sufijos técnicos en inglés.

## Principios de trabajo

> [!tip] Cómo avanzamos
> - Una fase = una porción vertical y desplegable.
> - Primero se documenta el qué y el porqué; después se implementa.
> - Cada decisión relevante se registra como ADR.
> - Al terminar cada sesión se actualiza el [[Diario de cambios]].
> - No se duplica información: se enlaza la nota que ya la contiene.
