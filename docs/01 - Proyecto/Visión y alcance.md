---
tipo: documento
estado: activo
fase: 0
actualizado: 2026-11-04
tags:
  - proyecto
  - alcance
---

# Visión y alcance

## 1. Contexto

Una **mancomunidad de centros de enseñanza** agrupa varios centros que comparten administración, profesorado y alumnado. El modelo nace en una mancomunidad de escuelas de música, pero sirve para cualquier área —danza, artes escénicas, idiomas, deportes u otras— y una misma mancomunidad puede mezclarlas. Hoy la información vive dispersa en hojas de cálculo, papel, correos y mensajes: horarios que cambian, faltas que se pierden, observaciones que no llegan al equipo y matrículas que no cuadran.

El proyecto construye una aplicación web única para centralizar esa gestión.

## 2. Problema

- No existe una visión única del horario de cada profesor ni de los grupos.
- Pasar lista y registrar retrasos es lento y propenso a errores.
- Las observaciones y la preparación de clases no están centralizadas.
- La administración no puede consultar de forma rápida la actividad docente.
- Los datos de alumnos, tutores e instrumentos (cuando el área los usa) están repartidos.

## 3. Solución

Una **SPA Angular** que consume una **API REST Spring Boot**, preparada para varias mancomunidades, con tres experiencias diferenciadas:

- **Profesor:** su horario, sus asignaturas, sus alumnos, lista masiva, observaciones y preparación de clases.
- **Administrador:** gestión de usuarios, centros, asignaturas, alumnos, instrumentos (si el módulo está activo), supervisión de la actividad docente y construcción del horario de cualquier profesor de su mancomunidad mientras está en borrador, **dentro de su mancomunidad**.
- **Plataforma:** un `SUPERADMIN` crea, suspende y reactiva mancomunidades y da soporte a las cuentas de cualquier mancomunidad. No accede a los datos académicos.

> [!info] Cualquier área de enseñanza
> La mancomunidad declara un tipo informativo (`MUSICA`, `DANZA`, `ARTES_ESCENICAS`, `IDIOMAS`, `DEPORTES` u `OTRO`) que no limita sus asignaturas. Cada asignatura tiene su propio tipo y solo las asignaturas musicales instrumentales activan el módulo de instrumentos. Una mancomunidad sin ese módulo funciona igual y no pierde datos ([[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]]).

## 4. Objetivos

1. Sustituir la gestión manual por una herramienta web única.
2. Permitir que el profesor planifique su semana y pase lista en segundos.
3. Dar a la administración una visión completa y fiable de centros, alumnado y docencia.
4. Conservar el histórico (cursos anteriores, observaciones, asistencias) sin borrar datos.
5. Mantener una base técnica moderna, segura y fácil de mantener.
6. Estar preparada para incorporar nuevas mancomunidades sin rehacer el modelo ni duplicar cuentas de usuario.

## 5. Alcance de la v1

| Módulo | Incluido |
| --- | --- |
| Autenticación | Login global, selección de mancomunidad, logout, refresh, cambio de contraseña, recuperación por enlace de un solo uso y roles por tenant. |
| Mancomunidades | Modelo multi-tenant y siembra de una mancomunidad; panel de plataforma para crear, suspender y reactivar (Fase 11) y soporte de cuentas con auditoría. |
| Usuarios | Alta, edición, activación/desactivación y pertenencias con rol por mancomunidad. |
| Centros | Alta, edición, activación/desactivación. |
| Asignaturas | Alta, edición, activación/desactivación y asignación de profesores. |
| Alumnos | Alta, edición, activación/desactivación, ficha y tutores. El alumno es local a la mancomunidad, sin identidad global ni búsqueda de menores desde la plataforma. |
| Instrumentos | Varios por alumno, catálogo reutilizable y gestión administrativa. |
| Matrículas | Matricular, dar de baja y reactivar en asignaturas, con histórico por curso escolar; la baja no borra asistencias y excluye de listas futuras. |
| Cursos escolares | Curso activo, planificación, cierre con preflight y reapertura con motivo; un único curso activo por mancomunidad. |
| Horarios | El profesor titular y el administrador pueden montar y finalizar el horario de un profesor mientras está en borrador, para cubrir las dos formas de trabajo; después los cambios se solicitan o se reabren con motivo y auditoría. Incluye copia explícita al curso siguiente y sustituciones por asignatura y periodo. |
| Solicitudes | El profesor solicita cambios sobre un horario finalizado; el administrador aprueba o rechaza y queda registro. |
| Observaciones | El profesor registra; el administrador consulta. |
| Asistencia | Sesiones de clase, lista masiva editable siempre, cancelación de clases no impartidas con motivo y historial de cambios visible solo para administración, conservado mientras la matrícula está activa y 2 años desde la baja. |
| Calendario | Días no lectivos por mancomunidad o centro que el generador de sesiones respeta y que cancelan las clases pendientes sin lista. |
| Preparación | Planificación de clases con estados y consulta administrativa. |
| Consultas | Filtros, búsquedas, paginación y vistas semanales, siempre dentro de la mancomunidad activa. |

## 6. Fuera de alcance (v1)

> [!warning] No entra en esta versión
> - Facturación o cobro por mancomunidad.
> - Autoservicio de alta de mancomunidades por terceros.
> - Panel de métricas avanzado o impersonación de soporte; el soporte administra cuentas, no datos académicos.
> - Notificaciones de negocio por email o push. El correo transaccional de recuperación de contraseña sí entra.
> - Aplicaciones móviles nativas: la distribución prevista es una **PWA instalable** desde un enlace (Fases 12–13).
> - Registro de jornada del profesorado (fichaje): fuera de la v1, planificado en las **Fases 12 y 13**.
> - Pagos, facturación o cobros.
> - Notas numéricas, boletines o expedientes académicos.
> - Importación/exportación masiva desde la interfaz (CSV/Excel). La exportación lógica por mancomunidad existe solo como script de operación ([[Copias de seguridad y exportación]]).
> - Firma digital o documentos adjuntos.
> - Multiidioma (la interfaz será en español).
> - Gestión de aulas como entidad propia (el aula será un texto libre de la franja).
> - Videollamadas o clases online.
> - Auditoría genérica de todos los cambios (solo se auditan la asistencia, las solicitudes de horario y los cambios de estado del horario).

Estas funcionalidades pueden convertirse en fases posteriores; no se implementan «por si acaso».

## 7. Criterios de éxito

- Un profesor puede crear su horario semanal y corregir conflictos sin ayuda.
- Pasar lista de una clase de 20 alumnos lleva menos de 2 minutos.
- La administración puede dar de alta un centro, una asignatura y un alumno sin tocar la base de datos.
- Cualquier profesor ve únicamente sus datos; cualquier administrador ve el conjunto de su mancomunidad y nada de las demás.
- Un profesor con pertenencia en dos mancomunidades entra con una sola cuenta y elige con cuál trabajar.
- Un usuario que olvida su contraseña la recupera por email sin intervención del soporte.
- La aplicación es usable en escritorio y tableta.
- No hay vulnerabilidades críticas conocidas en la v1.

## 8. Supuestos

- La plataforma puede alojar varias mancomunidades en una base de datos compartida, aisladas por `mancomunidad_id`.
- Una mancomunidad tiene varios centros.
- Dos roles de mancomunidad (`PROFESOR` y `ADMINISTRADOR`) y un rol de plataforma (`SUPERADMIN`) con soporte de cuentas.
- Hay un canal de correo transaccional para la recuperación de contraseña.
- Un usuario puede pertenecer a varias mancomunidades con roles distintos.
- Hay un curso escolar activo **por mancomunidad**; los anteriores se consultan como histórico.
- La interfaz y los mensajes están en español.
- El despliegue inicial es local con Docker Compose; producción se definirá en la Fase 10.

## 9. Relación con el proyecto anterior

> [!note] Proyecto nuevo desde cero
> El proyecto PHP `ProyectoFinalDAW` se toma solo como referencia de dominio. **No se reutiliza código, esquema ni datos.** Si en el futuro se quisiera migrar la información, se planificaría como una fase independiente con su propio análisis.

## 10. Dudas resueltas

- ~~¿Puede la administración reabrir un horario finalizado?~~ **Resuelto: sí, solo un administrador, solo desde `FINALIZADO`, con motivo, auditoría y cancelación de solicitudes pendientes.**
- ~~¿Cuánto se conserva el historial de asistencia?~~ **Resuelto: mientras la matrícula está activa y 2 años desde la baja; después se purga y solo quedan contadores anonimizados en [[Resumen de asistencia]] ([[ADR-019 - Resumen de asistencia y purga anonimizada]]).**
- ~~¿Métricas de plataforma en tiempo real o bajo demanda?~~ **Resuelto: bajo demanda (centros, usuarios y alumnos activos).**
- ~~¿Cómo se registra la jornada laboral del profesorado?~~ **Resuelto: fichaje con hora del servidor y auditoría, fuera de la v1, en Fases 12–13, distribuido como PWA.** Véase [[Control horario]].
- ~~¿Cómo recupera un usuario su contraseña?~~ **Resuelto: enlace de un solo uso por correo, de 60 minutos; la contraseña temporal del reset caduca en 24 horas y obliga a cambiarla.**
- ~~¿El superadministrador puede tocar cuentas de cualquier mancomunidad?~~ **Resuelto: sí, en el plano de cuentas (reset, alta y baja, rol), con motivo y auditoría; nunca en el académico ni con impersonación en la v1.**
- ~~¿Quién puede construir el horario de un profesor?~~ **Resuelto: el profesor titular y cualquier administrador de su mancomunidad, mientras está en `BORRADOR`; después solo solicitud o reapertura con motivo ([[ADR-016 - Horario editable por profesor y administrador]]).**
- ~~¿Cómo se representan una clase no impartida y un festivo?~~ **Resuelto: sesión `CANCELADA` con motivo y autor, y [[Día no lectivo]] por curso y centro opcional ([[ADR-017 - Cancelación de sesiones y días no lectivos]]).**
- ~~¿Las observaciones necesitan curso escolar?~~ **Resuelto: sí, obligatorio y con la fecha dentro del curso ([[ADR-018 - Curso escolar en las observaciones]]).**
- ~~¿Se valida que un alumno no tenga dos clases a la misma hora?~~ **Resuelto: sí, aunque sean grupos distintos ([[ADR-021 - Validación de conflictos de alumno]]).**
- ~~¿Cómo se reutiliza el horario de un curso a otro?~~ **Resuelto: con una copia explícita a `BORRADOR`, sin sesiones ni solicitudes y omitiendo franjas no válidas ([[ADR-023 - Copia de horario al curso siguiente]]).**
- ~~¿Qué ocurre al cerrar un curso?~~ **Resuelto: no se borra nada; se cancelan las sesiones `PROGRAMADA` sin lista, se congelan horarios y matrículas y la reapertura es administrativa y motivada ([[ADR-024 - Cierre y reapertura de curso escolar]]).**
- ~~¿Cómo se refleja un cambio de profesor a mitad de curso?~~ **Resuelto: con una sustitución por asignatura y periodo que no cambia el horario y solo reasigna las sesiones ([[ADR-025 - Sustituciones y cambio de titular]]).**
- ~~¿Qué pasa si un alumno causa baja a mitad de curso?~~ **Resuelto: conserva su historial, sale de las listas futuras y vuelve si se reactiva ([[ADR-026 - Baja de alumno a mitad de curso]]).**
- ~~¿El alumno tiene identidad global entre mancomunidades?~~ **Resuelto: no; es local al tenant y no se deduplica.**
- ~~¿Se migran datos existentes de centros que ya tienen información?~~ **Resuelto: no hay importación en la v1. Si un centro llega con datos, se crea la mancomunidad vacía y se cargan manualmente o mediante el script de exportación/importación de operación, filtrado por `mancomunidad_id` y sin hashes ni tokens; la importación masiva desde la interfaz queda fuera de alcance ([[Copias de seguridad y exportación]]).**

> [!success] Decisiones ya cerradas
> MySQL 8.4, flujo borrador → finalizado → solicitud, horario construible por el profesor titular o un administrador y bloqueado tras finalizar, reapertura administrativa auditada, aula como texto libre, asistencia editable siempre con historial y retención de 2 años tras la baja, curso escolar como entidad ligera, **multi-tenant con base compartida e identidad global**, **recuperación de contraseña por enlace de un solo uso**, **soporte de cuentas del superadministrador sin acceso académico** y **control horario diferido a Fases 12–13**.

## 11. Páginas legales públicas

La v1 incluirá, accesibles sin autenticación y enlazadas desde el pie de la aplicación y desde el login:

- **Aviso legal:** titular de la plataforma, datos de contacto, NIF/CIF, domicilio y condiciones de uso del servicio.
- **Política de privacidad:** finalidad, base jurídica, categorías de datos, conservación, derechos del interesado y contacto del responsable, enlazando a [[Privacidad y tratamiento de datos]].
- **Condiciones de uso:** aceptación por parte del usuario, usos permitidos y prohibidos, responsabilidad y suspensión de cuentas.

Contenido mínimo y redacción final se revisan antes de la preproducción. Las páginas no requieren autenticación y no exponen datos personales.

[[Roles y permisos]] · [[Requisitos funcionales]] · [[Requisitos no funcionales]] · [[Roadmap]] · [[Mancomunidad]] · [[Asignatura]] · [[Alumno]] · [[Instrumento]] · [[Matrícula]] · [[Curso escolar]] · [[Día no lectivo]] · [[Sustitución]] · [[Resumen de asistencia]] · [[Control horario]] · [[Multi-tenencia]] · [[ADR-001 - Stack tecnológico]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-010 - Control horario diferido y PWA]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]] · [[ADR-016 - Horario editable por profesor y administrador]] · [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[ADR-018 - Curso escolar en las observaciones]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]] · [[ADR-021 - Validación de conflictos de alumno]] · [[ADR-022 - Conflictos de profesor entre mancomunidades]] · [[ADR-023 - Copia de horario al curso siguiente]] · [[ADR-024 - Cierre y reapertura de curso escolar]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[ADR-026 - Baja de alumno a mitad de curso]]
