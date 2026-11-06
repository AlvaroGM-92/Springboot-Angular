---
tipo: documento
estado: activo
fase: 0
actualizado: 2026-11-04
tags:
  - glosario
  - dominio
---

# Glosario

Términos del dominio tal y como se usan en esta documentación. Si un término cambia de significado, se actualiza aquí y se avisa en el [[Diario de cambios]].

| Término | Definición | Nota |
| --- | --- | --- |
| **Mancomunidad** | Agrupación de varios centros educativos que comparten administración y profesorado. Es el **tenant** del sistema: la unidad de aislamiento de datos. | [[Mancomunidad]] |
| **Tenant** | Ámbito de datos aislado. En este proyecto, cada mancomunidad es un tenant identificado por `mancomunidad_id`. | [[Multi-tenencia]] |
| **Código de mancomunidad** | Etiqueta legible única de la mancomunidad. Se fija al crearla, no cambia, no se reutiliza y no decide el tenant. | [[Mancomunidad]] |
| **Usuario** | Identidad global de una persona: email, contraseña y estado. No tiene rol global. | [[Usuario]] |
| **UsuarioMancomunidad** | Pertenencia de un usuario a una mancomunidad, con su rol (`PROFESOR` o `ADMINISTRADOR`) y estado. | [[UsuarioMancomunidad]] |
| **Superadministrador** | Rol de plataforma (`SUPERADMIN`) que crea, suspende y reactiva mancomunidades y da soporte a las cuentas de cualquier mancomunidad, sin acceder a sus datos académicos. | [[Roles y permisos]] |
| **Soporte de cuentas** | Plano de plataforma para buscar usuarios, resetear contraseñas, activar o desactivar cuentas y pertenencias y cambiar roles, con motivo y auditoría. No incluye datos académicos ni impersonación. | [[ADR-012 - Soporte de cuentas del superadministrador]] |
| **Recuperación de contraseña** | Autoservicio del login que envía un enlace de un solo uso al email para que el usuario elija una contraseña nueva. | [[ADR-011 - Recuperación de contraseña y correo transaccional]] |
| **Contraseña temporal** | Contraseña generada por un administrador o el soporte al resetear una cuenta. Caduca en 24 horas y obliga a cambiarla antes de operar. | [[Usuario]] |
| **Bloqueo de cuenta** | Bloqueo temporal del login por intentos fallidos: 5 por cuenta o 20 por IP en 15 minutos, durante 15 minutos. | [[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]] |
| **Verificación de email** | Confirmación de que el titular controla la dirección, mediante un enlace de un solo uso de 24 horas. Habilita la recuperación autoservicio. | [[ADR-028 - Verificación y cambio de email]] |
| **Familia de refresh** | Conjunto de refresh tokens emitidos al rotar una misma sesión, identificados por `familia_id`. Se revoca entera al cerrar sesión o detectar reutilización. | [[ADR-030 - Detección de reutilización de refresh tokens]] |
| **Reutilización de refresh** | Uso de un refresh ya rotado. Se interpreta como robo: revoca la familia, invalida los access tokens y se audita. | [[ADR-030 - Detección de reutilización de refresh tokens]] |
| **Versión de token** | Contador `token_version` de la cuenta o de la pertenencia que viaja en el access token y permite revocarlo en menos de 60 segundos. | [[ADR-029 - Claves JWT, rotación y secretos]] |
| **Idempotencia** | Garantía de que reintentar la misma operación con la misma `Idempotency-Key` no duplica el efecto y devuelve la respuesta guardada. | [[ADR-033 - Idempotencia de peticiones sensibles]] |
| **Rate limiting** | Límite de peticiones por sujeto y ventana que responde `429` con `Retry-After` para frenar abuso sin revelar si una cuenta existe. | [[ADR-032 - Rate limiting de extremos sensibles]] |
| **Bootstrap del superadministrador** | Comando explícito y auditado que crea el primer `SUPERADMIN` sin contraseña sembrada; sirve también de break-glass. | [[ADR-034 - Bootstrap del primer superadministrador]] |
| **MFA** | Segundo factor de autenticación. Queda fuera de la v1 como riesgo aceptado; la previsión es TOTP obligatorio para plataforma. | [[ADR-027 - MFA y riesgo aceptado del superadministrador]] |
| **Correo transaccional** | Correo necesario para una operación del sistema, como la recuperación de contraseña. No es una notificación de negocio. | [[ADR-011 - Recuperación de contraseña y correo transaccional]] |
| **Selección de mancomunidad** | Paso del login en el que un usuario con varias pertenencias elige con cuál trabajar. | [[Multi-tenencia]] |
| **Centro** | Centro de enseñanza perteneciente a la mancomunidad, de cualquier área. Tiene alumnos y asignaturas. | [[Centro]] |
| **Curso escolar** | Periodo lectivo identificado como `2026-2027`. Va del 1 de septiembre al 31 de agosto y tiene estado `PLANIFICACION`, `ACTIVO` o `CERRADO`. Al cerrar se cancela lo programado sin lista y se congela el resto sin borrar datos. | [[Curso escolar]] |
| **Asignatura** | Materia impartida en un centro, con un nivel, un grupo y un tipo de área (por ejemplo, «Lenguaje musical · 2º · A»). | [[Asignatura]] |
| **Grupo** | Subdivisión de un curso dentro de una asignatura (A, B, …). | [[Asignatura]] |
| **Profesor** | Usuario con pertenencia `PROFESOR` en una mancomunidad. Imparte asignaturas, gestiona su horario, pasa lista y registra seguimiento de sus alumnos. | [[UsuarioMancomunidad]] |
| **Administrador** | Usuario con pertenencia `ADMINISTRADOR` en una mancomunidad. Gestiona usuarios, centros, cursos, asignaturas, alumnos, matrículas y sustituciones; construye, finaliza y reabre el horario de cualquier profesor, y consulta observaciones, asistencias y preparaciones sin editarlas. | [[UsuarioMancomunidad]] · [[ADR-016 - Horario editable por profesor y administrador]] · [[ADR-025 - Sustituciones y cambio de titular]] |
| **Alumno** | Persona matriculada en una o varias asignaturas de un centro. | [[Alumno]] |
| **Tutor** | Persona de contacto del alumno (padre, madre o tutor legal). | [[Alumno]] |
| **Instrumento** | Instrumento musical que practica un alumno. El catálogo crece según se usa y solo está visible si el módulo de instrumentos está activo. | [[Instrumento]] |
| **Tipo de mancomunidad** | Orientación principal de la mancomunidad (`MUSICA`, `DANZA`, `ARTES_ESCENICAS`, `IDIOMAS`, `DEPORTES` u `OTRO`). Es informativo y no limita las asignaturas. | [[Mancomunidad]] |
| **Tipo de asignatura** | Área de una asignatura, de la misma lista cerrada. Decide, junto con `requiereInstrumento`, si esa asignatura usa instrumentos. | [[Asignatura]] |
| **Módulo de instrumentos** | Funcionalidad opcional, activa solo si existe al menos una asignatura activa instrumental. Si se desactiva, se oculta sin borrar datos. | [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]] |
| **Matrícula** | Vinculación de un alumno a una asignatura durante un curso escolar. Puede estar `ACTIVA` o `BAJA`; la baja conserva el histórico y solo excluye de las listas futuras. | [[Matrícula]] |
| **Franja horaria** | Bloque de clase con día, hora de inicio, hora de fin, asignatura y aula. | [[Franja horaria]] |
| **Horario del profesor** | Conjunto de franjas de un profesor durante un curso escolar. Tiene estado `BORRADOR` o `FINALIZADO` y se puede copiar explícitamente a otro curso. | [[Horario del profesor]] |
| **Curso activo** | Curso escolar en estado `ACTIVO`. Solo puede haber uno por mancomunidad; es el que se usa para nuevas matrículas, horarios y preparaciones. | [[Curso escolar]] |
| **Día no lectivo** | Fecha sin actividad lectiva para toda la mancomunidad o para un centro concreto; el generador no crea sesiones y cancela las `PROGRAMADA` sin asistencia de su ámbito. | [[Día no lectivo]] |
| **Horario en borrador** | Horario que se está construyendo y se puede guardar, editar y completar por partes. Lo pueden montar el profesor titular y cualquier administrador de su mancomunidad. | [[Horario del profesor]] |
| **Horario finalizado** | Horario cerrado por el profesor titular o por un administrador. Su edición directa se bloquea para ambos y los cambios requieren solicitud o una reapertura administrativa. | [[Horario del profesor]] |
| **Reapertura de horario** | Acción de un administrador que devuelve un horario `FINALIZADO` a `BORRADOR`, con motivo y auditoría, sin alterar las franjas y cancelando las solicitudes pendientes. | [[Historial de horario]] |
| **Historial de horario** | Traza inmutable de creación, finalización, reapertura y copia de un horario, con autor, fecha y motivo. | [[Historial de horario]] |
| **Copia de horario** | Acción explícita que crea un horario `BORRADOR` en otro curso con las franjas válidas, informa de las omitidas y no arrastra sesiones ni solicitudes. | [[ADR-023 - Copia de horario al curso siguiente]] |
| **Sustitución** | Periodo en que un profesor sustituto imparte una asignatura en lugar de su titular. No cambia el horario y solo afecta a las sesiones del periodo. | [[Sustitución]] |
| **Baja de matrícula** | Cambio de estado a `BAJA` que conserva asistencias e historial, excluye al alumno de las listas futuras y programa la purga a dos años. | [[ADR-026 - Baja de alumno a mitad de curso]] |
| **Curso cerrado** | Curso `CERRADO` en el que no se crean matrículas, horarios, preparaciones, observaciones, días no lectivos ni sustituciones; solo se corrigen asistencias existentes. | [[Curso escolar]] |
| **Sesión de clase** | Encuentro concreto de una asignatura en una fecha. Puede generarse desde una franja o crearse puntualmente. | [[Sesión de clase]] |
| **Sesión programada** | Sesión generada automáticamente desde una franja del horario, todavía sin lista de asistencia. | [[Sesión de clase]] |
| **Sesión cancelada** | Sesión `PROGRAMADA` que no se imparte, con motivo, autor y fecha. No tiene lista de asistencia, no cuenta en los contadores y no se reabre. | [[Sesión de clase]] |
| **Conflicto de alumno** | Dos franjas solapadas del mismo curso que comparten al menos un alumno con matrícula `ACTIVA`, aunque sean de grupos distintos. | [[ADR-021 - Validación de conflictos de alumno]] |
| **Asistencia** | Estado de un alumno en una sesión: presente, ausente, ausencia justificada o retraso. | [[Asistencia]] |
| **Historial de asistencia** | Registro inmutable de cada cambio de asistencia, con valores anterior y nuevo, autor y fecha. Solo lo consulta la administración. Se conserva mientras la matrícula está activa y 2 años desde la baja. | [[Historial de asistencia]] |
| **Resumen de asistencia** | Contadores anonimizados por alumno, asignatura y curso que quedan tras purgar asistencias e historial; usa `alumno_hash`, no `alumno_id`. | [[Resumen de asistencia]] |
| **Falta** | Ausencia de un alumno a una sesión. | [[Asistencia]] |
| **Retraso** | Llegada tarde de un alumno a una sesión. | [[Asistencia]] |
| **Observación** | Anotación de un profesor sobre un alumno (conducta, rendimiento, asistencia u otra). | [[Observación]] |
| **Preparación de clase** | Planificación docente de una sesión o fecha: objetivos, contenidos, recursos y estado. | [[Preparación de clase]] |
| **Rejilla semanal** | Vista de lunes a domingo con las franjas de un profesor o de un grupo. La del profesor es editable en `BORRADOR` por el profesor titular o un administrador; la del grupo es siempre de solo lectura. | [[Horario del profesor]] |
| **Rejilla por grupo** | Rejilla de solo lectura que muestra todas las asignaturas del mismo centro, curso y grupo, con el profesor de cada franja. | [[Horario del profesor]] |
| **Solicitud de cambio** | Petición del profesor para crear, modificar o eliminar una franja de un horario finalizado; la resuelve la administración y queda registrada. | [[Solicitud de cambio de horario]] |
| **Fichaje** | Registro de entrada o de salida de la jornada laboral del profesor, con la hora del servidor y el centro donde se produce. | [[Control horario]] |
| **Jornada abierta** | Situación del profesor que ha fichado la entrada y todavía no ha fichado la salida. | [[Control horario]] |
| **Rol** | Perfil de permisos. En una mancomunidad: `PROFESOR` o `ADMINISTRADOR`; en la plataforma: `SUPERADMIN`. | [[Roles y permisos]] |
| **Borrado lógico** | Desactivar un registro (`activo = false`) sin eliminarlo físicamente, para conservar el histórico. | [[Modelo de datos]] |
| **Bloqueo optimista** | Control de concurrencia que compara `version` en cada actualización y rechaza con `409` si otro usuario la cambió antes. | [[Modelo de datos]] |
| **Copia de seguridad** | Volcado lógico completo de la base, cifrado, con 30 copias diarias y 12 mensuales. | [[Copias de seguridad y exportación]] |
| **Exportación lógica** | Extracción de los datos de una mancomunidad mediante un script, filtrada por `mancomunidad_id` y sin credenciales. | [[Copias de seguridad y exportación]] |
| **Retención** | Periodo durante el que se conservan los datos. El historial de asistencia se mantiene mientras la matrícula está activa y 2 años después de la baja; los fichajes de jornada, 4 años. | [[Historial de asistencia]] · [[Resumen de asistencia]] · [[Control horario]] |
| **PWA** | Aplicación web instalable desde el navegador, sin pasar por las tiendas, que se actualiza al abrirse. Es la vía de distribución prevista para el fichaje. | [[ADR-010 - Control horario diferido y PWA]] |
| **ADR** | *Architecture Decision Record*: nota que documenta una decisión técnica y sus consecuencias. | [[Plantilla - ADR]] |
