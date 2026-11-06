---
tipo: documento
estado: activo
fase: 0
actualizado: 2026-11-04
tags:
  - proyecto
  - requisitos
---

# Requisitos funcionales

> [!info] Cómo leer esta tabla
> Los IDs son estables y no se reutilizan. La columna **Fase** indica cuándo se implementa. Todos los requisitos están **pendientes** hasta que su fase se complete; el estado real se sigue en [[Roadmap]].
>
> Prioridad: **M** = imprescindible, **S** = importante, **C** = deseable.

## Autenticación y sesión

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| AUTH-01 | Iniciar sesión con email y contraseña contra la identidad global. | Todos | M | 2 |
| AUTH-02 | Cerrar sesión revocando la familia de refresh de la sesión activa. | Todos | M | 2 |
| AUTH-03 | Distinguir los roles `PROFESOR` y `ADMINISTRADOR` dentro de cada mancomunidad y el rol de plataforma `SUPERADMIN`. | Sistema | M | 2 |
| AUTH-04 | Proteger todos los endpoints según mancomunidad activa, rol y propiedad del recurso. | Sistema | M | 2 |
| AUTH-05 | Renovar el token de acceso sin volver a pedir credenciales. | Todos | M | 2 |
| AUTH-06 | Cambiar la propia contraseña indicando la actual. | Todos | M | 2 |
| AUTH-07 | Permitir al administrador resetear la contraseña de un usuario de su mancomunidad y limpiar su bloqueo por intentos fallidos. | Admin | S | 3 |
| AUTH-08 | Tras validar las credenciales, detectar las pertenencias activas: 0 deniega el acceso, 1 entra directamente y varias muestran un selector. | Sistema | M | 2 |
| AUTH-09 | Emitir un token de selección de corta vida y canjearlo por tokens de la mancomunidad elegida, validando la pertenencia. | Sistema | M | 2 |
| AUTH-10 | Permitir cambiar de mancomunidad activa sin volver a introducir la contraseña, rotando los tokens. | Todos | M | 2 |
| AUTH-11 | Permitir el acceso al panel de plataforma a un `SUPERADMIN`, aunque no tenga pertenencias de mancomunidad. | Superadmin | M | 11 |
| AUTH-12 | Permitir solicitar la recuperación de contraseña desde el login con el email, respondiendo siempre igual exista o no la cuenta y limitando las peticiones. | Todos | M | 3 |
| AUTH-13 | Restablecer la contraseña mediante un enlace de un solo uso, hasheado y con caducidad de 60 minutos, enviado por correo transaccional. | Todos | M | 3 |
| AUTH-14 | Exigir el cambio de contraseña al iniciar sesión cuando sea temporal o haya sido reseteada por un tercero, sin permitir operar hasta cambiarla. | Todos | M | 2 |
| AUTH-15 | Revocar todos los refresh tokens del usuario al cambiar o restablecer la contraseña. | Sistema | M | 2 |
| AUTH-16 | Bloquear temporalmente el login tras 5 fallos por cuenta o 20 por IP en 15 minutos, sin revelar si la cuenta existe. | Sistema | M | 2 |
| AUTH-17 | Permitir al `SUPERADMIN` desbloquear una cuenta con motivo y auditoría; el reset de contraseña del administrador también limpia el bloqueo. | Superadmin | S | 11 |
| AUTH-18 | Dejar la MFA fuera de la v1, incluida la cuenta de plataforma, y registrar el riesgo aceptado con controles compensatorios y revisión antes de producción. | Sistema | M | 2 |
| AUTH-19 | Verificar el email al crear la cuenta con un enlace de un solo uso de 24 horas; la cuenta puede operar sin verificar, pero la recuperación autoservicio queda deshabilitada hasta confirmarlo. | Sistema | M | 3 |
| AUTH-20 | Permitir cambiar el email solo al titular con la contraseña actual o al soporte con motivo; el cambio exige confirmar la dirección nueva y la anterior sigue vigente hasta entonces. | Todos | M | 3 |
| AUTH-21 | Firmar los access tokens con RS256 e `kid`, publicar el JWKS y rotar la clave cada 90 días con 24 horas de solapamiento y rotación de emergencia. | Sistema | M | 2 |
| AUTH-22 | Revocar los access tokens en curso con `tokenVersion` de cuenta y pertenencia, comprobada con una caché máxima de 60 segundos. | Sistema | M | 2 |
| AUTH-23 | Detectar la reutilización de un refresh ya rotado, revocar toda la familia, incrementar `tokenVersion`, auditar el evento y responder `401`. | Sistema | M | 2 |
| AUTH-24 | Limitar a 5 las familias de refresh activas por cuenta, listar las sesiones sin tokens y permitir cerrarlas todas con la contraseña actual. | Todos | S | 2 |
| AUTH-25 | Aplicar rate limiting a recuperación, reenvío de verificación, refresh, escrituras y soporte, con `429`, `Retry-After` y sin revelar si la cuenta existe. | Sistema | M | 2 |
| AUTH-26 | Crear el primer `SUPERADMIN` con un comando de bootstrap explícito, sin contraseña sembrada, y usarlo también como break-glass auditado. | Superadmin | M | 11 |

## Mancomunidades y plataforma

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| MAN-01 | Listar las mancomunidades con estado, código y métricas básicas. | Superadmin | M | 11 |
| MAN-02 | Crear una mancomunidad con nombre, código, tipo y primer administrador. | Superadmin | M | 11 |
| MAN-03 | Sembrar al crear una mancomunidad su curso escolar inicial activo y, opcionalmente, un centro de ejemplo. | Sistema | M | 11 |
| MAN-04 | Suspender una mancomunidad e impedir el acceso y la operación en ella. | Superadmin | M | 11 |
| MAN-05 | Reactivar una mancomunidad suspendida. | Superadmin | M | 11 |
| MAN-06 | Validar que el nombre y el código de la mancomunidad son únicos en la plataforma. | Sistema | M | 11 |
| MAN-07 | Auditar altas, ediciones, suspensiones y reactivaciones de mancomunidades. | Sistema | M | 11 |
| MAN-08 | Impedir que un administrador de mancomunidad cree o gestione otras mancomunidades. | Sistema | M | 11 |
| MAN-09 | Impedir cambiar el código de una mancomunidad y reutilizarlo en otra; el código solo es una etiqueta legible y no decide el tenant. | Sistema | M | 11 |
| MAN-10 | Asignar a la mancomunidad un tipo de la lista cerrada (`MUSICA`, `DANZA`, `ARTES_ESCENICAS`, `IDIOMAS`, `DEPORTES`, `OTRO`) al crearla; el tipo es informativo y no limita las asignaturas. | Superadmin | M | 11 |
| MAN-11 | Permitir que un `SUPERADMIN` cambie el tipo de una mancomunidad con motivo y auditoría, sin alterar sus asignaturas. | Superadmin | S | 11 |

## Usuarios

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| USR-01 | Listar, buscar y filtrar los usuarios de la mancomunidad activa por rol y estado. | Admin | M | 3 |
| USR-02 | Crear profesores y administradores y asignarles una pertenencia con rol en la mancomunidad. | Admin | M | 3 |
| USR-03 | Editar datos de un usuario sin exponer la contraseña. | Admin | M | 3 |
| USR-04 | Activar y desactivar la pertenencia de un usuario sin borrarlo físicamente. | Admin | M | 3 |
| USR-05 | Validar email único global, formato y campos obligatorios. | Sistema | M | 3 |
| USR-06 | Impedir desactivar la propia pertenencia o el último administrador activo de la mancomunidad. | Sistema | M | 3 |
| USR-07 | Permitir que un administrador consulte y cambie el rol de una pertenencia dentro de su mancomunidad. | Admin | S | 3 |
| USR-08 | Impedir que un administrador vea o modifique pertenencias, datos o usuarios de otra mancomunidad. | Sistema | M | 3 |
| USR-09 | Crear usuarios sin pertenencia solo desde la plataforma, para casos de soporte o alta posterior. | Superadmin | C | 11 |
| USR-10 | Impedir que el administrador de mancomunidad cambie el email de un usuario de su tenant; la dirección solo la cambia el titular con la contraseña actual o el soporte con motivo y doble confirmación. | Sistema | M | 3 |

## Soporte de cuentas (plataforma)

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| SOP-01 | Buscar cualquier usuario global por email o nombre y consultar sus pertenencias, rol y estado. | Superadmin | M | 11 |
| SOP-02 | Resetear la contraseña de cualquier usuario generando una temporal con cambio obligatorio. | Superadmin | M | 11 |
| SOP-03 | Activar o desactivar la cuenta global y cada pertenencia de un usuario. | Superadmin | M | 11 |
| SOP-04 | Cambiar el rol de una pertenencia entre `PROFESOR` y `ADMINISTRADOR`, con motivo. | Superadmin | S | 11 |
| SOP-05 | Auditar cada acción de soporte con autor, afectado, mancomunidad, acción y valores anterior y nuevo. | Sistema | M | 11 |
| SOP-06 | Impedir que el soporte lea o modifique datos académicos y que impersone a un usuario en la v1. | Sistema | M | 11 |
| SOP-07 | Impedir conceder o retirar `admin_plataforma` desde una mancomunidad y dejar la plataforma sin superadministradores activos. | Sistema | M | 11 |

## Centros

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| CEN-01 | Crear, consultar y editar centros. | Admin | M | 3 |
| CEN-02 | Validar nombre único dentro de la mancomunidad y datos de contacto. | Sistema | M | 3 |
| CEN-03 | Activar y desactivar centros con comprobación de dependencias activas. | Admin | M | 3 |
| CEN-04 | Buscar y filtrar centros por estado. | Admin | S | 3 |

## Cursos escolares

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| CUR-01 | Consultar el curso escolar activo y su código. | Ambos | M | 1 |
| CUR-02 | Crear cursos escolares con código, fecha de inicio y fecha de fin. | Admin | M | 3 |
| CUR-03 | Activar un curso y garantizar que solo haya uno activo por mancomunidad. | Admin | M | 3 |
| CUR-04 | Cerrar un curso con preflight: cancelar las sesiones `PROGRAMADA` sin asistencia con motivo «Cierre del curso», congelar horarios y matrículas y no borrar datos. | Admin | M | 3 |
| CUR-05 | Reabrir un curso cerrado con motivo, cerrando el curso activo en la misma transacción si existe y sin reactivar sesiones canceladas. | Admin | S | 3 |
| CUR-06 | Impedir matrículas, horarios, preparaciones, observaciones, días no lectivos y sustituciones nuevos en cursos cerrados; permitir solo la corrección de asistencias existentes. | Sistema | M | 3/6/8/9 |
| CUR-07 | Devolver en el cierre `sesionesCanceladas` y `horariosCongelados` y rechazar con `409` un curso que ya está `CERRADO` o que sigue en `PLANIFICACION`. | Sistema | M | 3 |
| CUR-08 | Impedir la generación retroactiva de sesiones al reabrir: el generador vuelve a crear desde la fecha de reapertura, no desde el cierre. | Sistema | M | 3/8 |

## Calendario y días no lectivos

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| CAL-01 | Registrar días no lectivos por curso escolar y ámbito: toda la mancomunidad o un centro concreto. | Admin | M | 8 |
| CAL-02 | Impedir días no lectivos duplicados para el mismo ámbito o fuera del rango del curso. | Sistema | M | 8 |
| CAL-03 | Omitir las fechas no lectivas en la generación automática y cancelar las sesiones `PROGRAMADA` sin asistencia de su ámbito al registrarlas. | Sistema | M | 8 |
| CAL-04 | Consultar y eliminar días no lectivos; en un curso `CERRADO` el calendario es de solo lectura y eliminar un día no reactiva las sesiones ya canceladas. | Admin | M | 8 |

## Asignaturas y profesorado

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| ASG-01 | Crear, consultar y editar asignaturas con centro, nombre, curso, grupo, tipo y uso de instrumento. | Admin | M | 4 |
| ASG-02 | Validar que no se duplique nombre + curso + grupo dentro de un centro de la misma mancomunidad. | Sistema | M | 4 |
| ASG-03 | Asignar y retirar profesores con pertenencia activa en la mancomunidad. | Admin | M | 4 |
| ASG-04 | Activar y desactivar asignaturas con comprobación de matrículas activas. | Admin | M | 4 |
| ASG-05 | Buscar y filtrar asignaturas por centro, profesor y estado. | Admin | S | 4 |
| ASG-06 | Ver únicamente las asignaturas que imparte el profesor. | Profesor | M | 4 |
| ASG-07 | Asignar a cada asignatura un tipo de la lista cerrada compartida con la mancomunidad, sin depender del tipo de esta. | Admin | M | 4 |
| ASG-08 | Impedir marcar `requiereInstrumento` en una asignatura que no sea de tipo `MUSICA`. | Sistema | M | 4 |
| ASG-09 | Activar y desactivar el módulo de instrumentos según exista al menos una asignatura activa instrumental, sin borrar datos. | Sistema | M | 4/5 |
| ASG-10 | Registrar una sustitución por asignatura y curso, con titular, sustituto, periodo inclusivo y motivo de 10–500 caracteres; no cambia el horario. | Admin | M | 4 |
| ASG-11 | Impedir sustituciones solapadas de la misma asignatura y validar que el sustituto tiene pertenencia activa y disponibilidad frente a su horario `FINALIZADO` y sus otras sustituciones, con `422` y `conflictos`. | Sistema | M | 4 |

## Alumnos, instrumentos y matrículas

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| ALU-01 | Crear, consultar y editar alumnos con centro y datos personales. | Admin | M | 5 |
| ALU-02 | Registrar y validar los datos de contacto del tutor. | Admin | M | 5 |
| ALU-03 | Activar y desactivar alumnos conservando su histórico. | Admin | M | 5 |
| ALU-04 | Ver una ficha del alumno con asignaturas, instrumentos, observaciones y asistencias. | Ambos | M | 5/8 |
| ALU-05 | Buscar y filtrar alumnos por centro, instrumento y estado. | Ambos | S | 5 |
| ALU-06 | Ver solo los alumnos matriculados en sus asignaturas. | Profesor | M | 5 |
| ALU-07 | Bloquear el cambio de centro mientras haya matrículas activas. | Sistema | M | 5 |
| ALU-08 | Mostrar la sección de instrumentos solo cuando el módulo esté activo en la mancomunidad. | Ambos | M | 5 |
| ALU-09 | Tratar al alumno como local a la mancomunidad, sin identidad global ni deduplicación entre tenants, y sin búsqueda de menores desde la plataforma. | Sistema | M | 5 |
| INS-01 | Asociar varios instrumentos a un alumno. | Admin | M | 5 |
| INS-02 | Sugerir instrumentos existentes y crear uno nuevo al escribirlo. | Admin | M | 5 |
| INS-03 | Normalizar nombres y limitar a 10 instrumentos y 80 caracteres por nombre. | Sistema | M | 5 |
| INS-04 | Listar el catálogo con número de alumnos, renombrar y fusionar duplicados. | Admin | M | 5 |
| INS-05 | Activar y desactivar instrumentos; reactivar automáticamente si se vuelven a usar. | Admin | M | 5 |
| INS-06 | Filtrar alumnos por instrumento. | Ambos | S | 5 |
| INS-07 | Ocultar el catálogo y la sección de instrumentos del alumno cuando el módulo esté inactivo y rechazar sus operaciones con `422`, conservando los datos existentes. | Sistema | M | 5 |
| MAT-01 | Matricular un alumno en asignaturas activas del mismo centro y del curso escolar activo. | Admin | M | 5 |
| MAT-02 | Dar de baja una matrícula conservando el histórico. | Admin | M | 5 |
| MAT-03 | Consultar matrículas por curso escolar. | Admin | M | 5 |
| MAT-04 | Evitar matrículas duplicadas para el mismo alumno, asignatura y curso. | Sistema | M | 5 |
| MAT-05 | Impedir matrículas que mezclen alumno, asignatura o curso de mancomunidades distintas. | Sistema | M | 5 |
| MAT-06 | Rechazar una matrícula o reactivación que genere un conflicto de alumno con un horario `FINALIZADO` del mismo curso, con el detalle de los solapes en `conflictos`. | Sistema | M | 5/6 |
| MAT-07 | Dar de baja una matrícula sin borrar asistencias ni historial, excluyendo al alumno de las listas que se abran desde la fecha de baja. | Admin | M | 5/8 |
| MAT-08 | Reactivar una matrícula limpiando `bajaAt`, `purgaAt` y `purgadaAt`, reincorporando al alumno a las listas futuras sin añadir filas a sesiones ya abiertas. | Admin | M | 5/8 |

## Horarios

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| HOR-01 | Crear, editar y eliminar sus propias franjas horarias mientras el horario está en borrador. | Profesor | M | 6 |
| HOR-02 | Definir asignatura, día (lunes a domingo), hora de inicio, hora de fin y aula. | Profesor/Admin | M | 6 |
| HOR-03 | Validar que la hora de fin sea posterior a la de inicio y que la franja esté dentro del curso. | Sistema | M | 6 |
| HOR-04 | Impedir solapamientos de un mismo profesor. | Sistema | M | 6 |
| HOR-05 | Impedir solapamientos de un mismo grupo (centro + curso + grupo). | Sistema | M | 6 |
| HOR-06 | Impedir franjas de asignaturas que el profesor no imparte o que estén inactivas. | Sistema | M | 6 |
| HOR-07 | Consultar los horarios de todos los profesores con filtros por centro, profesor, día y curso. | Admin | M | 6 |
| HOR-08 | Ver la rejilla semanal de lunes a domingo de un profesor, con selector de curso escolar; el profesor titular y el administrador la editan mientras está en `BORRADOR` y la consultan en solo lectura cuando está `FINALIZADO`. | Ambos | M | 6/7 |
| HOR-09 | Guardar el horario de forma parcial en estado `BORRADOR` mientras se planifica. | Profesor/Admin | M | 6 |
| HOR-10 | Finalizar el horario cuando esté completo; puede hacerlo el profesor titular o un administrador y, a partir de ese momento, se bloquea la edición directa salvo reapertura administrativa. | Profesor/Admin | M | 6 |
| HOR-11 | Consultar el estado del horario (borrador/finalizado) y la fecha de finalización. | Ambos | M | 6 |
| HOR-12 | Aislar horarios, franjas y solicitudes por mancomunidad activa. | Sistema | M | 6 |
| HOR-13 | Permitir al administrador reabrir un horario `FINALIZADO` con motivo obligatorio, auditoría y cancelación de las solicitudes pendientes, sin alterar las franjas. | Admin | M | 6 |
| HOR-14 | Validar que la franja no supera la duración máxima opcional definida por la mancomunidad; sin máximo, solo se exige que el fin sea posterior al inicio. | Sistema | M | 6 |
| HOR-15 | Ver la rejilla semanal de un grupo con todas sus asignaturas, en solo lectura; el profesor solo accede a los grupos donde imparte alguna asignatura. | Ambos | M | 6/7 |
| HOR-16 | Crear el horario de cualquier profesor de su mancomunidad y editar sus franjas mientras esté en `BORRADOR`. | Admin | M | 6 |
| HOR-17 | Registrar `creadoPor` y `actualizadoPor` en el horario y en cada franja, tanto si los toca el profesor como la administración. | Sistema | S | 6 |
| HOR-18 | Impedir que el administrador edite franjas de un horario `FINALIZADO`; para cambiarlo debe reabrirlo con motivo y auditoría. | Sistema | M | 6 |
| HOR-19 | Tratar como inexistente (`404`) el horario de un profesor de otra mancomunidad, tanto para consulta como para escritura. | Sistema | M | 6 |
| HOR-20 | Impedir franjas solapadas del mismo curso que compartan al menos un alumno con matrícula `ACTIVA`, aunque sean de asignaturas o grupos distintos; se valida al crear o editar la franja, al finalizar el horario y al matricular. | Sistema | M | 6 |
| HOR-21 | Previsualizar la copia de un horario a otro curso con `franjasCopiables` y `franjasOmitidas`, sin escribir nada. | Admin/Profesor | S | 6 |
| HOR-22 | Copiar un horario a otro curso creando un `BORRADOR` nuevo, solo con franjas válidas, sin sesiones ni solicitudes, y registrar `COPIA` con `origenHorarioId`; `422` si no queda ninguna. | Admin/Profesor | M | 6 |

## Solicitudes de cambio de horario

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| SOL-01 | Crear una solicitud de creación, modificación o eliminación de una franja de un horario finalizado. | Profesor | M | 6 |
| SOL-02 | Indicar datos propuestos y motivo de la solicitud. | Profesor | M | 6 |
| SOL-03 | Permitir solo una solicitud pendiente por franja. | Sistema | M | 6 |
| SOL-04 | Listar y filtrar solicitudes por estado, profesor y fecha. | Admin | M | 6 |
| SOL-05 | Aprobar o rechazar con respuesta; al aprobar se revalidan conflictos y se aplica el cambio en una transacción. | Admin | M | 6 |
| SOL-06 | Consultar el estado de sus solicitudes; la administración conserva el histórico de cambios. | Profesor/Admin | M | 6 |
| SOL-07 | Mostrar un aviso interno (contador) en el panel del profesor cuando una de sus solicitudes cambia de estado; sin correo ni push en la v1. | Profesor | S | 6 |

## Observaciones

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| OBS-01 | Crear observaciones sobre alumnos de sus asignaturas. | Profesor | M | 8 |
| OBS-02 | Editar y eliminar sus propias observaciones. | Profesor | M | 8 |
| OBS-03 | Clasificar por tipo, fecha, asignatura opcional y texto validado. | Profesor | M | 8 |
| OBS-04 | Filtrar y consultar observaciones en la ficha del alumno. | Ambos | M | 8 |
| OBS-05 | Consultar todas las observaciones en modo solo lectura. | Admin | M | 8 |
| OBS-06 | Asociar cada observación a un curso escolar obligatorio; la API puede inferirlo de la fecha y responde `422` si no es único. | Sistema | M | 8 |

## Sesiones y asistencia

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| SES-01 | Abrir una sesión existente o crear una sesión puntual para una asignatura, fecha y hora. | Profesor | M | 8 |
| SES-02 | Cargar automáticamente todos los alumnos matriculados como presentes al abrir la sesión, no al generarla. | Sistema | M | 8 |
| SES-03 | Generar cada madrugada una sesión `PROGRAMADA` por cada franja del día, sin duplicados. | Sistema | M | 8 |
| SES-04 | Mantener como pendientes las sesiones sin lista y permitir abrirlas en cualquier fecha posterior. | Profesor | M | 8 |
| SES-05 | Actualizar las sesiones `PROGRAMADA` sin asistencia cuando cambie su franja; no alterar las que ya tienen lista. | Sistema | S | 8 |
| SES-06 | Cancelar una sesión `PROGRAMADA` con motivo de 10–500 caracteres y autor; el profesor titular y cualquier administrador pueden hacerlo y la cancelación administrativa queda auditada. | Profesor/Admin | M | 8 |
| SES-07 | Impedir asistencias y reapertura en una sesión `CANCELADA`; si la clase se imparte, se crea una sesión puntual nueva y la cancelada se conserva como histórico. | Sistema | M | 8 |
| SES-08 | Al borrar una franja, eliminar sus sesiones `PROGRAMADA` sin asistencia y conservar las que tienen lista con `franjaId` nulo y su instantánea propia. | Sistema | M | 8 |
| SES-09 | Registrar `cursoEscolarId` obligatorio en toda sesión, heredado de la franja o indicado en las puntuales. | Sistema | M | 8 |
| SES-10 | Asignar al sustituto y guardar `sustitucionId` en las sesiones cuya fecha cae dentro de una sustitución `ACTIVA`, sin cambiar las ya abiertas o cerradas. | Sistema | M | 8 |
| SES-11 | Cargar al abrir la sesión solo alumnos con matrícula `ACTIVA` en esa fecha; conservar las filas ya creadas en sesiones abiertas aunque el alumno cause baja después. | Sistema | M | 8 |
| AST-01 | Marcar en una sola pantalla faltas, ausencias justificadas y retrasos. | Profesor | M | 8 |
| AST-02 | Guardar toda la lista en una única operación. | Profesor | M | 8 |
| AST-03 | Editar la asistencia de una sesión propia en cualquier momento. | Profesor | M | 8 |
| AST-04 | Evitar duplicados de asistencia por sesión y alumno. | Sistema | M | 8 |
| AST-05 | Consultar asistencias y su historial por alumno, asignatura y rango de fechas. | Admin | M | 8 |
| AST-06 | Limitar la operación a alumnos de sus propias asignaturas. | Profesor | M | 8 |
| AST-07 | Mostrar contadores de faltas, retrasos y justificadas en listados y ficha. | Ambos | S | 8 |
| AST-08 | Registrar cada cambio de asistencia en un historial inmutable con valores anterior/nuevo, autor y fecha. | Sistema | M | 8 |
| AST-09 | Ocultar el historial de asistencia al profesor; solo la administración puede consultarlo. | Sistema | M | 8 |
| AST-10 | Impedir que la administración edite asistencias; solo puede consultarlas. | Sistema | M | 8 |
| AST-11 | Aislar sesiones, asistencias e historial por mancomunidad activa. | Sistema | M | 8 |
| AST-12 | Ofrecer un resumen de asistencia por alumno, asignatura y curso que incluya los contadores anonimizados cuando la matrícula ya fue purgada. | Ambos | S | 8 |
| AST-13 | Conservar la asistencia y su historial mientras la matrícula esté `ACTIVA` y 2 años desde la baja; después purgarlos y dejar solo [[Resumen de asistencia]] con `alumnoHash`. | Sistema | M | 10 |
| AST-14 | Rechazar con `409` cualquier alta o corrección de asistencia sobre una sesión `CANCELADA`. | Sistema | M | 8 |

## Preparación de clases

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| PRE-01 | Crear una preparación para una asignatura y fecha, con vínculo opcional a una sesión. | Profesor | M | 9 |
| PRE-02 | Registrar título, objetivos, contenido, recursos y observaciones. | Profesor | M | 9 |
| PRE-03 | Gestionar estados `BORRADOR`, `PREPARADA` e `IMPARTIDA`. | Profesor | M | 9 |
| PRE-04 | Editar sus propias preparaciones. | Profesor | M | 9 |
| PRE-05 | Eliminar solo preparaciones en estado `BORRADOR`. | Profesor | M | 9 |
| PRE-06 | Filtrar por asignatura, estado y rango de fechas. | Profesor | M | 9 |
| PRE-07 | Consultar todas las preparaciones, incluidas las de borrador, en solo lectura. | Admin | M | 9 |

## Sistema y experiencia de uso

| ID | Requisito | Rol | Prio | Fase |
| --- | --- | --- | --- | --- |
| SYS-01 | Paginar, ordenar y buscar en todos los listados. | Ambos | M | 3 |
| SYS-02 | Guardar campos de auditoría (`created_at`, `updated_at`, usuario). | Sistema | M | 1 |
| SYS-03 | Interfaz en español, responsive y usable con teclado. | Ambos | M | 2 |
| SYS-04 | Devolver errores homogéneos y comprensibles. | Sistema | M | 2 |
| SYS-05 | Publicar documentación OpenAPI de la API. | Desarrollo | S | 10 |
| SYS-06 | Registrar auditoría de cambios en asistencia y solicitudes de horario. | Sistema | M | 8 |
| SYS-07 | Mostrar siempre la mancomunidad activa y permitir cambiarla si el usuario tiene más de una pertenencia. | Ambos | M | 2 |
| SYS-08 | Ejecutar las pruebas de aislamiento con al menos dos mancomunidades en cada fase con datos de negocio. | Sistema | M | 1/10 |
| SYS-09 | Aceptar `Idempotency-Key` en las altas y acciones sensibles, y opcionalmente en el resto de escrituras, para que un reintento no duplique el efecto. | Sistema | S | 3 |

## Control horario (fases 12–13)

> [!info] Fuera de la v1
> El fichaje de jornada del profesorado no forma parte de la v1. Sus requisitos (`FIC-01` a `FIC-12`), el modelo de `fichaje` y el funcionamiento sin conexión están en [[Control horario]]. La decisión de planificarlo más adelante y de distribuirlo como PWA está en [[ADR-010 - Control horario diferido y PWA]].

## Relacionado

[[Roles y permisos]] · [[Requisitos no funcionales]] · [[Modelo de dominio]] · [[Roadmap]] · [[API REST]] · [[Multi-tenencia]] · [[Control horario]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-010 - Control horario diferido y PWA]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-013 - Generación automática de sesiones desde el horario]] · [[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]] · [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]] · [[ADR-016 - Horario editable por profesor y administrador]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-033 - Idempotencia de peticiones sensibles]] · [[ADR-034 - Bootstrap del primer superadministrador]]
