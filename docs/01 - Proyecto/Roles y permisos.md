---
tipo: documento
estado: activo
fase: 0
actualizado: 2026-11-04
tags:
  - proyecto
  - seguridad
  - roles
---

# Roles y permisos

## 1. Roles

| Rol | Código | Ámbito | Descripción |
| --- | --- | --- | --- |
| Profesor | `PROFESOR` | Mancomunidad | Imparte asignaturas. Gestiona su horario, pasa lista, registra observaciones y prepara clases. Solo ve los datos de la mancomunidad activa que le corresponden. |
| Administrador | `ADMINISTRADOR` | Mancomunidad | Gestiona la estructura de su mancomunidad y supervisa la actividad docente. Ve todo dentro de su mancomunidad, pero no sustituye al profesor en su trabajo diario. |
| Superadministrador | `SUPERADMIN` | Plataforma | Crea, suspende y reactiva mancomunidades y da soporte a las cuentas de cualquier mancomunidad (docentes y administradores). No accede a los datos académicos. |

> [!important] El rol es por mancomunidad
> `PROFESOR` y `ADMINISTRADOR` viven en `UsuarioMancomunidad`, no en `Usuario`. Una misma persona puede ser profesora en la mancomunidad A y administradora en la B. `SUPERADMIN` es un flag de plataforma (`admin_plataforma`) y es independiente de las pertenencias. El superadministrador actúa en **dos planos separados**: cuentas (sí) y datos académicos (no). Véase [[ADR-012 - Soporte de cuentas del superadministrador]].

No hay auto-registro. Los usuarios los crea un administrador de la mancomunidad o, en casos de soporte, un `SUPERADMIN`.

## 2. Matriz de permisos dentro de una mancomunidad

| Recurso / acción | Profesor | Administrador |
| --- | --- | --- |
| Iniciar / cerrar sesión | Sí | Sí |
| Cambiar su propia contraseña | Sí | Sí |
| Recuperar su contraseña por correo (enlace de un solo uso) | Sí, solo con email verificado | Sí, solo con email verificado |
| Verificar su email o cambiar de dirección | Sí, con confirmación de la nueva | Sí, con confirmación de la nueva |
| Cambiar el email de otro usuario | No | No; solo el soporte, con motivo y doble confirmación |
| Consultar y cerrar sus sesiones activas | Sí | Sí |
| Resetear la contraseña de un usuario de su mancomunidad | No | Sí, con cambio obligatorio y limpieza del bloqueo |
| Ver su perfil y la mancomunidad activa | Sí | Sí |
| Cambiar de mancomunidad si tiene varias pertenencias | Sí | Sí |
| Gestionar usuarios y pertenencias de su mancomunidad | No | CRUD |
| Gestionar centros | No | CRUD |
| Consultar el curso escolar activo | Sí | Sí |
| Gestionar cursos escolares (crear, activar, cerrar con preflight y reabrir con motivo) | No | Sí |
| Gestionar sustituciones de una asignatura | No | CRUD con motivo y auditoría |
| Consultar sustituciones | Solo las que le afectan como titular o sustituto | Todas |
| Copiar un horario a otro curso | Solo el suyo | Cualquiera de su mancomunidad |
| Gestionar días no lectivos del curso (globales o por centro) | No | CRUD |
| Consultar el calendario de días no lectivos | Sí | Sí |
| Gestionar asignaturas | No | CRUD |
| Asignar profesores a asignaturas | No | Sí |
| Ver sus asignaturas | Sí | Sí (todas) |
| Gestionar alumnos | No | CRUD |
| Ver alumnos de sus asignaturas | Sí | Sí (todos) |
| Gestionar instrumentos de un alumno | No | Sí, solo con el módulo activo |
| Consultar catálogo de instrumentos | Sí | Sí |
| Gestionar el catálogo de instrumentos | No | Sí, solo con el módulo activo |
| Cambiar el tipo de la mancomunidad | No | No; solo un `SUPERADMIN`, con motivo y auditoría |
| Matricular / dar de baja / reactivar alumnos | No | Sí |
| Crear / editar / borrar franjas horarias | Sí, las suyas mientras el horario está en `BORRADOR` | Sí, las de cualquier profesor de su mancomunidad en `BORRADOR` |
| Guardar el horario de forma parcial | Sí | Sí |
| Finalizar un horario | Sí, el suyo | Sí, el de cualquier profesor de su mancomunidad |
| Reabrir un horario finalizado (con motivo y auditoría) | No | Sí |
| Ver el horario de cualquier profesor | No | Sí |
| Ver la rejilla semanal propia (editable en `BORRADOR`) | Sí | No aplica |
| Ver la rejilla semanal de un grupo (solo lectura) | Sí, solo grupos donde imparte | Sí, todos |
| Ver la rejilla semanal de cualquier profesor (editable en `BORRADOR`, lectura en `FINALIZADO`) | No | Sí |
| Solicitar cambios de horario | Sí, tras finalizar | — |
| Aprobar / rechazar solicitudes | No | Sí |
| Consultar el histórico de solicitudes y cambios de horario | Solo sus solicitudes | Todo |
| Crear / editar / borrar sus observaciones | Sí | No |
| Consultar observaciones | Solo de sus alumnos | Todas |
| Abrir sesiones de clase | Sí (sus asignaturas) | No |
| Cancelar una sesión `PROGRAMADA` (con motivo) | Sí, las suyas | Sí, las de su mancomunidad, con auditoría |
| Pasar lista / editar asistencia | Sí (sus sesiones), en cualquier momento | No |
| Consultar asistencias | Solo de sus alumnos | Todas |
| Consultar el resumen de asistencia (incluido el histórico anonimizado) | Solo de sus alumnos | Todos |
| Consultar el historial de cambios de asistencia | No | Sí |
| Crear / editar / borrar sus preparaciones | Sí | No |
| Consultar preparaciones | Solo las suyas | Todas, incluidas borradores |
| Consultar la actividad docente | Solo la suya | Toda |
| Ver datos de otra mancomunidad | No | No |

## 3. Matriz de plataforma

| Recurso / acción | Superadmin |
| --- | --- |
| Listar mancomunidades y sus métricas básicas | Sí |
| Crear una mancomunidad con su primer administrador | Sí |
| Suspender y reactivar mancomunidades | Sí |
| Consultar la auditoría de altas, suspensiones y reactivaciones | Sí |
| Ver o editar datos académicos de una mancomunidad | No |
| Crear usuarios sin pertenencia | Sí, para soporte |
| Buscar cualquier usuario global por email o nombre | Sí |
| Ver sus pertenencias, rol y estado | Sí |
| Resetear su contraseña o forzar el cambio | Sí |
| Desbloquear una cuenta bloqueada por intentos fallidos | Sí, con motivo y auditoría |
| Activar o desactivar la cuenta global o una pertenencia | Sí |
| Cambiar el rol de una pertenencia | Sí, con motivo y auditoría |
| Iniciar un cambio de email de un usuario | Sí, con motivo; la dirección nueva debe confirmarse y el soporte no puede confirmarla |
| Ver o cerrar las sesiones de otro usuario | No; su reset, desactivación o cambio de rol ya las revoca |
| Consultar la auditoría de acciones de soporte | Sí |
| Conceder o retirar el flag de plataforma | Solo otro superadministrador, nunca desde una mancomunidad |
| Crear el primer superadministrador desde la API | No; solo por el comando de bootstrap |
| Entrar en una mancomunidad como si fuera su administrador | No en la v1 |

> [!warning] Separación de planos
> Ser `SUPERADMIN` concede el **plano de cuentas**, no el académico. No puede leer alumnos, horarios, asistencias, observaciones ni preparaciones, y no puede impersonar a un usuario en la v1. Si algún día se añade soporte con acceso temporal, será una impersonación explícita, auditada y con caducidad ([[ADR-012 - Soporte de cuentas del superadministrador]]).

## 4. Reglas de autorización

> [!important] El rol no basta
> Además de comprobar el rol, el backend debe comprobar la **mancomunidad activa** y la **propiedad del recurso**. Un profesor con un ID válido de otro profesor o de otra mancomunidad no puede acceder a sus datos.

- **AUT-01** — Toda petición a `/api/v1/**`, salvo login, refresh y selección de mancomunidad, requiere token válido.
- **AUT-02** — El rol se comprueba a nivel de endpoint (`@PreAuthorize` o configuración de seguridad).
- **AUT-03** — La mancomunidad activa se obtiene del token; el `TenantContext` se establece tras validar la pertenencia.
- **AUT-04** — La propiedad se comprueba en el servicio: el profesor solo opera sobre asignaturas que imparte, alumnos matriculados en ellas, su propio horario, sus sesiones y sus preparaciones.
- **AUT-05** — El administrador tiene lectura global **dentro de su mancomunidad** sobre horarios, observaciones, asistencias, historial de asistencia y preparaciones. Sus escrituras se limitan a gestionar la estructura, construir el horario de cualquier profesor mientras esté en `BORRADOR`, corregir fichajes y reabrir horarios; todo lo demás es solo lectura.
- **AUT-06** — No se permite el borrado físico de usuarios, centros, asignaturas ni alumnos; se desactivan (`activo = false`).
- **AUT-07** — Un administrador no puede desactivar su propia pertenencia ni dejar su mancomunidad sin al menos un administrador activo.
- **AUT-08** — Un profesor no puede ver el horario, los alumnos ni las observaciones de otro profesor.
- **AUT-09** — Las solicitudes de cambio solo pueden crearse sobre un horario propio **finalizado**.
- **AUT-10** — El administrador puede reasignar profesores, resolver solicitudes y crear, editar y finalizar el horario de cualquier profesor de su mancomunidad mientras esté en `BORRADOR`. Sobre un horario `FINALIZADO` no edita franjas: solo puede reabrir el ciclo de vida con motivo y auditoría.
- **AUT-11** — Los errores de autorización devuelven `403`; si revelar la existencia del recurso es un riesgo, se devuelve `404`. Un recurso de otra mancomunidad se trata como inexistente.
- **AUT-12** — El profesor puede editar la asistencia en cualquier momento; cada cambio genera una fila inmutable en el historial.
- **AUT-13** — El profesor no puede consultar el historial de asistencia; el administrador sí.
- **AUT-14** — El paso de `BORRADOR` a `FINALIZADO` lo realiza el profesor titular o un administrador de la mancomunidad. La vuelta a `BORRADOR` solo la realiza un administrador, únicamente si el horario está `FINALIZADO`, con motivo de 10–500 caracteres y traza en [[Historial de horario]].
- **AUT-15** — Solo la administración gestiona cursos escolares. Los cursos cerrados son de solo lectura para todos.
- **AUT-16** — Todas las operaciones sobre matrículas, horarios y preparaciones exigen un curso escolar `ACTIVO` de la mancomunidad, salvo las consultas históricas.
- **AUT-17** — Solo un `SUPERADMIN` puede crear, suspender o reactivar mancomunidades.
- **AUT-18** — Un administrador de mancomunidad no puede conceder ni retirar el flag de plataforma.
- **AUT-19** — Una mancomunidad suspendida bloquea el login y la renovación de tokens de sus pertenencias.
- **AUT-20** — La reapertura de un horario no altera franjas y cancela las solicitudes `PENDIENTE` de ese horario, que pasan a `CANCELADA` en la misma transacción.
- **AUT-21** — El `SUPERADMIN` puede administrar las **cuentas** de cualquier mancomunidad: buscar, resetear contraseña, activar o desactivar la cuenta o la pertenencia y cambiar el rol. No puede leer ni modificar datos académicos, ni impersonar a un usuario en la v1.
- **AUT-22** — Toda acción de soporte sobre una cuenta exige un motivo de 10–500 caracteres y deja traza en la auditoría de plataforma con autor, afectado, mancomunidad, acción y valores anterior y nuevo.
- **AUT-23** — Un `SUPERADMIN` no puede conceder ni retirar `admin_plataforma` desde una mancomunidad, ni dejar la plataforma sin ningún superadministrador activo.
- **AUT-24** — Un cambio de rol o una desactivación de cuenta o pertenencia revoca los refresh tokens del afectado e incrementa `token_version`; el access token en curso deja de ser válido en menos de 60 segundos.
- **AUT-25** — La recuperación de contraseña responde siempre igual exista o no la cuenta y usa un token de un solo uso, hasheado y con caducidad. Mientras `debe_cambiar_password` esté activo, el usuario solo puede cambiar la contraseña o cerrar sesión.
- **AUT-26** — Tras 5 fallos por cuenta o 20 por IP en 15 minutos, el login se bloquea 15 minutos y responde `429` sin revelar si la cuenta existe. El bloqueo no revoca sesiones abiertas; solo el `SUPERADMIN` lo levanta antes de que caduque, con motivo y auditoría, y el reset de contraseña del administrador también lo limpia.
- **AUT-27** — La rejilla semanal de un grupo es siempre de solo lectura y muestra todas las asignaturas del mismo `centroId + curso + grupo`. El administrador ve cualquier grupo; el profesor, solo aquellos donde imparte alguna asignatura, y recibe `404` en los demás.
- **AUT-28** — El código de una mancomunidad se fija al crearla y no se modifica ni se reutiliza. No interviene en la resolución del tenant ni en URLs o subdominios; el aislamiento depende solo del token y del `mancomunidad_id`.
- **AUT-29** — El tipo de una mancomunidad es informativo y no limita las asignaturas. Solo un `SUPERADMIN` puede cambiarlo, con motivo y auditoría, y el cambio no altera las asignaturas existentes.
- **AUT-30** — El módulo de instrumentos está disponible solo si existe al menos una asignatura activa con `requiere_instrumento = true`. Si está inactivo, la interfaz lo oculta y la API responde `422` a sus operaciones sin borrar datos; si vuelve a activarse, los datos reaparecen.
- **AUT-31** — El profesor solo opera sobre su propio horario; el administrador, sobre el de cualquier profesor con pertenencia activa `PROFESOR` en su mancomunidad. Un horario de otra mancomunidad responde `404` en lectura y escritura.
- **AUT-32** — `horario_profesor` y `franja_horaria` guardan `creado_por` y `actualizado_por`. La edición normal del administrador en `BORRADOR` no exige motivo: el motivo de 10–500 caracteres se reserva para la reapertura. El [[Historial de horario]] registra creación, finalización, reapertura y copia.
- **AUT-33** — El profesor titular y cualquier administrador de la mancomunidad pueden cancelar una sesión `PROGRAMADA` con un motivo de 10–500 caracteres. La cancelación del administrador queda auditada con autor, fecha y motivo; ninguno cancela sesiones `ABIERTA` o `CERRADA`.
- **AUT-34** — El profesor solo opera sobre sus propias sesiones; el administrador puede consultar y cancelar sesiones `PROGRAMADA` de cualquier profesor de su mancomunidad, pero no crear, abrir, cerrar ni editar asistencias.
- **AUT-35** — Solo la administración gestiona los días no lectivos y el calendario de un curso `CERRADO` es de solo lectura para todos. El profesor puede consultar el calendario de su mancomunidad y recibe `403` si intenta modificarlo.
- **AUT-36** — El profesor titular de un horario y cualquier administrador de su mancomunidad pueden previsualizar y copiar el horario a otro curso de la misma mancomunidad. La copia crea un `BORRADOR` a nombre del profesor titular, registra `creadoPor` y no copia sesiones, solicitudes ni historial.
- **AUT-37** — Solo la administración crea, edita y anula sustituciones. El titular y el sustituto las consultan; durante la sustitución, el sustituto abre y cierra las sesiones del periodo y pasa lista, mientras el titular las ve en solo lectura. La sustitución no cambia el horario.
- **AUT-38** — Solo la administración da de baja y reactiva matrículas. La baja no borra asistencias ni historial y excluye al alumno de las listas futuras; la reactivación lo devuelve a las listas futuras. El profesor no puede alterar el estado de una matrícula.
- **AUT-39** — Cada cuenta puede tener como máximo 5 familias de refresh activas. `GET /auth/sesiones` y `POST /auth/logout-all` son personales: ningún rol lista ni cierra sesiones de otro usuario.
- **AUT-40** — El email solo lo cambia el titular con la contraseña actual o el soporte con motivo de 10–500 caracteres. El administrador de mancomunidad no puede cambiarlo. En ambos casos la dirección nueva debe confirmarse y la anterior sigue vigente hasta entonces.
- **AUT-41** — La recuperación autoservicio exige email verificado. La falta de verificación no impide operar ni justifica un `403`; solo deshabilita el envío del enlace.
- **AUT-42** — El rate limiting y la idempotencia no conceden ni retiran permisos: un `429` o un `409` de clave repetida son rechazos técnicos. La clave de idempotencia es personal del usuario autenticado.
- **AUT-43** — La MFA queda fuera de la v1 y no existe endpoint de bootstrap. El primer `SUPERADMIN` se crea solo con el comando auditado; ningún rol puede sembrar credenciales desde la API.

## 5. Escalado de privilegios y cambios de rol

- Un profesor no puede cambiar su rol ni crear usuarios.
- Un administrador no puede convertirse en profesor ni viceversa desde la interfaz; si se necesita, se hace con un cambio explícito y auditado.
- Un administrador de mancomunidad no puede ascender a nadie a `SUPERADMIN`.
- Un `SUPERADMIN` no puede ascender a nadie desde el panel de cuentas: el flag de plataforma solo lo cambia otro superadministrador y se audita.
- El reset de contraseña del soporte genera una temporal con cambio obligatorio; nunca devuelve ni muestra la contraseña actual, y el actor no puede usarla para entrar como el afectado.
- Un cambio de rol en una pertenencia revoca los refresh de esa mancomunidad e incrementa `token_version`; el access token deja de valer en menos de 60 segundos y el usuario debe renovar.
- Las contraseñas nunca se devuelven en ninguna respuesta.
- El email es único globalmente y se normaliza a minúsculas.
- El email solo lo cambia el titular o el soporte, siempre con confirmación de la dirección nueva; el administrador de mancomunidad no puede redirigir la recuperación de un usuario.
- La MFA no está disponible en la v1; las cuentas de plataforma se protegen con contraseña de 12+, límite de sesiones, rate limiting y auditoría ([[ADR-027 - MFA y riesgo aceptado del superadministrador]]).

## 6. Dudas resueltas

- ¿El administrador puede crear observaciones? En la v1 se decide **no**: solo consulta.
- ¿El profesor puede añadir instrumentos a sus alumnos? En la v1 se decide **no**: solo consulta; los gestiona administración.
- ¿El administrador puede reabrir un horario finalizado? **Sí**, únicamente desde `FINALIZADO`, con motivo, auditoría y cancelación de solicitudes pendientes.
- ¿Puede un profesor tener asignaturas en varios centros? Sí; la asignatura pertenece a un centro y el profesor se asigna a ella.
- ¿El historial de asistencia es visible para el alumno o su tutor? No en la v1; solo administración.
- ¿Un `SUPERADMIN` puede tener también pertenencias? Sí, y entonces puede elegir entre Plataforma y sus mancomunidades al iniciar sesión.
- ¿Puede el `SUPERADMIN` tocar cuentas de profesores y administradores de cualquier mancomunidad? **Sí**, en el plano de cuentas: reset, activación, desactivación y cambio de rol, con motivo y auditoría. **No** accede a datos académicos ni impersona.
- ¿La recuperación de contraseña la puede pedir cualquiera? Sí, desde el login, con un enlace de un solo uso enviado al email. La respuesta no revela si la cuenta existe y solo se envía si el email está verificado.
- ¿El administrador puede cambiar el email de un profesor? No; solo el titular o el soporte, y siempre con confirmación de la dirección nueva ([[ADR-028 - Verificación y cambio de email]]).
- ¿Se pueden ver y cerrar las sesiones de otro usuario? No; cada usuario ve y cierra las suyas. Un reset, una desactivación o un cambio de rol revoca las del afectado ([[ADR-031 - Límite de sesiones concurrentes y cierre global]]).
- ¿Hay MFA en la v1? No, ni para el `SUPERADMIN`; el riesgo queda aceptado con revisión antes de producción ([[ADR-027 - MFA y riesgo aceptado del superadministrador]]).
- ¿El tipo de mancomunidad restringe lo que puede hacer un administrador? No; es informativo. Solo decide etiquetas y valores por defecto, y el módulo de instrumentos depende de las asignaturas ([[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]]).

## 7. Control horario (fase futura)

> [!info] Fases 12–13
> Permisos previstos para el fichaje de jornada. No forman parte de la v1; se detallan en [[Control horario]].

| Recurso / acción | Profesor | Administrador |
| --- | --- | --- |
| Fichar entrada y salida | Sí, solo la suya | No |
| Consultar su historial de jornada | Sí | — |
| Consultar fichajes de toda la mancomunidad | No | Sí |
| Corregir o anular un fichaje (con motivo y auditoría) | No | Sí |
| Exportar el resumen mensual | No | Sí |

> [!warning] Excepción al solo lectura
> El control horario es un registro laboral, no contenido académico. El administrador **sí** corrige, pero nunca borra: cada corrección es una fila nueva, motivada y auditada. Las otras excepciones son la edición del horario en `BORRADOR` y su reapertura auditada.

## Relacionado

[[Visión y alcance]] · [[Requisitos funcionales]] · [[Seguridad y autenticación]] · [[Asignatura]] · [[Instrumento]] · [[Horario del profesor]] · [[Usuario]] · [[UsuarioMancomunidad]] · [[Mancomunidad]] · [[Multi-tenencia]] · [[API REST]] · [[Control horario]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]] · [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]] · [[ADR-016 - Horario editable por profesor y administrador]] · [[ADR-023 - Copia de horario al curso siguiente]] · [[ADR-024 - Cierre y reapertura de curso escolar]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[ADR-026 - Baja de alumno a mitad de curso]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-033 - Idempotencia de peticiones sensibles]] · [[ADR-034 - Bootstrap del primer superadministrador]]
