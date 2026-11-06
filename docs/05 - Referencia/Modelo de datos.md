---
tipo: referencia
estado: activo
fase: 1
actualizado: 2026-11-04
tags:
  - base-de-datos
  - referencia
  - multi-tenant
---

# Modelo de datos

> [!warning] Borrador para la Fase 1
> Este esquema se cerrará y se convertirá en migraciones Flyway en la Fase 1. Cualquier cambio debe reflejarse también en las notas de entidad y en [[Modelo de dominio]].

## 1. Convenciones

- Motor: **MySQL 8.4 LTS**, `utf8mb4`, collation `utf8mb4_0900_ai_ci`.
- Nombres de tabla en **singular** y `snake_case`, igual que la entidad.
- Claves primarias `BIGINT AUTO_INCREMENT`.
- Claves foráneas `<tabla_singular>_id`.
- **Toda tabla de negocio lleva `mancomunidad_id`** y las claves únicas lo incluyen. Es la frontera de aislamiento del tenant.
- Las FKs son **simples** (`<padre>_id`). Que padre e hijo pertenezcan a la misma mancomunidad se valida en el servicio dentro de la transacción y se cubre con tests de aislamiento; MySQL no lo refuerza con FK compuestas.
- Fechas y horas en UTC con `DATETIME(6)`; solo fecha con `DATE`; solo hora con `TIME`.
- Booleanos con `BOOLEAN` (alias de `TINYINT(1)`).
- Enumeraciones con `VARCHAR` + `CHECK`, no `ENUM` de MySQL.
- Borrado lógico con `activo`; las tablas de histórico no se borran.
- Auditoría: `created_at` y `updated_at` en todas las tablas editables.
- Bloqueo optimista: toda tabla editable con `updated_at` incluye además `version BIGINT NOT NULL DEFAULT 0`, mapeado con `@Version` en una superclase común. Las tablas append-only (`historial_horario`, `historial_asistencia`, `evento_acceso`, `auditoria_plataforma`) y los tokens no llevan `version`.
- Flyway: `V1__esquema_inicial.sql`, `V2__datos_iniciales.sql`, etc.

## 2. Tablas

### `mancomunidad`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK, auto_increment |
| `nombre` | VARCHAR(150) | NOT NULL, UNIQUE |
| `codigo` | VARCHAR(30) | NOT NULL, UNIQUE |
| `tipo` | VARCHAR(30) | NOT NULL, CHECK (`MUSICA`, `DANZA`, `ARTES_ESCENICAS`, `IDIOMAS`, `DEPORTES`, `OTRO`); informativo, no limita las asignaturas |
| `duracion_maxima_franja_min` | INT | NULL; máximo opcional de duración de una franja |
| `activo` | BOOLEAN | NOT NULL, DEFAULT TRUE |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Índices: `uq_mancomunidad_nombre (nombre)`, `uq_mancomunidad_codigo (codigo)`, `idx_mancomunidad_activo (activo)`.
`codigo` es inmutable desde el alta y no se reutiliza; es una etiqueta legible y no decide el tenant. No se usa en URLs ni subdominios ([[Mancomunidad]]).
`tipo` describe la orientación principal y es informativo: no limita las asignaturas y solo un `SUPERADMIN` lo cambia, con motivo y auditoría ([[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]]).

### `usuario`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK, auto_increment |
| `nombre` | VARCHAR(100) | NOT NULL |
| `apellidos` | VARCHAR(150) | NOT NULL |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE |
| `password_hash` | VARCHAR(100) | NOT NULL |
| `debe_cambiar_password` | BOOLEAN | NOT NULL, DEFAULT FALSE |
| `password_temporal_expira_at` | DATETIME(6) | NULL |
| `email_verificado` | BOOLEAN | NOT NULL, DEFAULT FALSE |
| `email_verificado_at` | DATETIME(6) | NULL |
| `token_version` | BIGINT | NOT NULL, DEFAULT 0; se incrementa para invalidar access tokens |
| `admin_plataforma` | BOOLEAN | NOT NULL, DEFAULT FALSE |
| `activo` | BOOLEAN | NOT NULL, DEFAULT TRUE |
| `ultimo_acceso` | DATETIME(6) | NULL |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Índices: `uq_usuario_email (email)`, `idx_usuario_activo (activo)`.
El usuario es **global**: no tiene `rol` ni `mancomunidad_id`. Su rol depende de la pertenencia.
`debe_cambiar_password` se activa al crear una cuenta con contraseña temporal o al resetearla; `password_temporal_expira_at` la invalida a las 24 horas.
`email_verificado` habilita la recuperación autoservicio y se activa al confirmar el enlace ([[ADR-028 - Verificación y cambio de email]]). `token_version` viaja en el access token y permite revocar todos los tokens de la cuenta ([[ADR-029 - Claves JWT, rotación y secretos]]).

### `usuario_mancomunidad`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `usuario_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `rol` | VARCHAR(20) | NOT NULL, CHECK (`PROFESOR`, `ADMINISTRADOR`) |
| `token_version` | BIGINT | NOT NULL, DEFAULT 0; se incrementa al cambiar rol o desactivar la pertenencia |
| `activo` | BOOLEAN | NOT NULL, DEFAULT TRUE |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_usuario_mancomunidad (usuario_id, mancomunidad_id)`.
Índices: `idx_um_mancomunidad_rol (mancomunidad_id, rol, activo)`, `idx_um_usuario (usuario_id, activo)`.

### `refresh_token`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `usuario_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `mancomunidad_id` | BIGINT | NULL, FK → `mancomunidad.id` |
| `scope` | VARCHAR(20) | NOT NULL, CHECK (`TENANT`, `PLATFORM`) |
| `familia_id` | CHAR(36) | NOT NULL; UUID de la sesión, compartido por todas sus rotaciones |
| `token_hash` | CHAR(64) | NOT NULL, UNIQUE |
| `expires_at` | DATETIME(6) | NOT NULL |
| `revocado` | BOOLEAN | NOT NULL, DEFAULT FALSE |
| `revocado_at` | DATETIME(6) | NULL |
| `motivo_revocacion` | VARCHAR(30) | NULL, CHECK (`LOGOUT`, `ROTACION`, `REUTILIZACION`, `CAMBIO_PASSWORD`, `CAMBIO_EMAIL`, `DESACTIVACION`, `CAMBIO_ROL`, `LIMITE_SESIONES`) |
| `reemplazado_por` | BIGINT | NULL, FK → `refresh_token.id` |
| `ultimo_uso_at` | DATETIME(6) | NULL; se actualiza en cada rotación |
| `ip` | VARCHAR(45) | NULL; IPv4 o IPv6 de la sesión |
| `user_agent` | VARCHAR(255) | NULL; para la lista de sesiones del usuario |
| `created_at` | DATETIME(6) | NOT NULL |

Índices: `idx_refresh_usuario (usuario_id, revocado, expires_at)`, `idx_refresh_mancomunidad (mancomunidad_id, revocado)`, `idx_refresh_familia (familia_id, revocado)`, `idx_refresh_ultimo_uso (usuario_id, ultimo_uso_at)`.
`mancomunidad_id` es obligatorio para `scope = TENANT` y nulo para `scope = PLATFORM`; se valida en el servicio.
Cada familia se revoca entera al cerrar sesión, al cambiar la contraseña o el email, al desactivar la cuenta o la pertenencia y al detectar reutilización ([[ADR-030 - Detección de reutilización de refresh tokens]]). El límite de 5 familias activas y el cierre global están en [[ADR-031 - Límite de sesiones concurrentes y cierre global]]. Los tokens revocados o caducados se purgan a los 30 días.

### `token_recuperacion_password`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `usuario_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `token_hash` | CHAR(64) | NOT NULL, UNIQUE |
| `expires_at` | DATETIME(6) | NOT NULL; 60 minutos desde la emisión |
| `usado` | BOOLEAN | NOT NULL, DEFAULT FALSE |
| `created_at` | DATETIME(6) | NOT NULL |

Índices: `uq_token_recuperacion_hash (token_hash)`, `idx_token_recuperacion_usuario (usuario_id, usado)`.
Solo se guarda el SHA-256; el token en claro viaja únicamente en el enlace del correo. Se invalida al usarse, al pedir otro o al cambiar la contraseña. Los usados y caducados se purgan.

### `evento_acceso`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `email` | VARCHAR(150) | NOT NULL; normalizado a minúsculas |
| `usuario_id` | BIGINT | NULL, FK → `usuario.id` |
| `ip` | VARCHAR(45) | NOT NULL; IPv4 o IPv6 |
| `tipo` | VARCHAR(20) | NOT NULL, CHECK (`FALLIDO`, `EXITOSO`, `DESBLOQUEO`) |
| `ocurrido_at` | DATETIME(6) | NOT NULL |

Índices: `idx_evento_email_fecha (email, ocurrido_at)`, `idx_evento_ip_fecha (ip, ocurrido_at)`, `idx_evento_usuario_fecha (usuario_id, ocurrido_at)`.
Tabla **append-only** y global: registra intentos de login, accesos correctos y desbloqueos para calcular los límites de 5 fallos por cuenta y 20 por IP en 15 minutos ([[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]]). No guarda contraseñas ni tokens. Se purga a los 30 días.

### `auditoria_plataforma`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `actor_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `usuario_afectado_id` | BIGINT | NULL, FK → `usuario.id` |
| `mancomunidad_id` | BIGINT | NULL, FK → `mancomunidad.id` |
| `accion` | VARCHAR(40) | NOT NULL, CHECK (`ALTA_MANCOMUNIDAD`, `EDICION_MANCOMUNIDAD`, `SUSPENSION_MANCOMUNIDAD`, `REACTIVACION_MANCOMUNIDAD`, `ALTA_SUPERADMIN`, `RESET_PASSWORD`, `CAMBIO_ROL`, `ACTIVACION_CUENTA`, `DESACTIVACION_CUENTA`, `ACTIVACION_PERTENENCIA`, `DESACTIVACION_PERTENENCIA`, `CORRECCION_EMAIL`, `DESBLOQUEO_CUENTA`) |
| `motivo` | VARCHAR(500) | NULL; obligatorio en acciones de soporte sobre cuentas |
| `datos_anteriores` | JSON | NULL |
| `datos_nuevos` | JSON | NULL |
| `created_at` | DATETIME(6) | NOT NULL |

Índices: `idx_auditoria_actor (actor_id, created_at)`, `idx_auditoria_afectado (usuario_afectado_id, created_at)`, `idx_auditoria_mancomunidad (mancomunidad_id, created_at)`.
Tabla **append-only** y global: registra altas, ediciones, suspensiones y reactivaciones de mancomunidades y las acciones de soporte sobre cuentas. No guarda contraseñas ni tokens. `ALTA_SUPERADMIN` se escribe una sola vez en el bootstrap y usa el nuevo usuario como actor ([[ADR-034 - Bootstrap del primer superadministrador]]). Se conserva **2 años** y se purga después.

### `verificacion_email`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `usuario_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `email_nuevo` | VARCHAR(150) | NOT NULL; dirección pendiente de confirmar |
| `token_hash` | CHAR(64) | NOT NULL, UNIQUE |
| `expira_at` | DATETIME(6) | NOT NULL; 24 horas desde la emisión |
| `usado` | BOOLEAN | NOT NULL, DEFAULT FALSE |
| `created_at` | DATETIME(6) | NOT NULL |

Índices: `uq_verificacion_email_hash (token_hash)`, `idx_verificacion_email_usuario (usuario_id, usado, expira_at)`.
Solo puede haber una verificación pendiente por usuario; pedir otra invalida la anterior. El token en claro solo viaja en el correo y se guarda su SHA-256. Al confirmar se actualiza `usuario.email`, se marca `email_verificado` y se revocan las sesiones ([[ADR-028 - Verificación y cambio de email]]). Los tokens usados o caducados se purgan a los 7 días.

### `evento_seguridad`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `usuario_id` | BIGINT | NULL, FK → `usuario.id` |
| `mancomunidad_id` | BIGINT | NULL, FK → `mancomunidad.id` |
| `tipo` | VARCHAR(40) | NOT NULL, CHECK (`REUTILIZACION_REFRESH`, `REVOCACION_MASIVA`, `ROTACION_CLAVE`, `CAMBIO_EMAIL`, `VERIFICACION_EMAIL`, `LIMITE_SESIONES`, `BOOTSTRAP_SUPERADMIN`) |
| `ip` | VARCHAR(45) | NULL |
| `detalle` | JSON | NULL; sin tokens ni contraseñas |
| `created_at` | DATETIME(6) | NOT NULL |

Índices: `idx_evento_seguridad_usuario (usuario_id, created_at)`, `idx_evento_seguridad_tipo (tipo, created_at)`.
Tabla **append-only** y global para eventos de seguridad que no encajan en `evento_acceso` ni en la auditoría de plataforma. Se conserva **1 año** y se purga por fecha.

### `limite_peticion`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `clave` | VARCHAR(200) | NOT NULL; ámbito y sujeto, con el email hasheado |
| `ventana_inicio` | DATETIME(6) | NOT NULL; inicio de la ventana fija |
| `contador` | INT | NOT NULL, DEFAULT 0 |
| `created_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_limite_peticion (clave, ventana_inicio)`.
Índice: `idx_limite_ventana (ventana_inicio)`.
El incremento es atómico con `INSERT ... ON DUPLICATE KEY UPDATE`. Se limpia a las 24 horas. No guarda emails en claro; usa SHA-256 para el sujeto ([[ADR-032 - Rate limiting de extremos sensibles]]).

### `idempotencia`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `usuario_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `clave` | VARCHAR(64) | NOT NULL; valor de `Idempotency-Key` |
| `metodo` | VARCHAR(10) | NOT NULL |
| `ruta` | VARCHAR(255) | NOT NULL |
| `hash_peticion` | CHAR(64) | NOT NULL; SHA-256 del cuerpo canónico |
| `estado` | VARCHAR(20) | NOT NULL, CHECK (`EN_CURSO`, `COMPLETADA`) |
| `respuesta_status` | SMALLINT | NULL |
| `respuesta_body` | JSON | NULL; se cifra en reposo y se purga |
| `created_at` | DATETIME(6) | NOT NULL |
| `expira_at` | DATETIME(6) | NOT NULL; 24 horas desde la creación |

Restricción: `uq_idempotencia_usuario_clave (usuario_id, clave)`.
Índice: `idx_idempotencia_expira (expira_at)`.
Se escribe en la misma transacción que la operación de negocio; un reintento con la misma clave y cuerpo devuelve la respuesta guardada ([[ADR-033 - Idempotencia de peticiones sensibles]]). Se purga a las 24 horas.

### `centro`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `nombre` | VARCHAR(150) | NOT NULL |
| `direccion` | VARCHAR(255) | NULL |
| `telefono` | VARCHAR(30) | NULL |
| `email` | VARCHAR(150) | NULL |
| `activo` | BOOLEAN | NOT NULL, DEFAULT TRUE |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_centro (mancomunidad_id, nombre)`.
Índice: `idx_centro_mancomunidad (mancomunidad_id, activo)`.

### `asignatura`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `centro_id` | BIGINT | NOT NULL, FK → `centro.id` |
| `nombre` | VARCHAR(150) | NOT NULL |
| `curso` | VARCHAR(50) | NOT NULL |
| `grupo` | VARCHAR(20) | NOT NULL |
| `tipo` | VARCHAR(30) | NOT NULL, CHECK (`MUSICA`, `DANZA`, `ARTES_ESCENICAS`, `IDIOMAS`, `DEPORTES`, `OTRO`) |
| `requiere_instrumento` | BOOLEAN | NOT NULL, DEFAULT FALSE, CHECK (`requiere_instrumento = FALSE OR tipo = 'MUSICA'`) |
| `activo` | BOOLEAN | NOT NULL, DEFAULT TRUE |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_asignatura (mancomunidad_id, centro_id, nombre, curso, grupo)`.
Índices: `idx_asignatura_centro (mancomunidad_id, centro_id, activo)`, `idx_asignatura_nombre (mancomunidad_id, nombre)`, `idx_asignatura_instrumento (mancomunidad_id, requiere_instrumento, activo)`.
`requiere_instrumento` solo puede ser `TRUE` cuando `tipo = 'MUSICA'`; el `CHECK` lo garantiza y el servicio lo valida antes con un mensaje claro. El módulo de instrumentos se calcula con un `EXISTS` sobre asignaturas activas con `requiere_instrumento = TRUE` ([[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]]).

### `asignatura_profesor`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `asignatura_id` | BIGINT | NOT NULL, FK → `asignatura.id` |
| `profesor_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `created_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_asignatura_profesor (mancomunidad_id, asignatura_id, profesor_id)`.
La pertenencia activa `PROFESOR` en esa mancomunidad se valida en la aplicación.

### `alumno`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `centro_id` | BIGINT | NOT NULL, FK → `centro.id` |
| `nombre` | VARCHAR(100) | NOT NULL |
| `apellidos` | VARCHAR(150) | NOT NULL |
| `fecha_nacimiento` | DATE | NULL |
| `tutor_nombre` | VARCHAR(150) | NULL |
| `tutor_email` | VARCHAR(150) | NULL |
| `tutor_telefono` | VARCHAR(30) | NULL |
| `activo` | BOOLEAN | NOT NULL, DEFAULT TRUE |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Índices: `idx_alumno_centro (mancomunidad_id, centro_id, activo)`, `idx_alumno_apellidos (mancomunidad_id, apellidos, nombre)`.
El alumno es **local al tenant**: no tiene identidad global, no se deduplica entre mancomunidades y la plataforma no busca menores. La misma persona en dos tenants son dos filas independientes ([[ADR-009 - Multi-tenant con mancomunidad e identidad global]]). No se desactiva un alumno con matrículas `ACTIVA` ([[ADR-026 - Baja de alumno a mitad de curso]]).

### `instrumento`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `nombre` | VARCHAR(80) | NOT NULL |
| `activo` | BOOLEAN | NOT NULL, DEFAULT TRUE |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_instrumento (mancomunidad_id, nombre)`.
Índice: `idx_instrumento_activo (mancomunidad_id, activo)`.

### `alumno_instrumento`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `alumno_id` | BIGINT | NOT NULL, FK → `alumno.id` |
| `instrumento_id` | BIGINT | NOT NULL, FK → `instrumento.id` |
| `created_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_alumno_instrumento (mancomunidad_id, alumno_id, instrumento_id)`.
Índice: `idx_ai_instrumento (mancomunidad_id, instrumento_id)`.

### `curso_escolar`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `codigo` | VARCHAR(9) | NOT NULL |
| `fecha_inicio` | DATE | NOT NULL |
| `fecha_fin` | DATE | NOT NULL, CHECK (`fecha_fin` > `fecha_inicio`) |
| `estado` | VARCHAR(20) | NOT NULL, CHECK (`PLANIFICACION`, `ACTIVO`, `CERRADO`) |
| `activo_unico` | BIGINT | `GENERATED ALWAYS AS (IF(estado = 'ACTIVO', mancomunidad_id, NULL)) STORED`, UNIQUE |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_curso_escolar (mancomunidad_id, codigo)`.
El índice único sobre `activo_unico` garantiza **como máximo un curso `ACTIVO` por mancomunidad**; MySQL permite múltiples `NULL`. La regla se valida también en el servicio con un mensaje claro.
Cerrar un curso no borra filas: cancela las sesiones `PROGRAMADA` sin asistencia, congela horarios y matrículas y permite corregir asistencias existentes. Reabrir exige motivo y no reactiva sesiones canceladas ([[ADR-024 - Cierre y reapertura de curso escolar]]).

### `dia_no_lectivo`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `curso_escolar_id` | BIGINT | NOT NULL, FK → `curso_escolar.id` |
| `fecha` | DATE | NOT NULL |
| `centro_id` | BIGINT | NULL, FK → `centro.id`; `NULL` = toda la mancomunidad |
| `nombre` | VARCHAR(120) | NOT NULL |
| `creado_por` | BIGINT | NOT NULL, FK → `usuario.id` |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_dia_no_lectivo (mancomunidad_id, curso_escolar_id, fecha, centro_id)`. MySQL permite varios `NULL`, así que la unicidad del día global se refuerza en el servicio.
Índice: `idx_dia_no_lectivo_fecha (mancomunidad_id, fecha, centro_id)`.
El generador consulta esta tabla antes de crear sesiones y, al registrar un día, cancela las sesiones `PROGRAMADA` sin asistencia de su ámbito ([[ADR-017 - Cancelación de sesiones y días no lectivos]]).

### `matricula`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `alumno_id` | BIGINT | NOT NULL, FK → `alumno.id` |
| `asignatura_id` | BIGINT | NOT NULL, FK → `asignatura.id` |
| `curso_escolar_id` | BIGINT | NOT NULL, FK → `curso_escolar.id` |
| `estado` | VARCHAR(20) | NOT NULL, CHECK (`ACTIVA`, `BAJA`) |
| `baja_at` | DATETIME(6) | NULL; se registra al pasar a `BAJA` y se limpia al reactivar |
| `purga_at` | DATETIME(6) | NULL; `baja_at + 2 años`; se limpia al reactivar |
| `purgada_at` | DATETIME(6) | NULL; momento de la purga; evita reprocesar |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_matricula (mancomunidad_id, alumno_id, asignatura_id, curso_escolar_id)`.
Índices: `idx_matricula_asignatura (mancomunidad_id, asignatura_id, curso_escolar_id, estado)`, `idx_matricula_alumno (mancomunidad_id, alumno_id, curso_escolar_id)`, `idx_matricula_purga (mancomunidad_id, estado, purga_at, purgada_at)`.
La baja conserva la fila y todo el histórico; excluye al alumno de las listas abiertas a partir de `baja_at` y la reactivación lo reincorpora a las futuras ([[ADR-026 - Baja de alumno a mitad de curso]]).

### `horario_profesor`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `profesor_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `curso_escolar_id` | BIGINT | NOT NULL, FK → `curso_escolar.id` |
| `estado` | VARCHAR(20) | NOT NULL, CHECK (`BORRADOR`, `FINALIZADO`) |
| `finalizado_at` | DATETIME(6) | NULL |
| `creado_por` | BIGINT | NOT NULL, FK → `usuario.id`; profesor titular o administrador |
| `actualizado_por` | BIGINT | NOT NULL, FK → `usuario.id`; última modificación |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_horario_profesor (mancomunidad_id, profesor_id, curso_escolar_id)`.
Índice: `idx_horario_profesor_estado (mancomunidad_id, profesor_id, estado)`.
`creado_por` y `actualizado_por` distinguen si el horario lo construyó el profesor o la administración; no sustituyen al historial de estados ([[ADR-016 - Horario editable por profesor y administrador]]).

### `historial_horario`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `horario_profesor_id` | BIGINT | NOT NULL, FK → `horario_profesor.id` |
| `origen_horario_id` | BIGINT | NULL, FK → `horario_profesor.id`; obligatorio en `COPIA` |
| `accion` | VARCHAR(20) | NOT NULL, CHECK (`CREACION`, `FINALIZACION`, `REAPERTURA`, `COPIA`) |
| `estado_anterior` | VARCHAR(20) | NULL, CHECK (`BORRADOR`, `FINALIZADO`) |
| `estado_nuevo` | VARCHAR(20) | NOT NULL, CHECK (`BORRADOR`, `FINALIZADO`) |
| `motivo` | VARCHAR(500) | NULL; obligatorio en `REAPERTURA` |
| `realizado_por` | BIGINT | NOT NULL, FK → `usuario.id` |
| `realizado_at` | DATETIME(6) | NOT NULL |

Índices: `idx_historial_horario (mancomunidad_id, horario_profesor_id, realizado_at)`, `idx_historial_horario_admin (mancomunidad_id, realizado_por, realizado_at)`.
Tabla **inmutable**: solo inserción y consulta. La reapertura se registra en la misma transacción que el cambio de estado y exige `motivo` de 10–500 caracteres. `FINALIZACION` puede tener como autor al profesor titular o a un administrador.
`COPIA` se inserta en el horario nuevo al copiarlo de otro curso, con `origen_horario_id` y `estado_nuevo = 'BORRADOR'` ([[ADR-023 - Copia de horario al curso siguiente]]).

### `franja_horaria`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `horario_profesor_id` | BIGINT | NOT NULL, FK → `horario_profesor.id` |
| `asignatura_id` | BIGINT | NOT NULL, FK → `asignatura.id` |
| `dia_semana` | TINYINT | NOT NULL, CHECK (1–7) |
| `hora_inicio` | TIME | NOT NULL |
| `hora_fin` | TIME | NOT NULL, CHECK (`hora_fin` > `hora_inicio`) |
| `aula` | VARCHAR(100) | NULL |
| `creado_por` | BIGINT | NOT NULL, FK → `usuario.id`; profesor titular o administrador |
| `actualizado_por` | BIGINT | NOT NULL, FK → `usuario.id`; última modificación |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Índices: `idx_franja_horario (mancomunidad_id, horario_profesor_id, dia_semana)`, `idx_franja_asignatura (mancomunidad_id, asignatura_id)`.
Los solapamientos se validan en el servicio; el profesor y el curso se obtienen de `horario_profesor`. `creado_por` y `actualizado_por` registran si la franja la tocó el profesor o la administración ([[ADR-016 - Horario editable por profesor y administrador]]).

### `sustitucion`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `curso_escolar_id` | BIGINT | NOT NULL, FK → `curso_escolar.id` |
| `asignatura_id` | BIGINT | NOT NULL, FK → `asignatura.id` |
| `profesor_titular_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `profesor_sustituto_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `desde` | DATE | NOT NULL |
| `hasta` | DATE | NOT NULL, CHECK (`hasta` >= `desde`) |
| `motivo` | VARCHAR(500) | NOT NULL; 10–500 caracteres |
| `estado` | VARCHAR(20) | NOT NULL, CHECK (`ACTIVA`, `ANULADA`) |
| `anulada_at` | DATETIME(6) | NULL |
| `anulada_por` | BIGINT | NULL, FK → `usuario.id`; obligatorio si `ANULADA` |
| `motivo_anulacion` | VARCHAR(500) | NULL; 10–500 caracteres si `ANULADA` |
| `creado_por` | BIGINT | NOT NULL, FK → `usuario.id` |
| `actualizado_por` | BIGINT | NOT NULL, FK → `usuario.id` |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Índices: `idx_sustitucion_asignatura (mancomunidad_id, asignatura_id, curso_escolar_id, estado)`, `idx_sustitucion_sustituto (mancomunidad_id, profesor_sustituto_id, desde, hasta)`, `idx_sustitucion_titular (mancomunidad_id, profesor_titular_id, desde, hasta)`.
Los solapes de la misma asignatura y la disponibilidad del sustituto se validan en el servicio; no se refuerzan con una restricción única porque dependen de rangos de fechas. La sustitución no modifica `horario_profesor` ni `franja_horaria` ([[ADR-025 - Sustituciones y cambio de titular]]).

### `sesion_clase`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `curso_escolar_id` | BIGINT | NOT NULL, FK → `curso_escolar.id` |
| `franja_id` | BIGINT | NULL, FK → `franja_horaria.id` `ON DELETE SET NULL` |
| `asignatura_id` | BIGINT | NOT NULL, FK → `asignatura.id` |
| `profesor_id` | BIGINT | NOT NULL, FK → `usuario.id`; profesor real (titular o sustituto) |
| `sustitucion_id` | BIGINT | NULL, FK → `sustitucion.id`; informado si la imparte un sustituto |
| `fecha` | DATE | NOT NULL |
| `hora_inicio` | TIME | NOT NULL |
| `hora_fin` | TIME | NOT NULL, CHECK (`hora_fin` > `hora_inicio`) |
| `estado` | VARCHAR(20) | NOT NULL, CHECK (`PROGRAMADA`, `ABIERTA`, `CERRADA`, `CANCELADA`) |
| `observaciones` | TEXT | NULL |
| `cancelada_at` | DATETIME(6) | NULL |
| `cancelada_por` | BIGINT | NULL, FK → `usuario.id`; obligatorio si `CANCELADA` |
| `motivo_cancelacion` | VARCHAR(500) | NULL; obligatorio si `CANCELADA` (10–500) |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricciones: `uq_sesion_franja_fecha (mancomunidad_id, franja_id, fecha)` (MySQL permite varios `NULL`; se refuerza en el servicio), `uq_sesion_puntual (mancomunidad_id, profesor_id, fecha, hora_inicio)`.
Índices: `idx_sesion_asignatura_fecha (mancomunidad_id, asignatura_id, fecha)`, `idx_sesion_estado_fecha (mancomunidad_id, estado, fecha)`, `idx_sesion_curso (mancomunidad_id, curso_escolar_id)`.
La tarea diaria crea una sesión `PROGRAMADA` por franja y fecha, sin filas de asistencia, y omite los [[Día no lectivo]]. Al abrirla pasa a `ABIERTA` y se cargan los alumnos como presentes ([[ADR-013 - Generación automática de sesiones desde el horario]]).
`CANCELADA` solo se alcanza desde `PROGRAMADA`, exige motivo y autor y no tiene asistencia ([[ADR-017 - Cancelación de sesiones y días no lectivos]]). `curso_escolar_id` es obligatorio: las generadas lo heredan de la franja y las puntuales usan el curso activo o el indicado ([[ADR-019 - Resumen de asistencia y purga anonimizada]]).
Al borrar una franja, las sesiones `PROGRAMADA` sin asistencia se eliminan y las que tienen lista se conservan con `franja_id = NULL` y su instantánea propia ([[ADR-020 - Borrado de franjas y conservación de sesiones]]).
`profesor_id` es el profesor que imparte realmente la sesión. Si la fecha cae dentro de una [[Sustitución]] `ACTIVA`, vale el sustituto y `sustitucion_id` queda informado; las sesiones ya abiertas o cerradas no se reasignan ([[ADR-025 - Sustituciones y cambio de titular]]).
Al abrir la sesión solo se cargan alumnos con matrícula `ACTIVA` en esa fecha; las filas de sesiones ya abiertas se conservan aunque el alumno cause baja después ([[ADR-026 - Baja de alumno a mitad de curso]]).

### `asistencia`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `sesion_id` | BIGINT | NOT NULL, FK → `sesion_clase.id` |
| `alumno_id` | BIGINT | NOT NULL, FK → `alumno.id` |
| `estado` | VARCHAR(25) | NOT NULL, CHECK (`PRESENTE`, `AUSENTE`, `AUSENCIA_JUSTIFICADA`, `RETRASO`) |
| `minutos_retraso` | SMALLINT | NULL, CHECK (1–180) |
| `motivo` | VARCHAR(255) | NULL |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_asistencia (mancomunidad_id, sesion_id, alumno_id)`.
Índices: `idx_asistencia_alumno (mancomunidad_id, alumno_id)`, `idx_asistencia_estado (mancomunidad_id, estado)`.

### `historial_asistencia`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `asistencia_id` | BIGINT | NOT NULL, FK → `asistencia.id` |
| `estado_anterior` | VARCHAR(25) | NULL |
| `estado_nuevo` | VARCHAR(25) | NOT NULL |
| `minutos_retraso_anterior` | SMALLINT | NULL |
| `minutos_retraso_nuevo` | SMALLINT | NULL |
| `motivo_anterior` | VARCHAR(255) | NULL |
| `motivo_nuevo` | VARCHAR(255) | NULL |
| `cambiado_por` | BIGINT | NOT NULL, FK → `usuario.id` |
| `cambiado_at` | DATETIME(6) | NOT NULL |
| `origen` | VARCHAR(20) | NOT NULL, CHECK (`CREACION`, `EDICION`, `CORRECCION`) |

Índices: `idx_historial_asistencia (mancomunidad_id, asistencia_id, cambiado_at)`, `idx_historial_usuario (mancomunidad_id, cambiado_por, cambiado_at)`.
Tabla **inmutable**: solo inserción y consulta; nunca se actualiza ni se borra.
Se purga cuando la matrícula lleva 2 años de baja, junto con las asistencias asociadas ([[ADR-019 - Resumen de asistencia y purga anonimizada]]).

### `resumen_asistencia`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `alumno_hash` | CHAR(64) | NOT NULL; SHA-256 de `mancomunidad_id:alumno_id:secreto` |
| `asignatura_id` | BIGINT | NOT NULL, FK → `asignatura.id` |
| `curso_escolar_id` | BIGINT | NOT NULL, FK → `curso_escolar.id` |
| `sesiones` | INT | NOT NULL DEFAULT 0 |
| `presentes` | INT | NOT NULL DEFAULT 0 |
| `ausentes` | INT | NOT NULL DEFAULT 0 |
| `ausencias_justificadas` | INT | NOT NULL DEFAULT 0 |
| `retrasos` | INT | NOT NULL DEFAULT 0 |
| `minutos_retraso` | INT | NOT NULL DEFAULT 0 |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_resumen_asistencia (mancomunidad_id, alumno_hash, asignatura_id, curso_escolar_id)`.
Índices: `idx_resumen_asistencia_alumno (mancomunidad_id, alumno_hash, curso_escolar_id)`, `idx_resumen_asistencia_asignatura (mancomunidad_id, asignatura_id, curso_escolar_id)`.
Tabla de agregados **anonimizados**: no tiene FK a `alumno`; solo se escribe en la purga por retención y se consulta como histórico ([[ADR-019 - Resumen de asistencia y purga anonimizada]]).

### `observacion`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `alumno_id` | BIGINT | NOT NULL, FK → `alumno.id` |
| `profesor_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `asignatura_id` | BIGINT | NULL, FK → `asignatura.id` |
| `curso_escolar_id` | BIGINT | NOT NULL, FK → `curso_escolar.id` |
| `tipo` | VARCHAR(20) | NOT NULL, CHECK (`CONDUCTA`, `ACADEMICA`, `ASISTENCIA`, `OTRA`) |
| `texto` | TEXT | NOT NULL |
| `fecha` | DATE | NOT NULL |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Índices: `idx_observacion_alumno (mancomunidad_id, alumno_id, fecha)`, `idx_observacion_profesor (mancomunidad_id, profesor_id)`, `idx_observacion_curso (mancomunidad_id, curso_escolar_id, fecha)`.
`curso_escolar_id` es obligatorio y la `fecha` debe caer dentro de su rango ([[ADR-018 - Curso escolar en las observaciones]]).

### `preparacion_clase`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `profesor_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `asignatura_id` | BIGINT | NOT NULL, FK → `asignatura.id` |
| `curso_escolar_id` | BIGINT | NOT NULL, FK → `curso_escolar.id` |
| `sesion_id` | BIGINT | NULL, FK → `sesion_clase.id` |
| `fecha` | DATE | NOT NULL |
| `titulo` | VARCHAR(150) | NOT NULL |
| `objetivos` | TEXT | NULL |
| `contenido` | TEXT | NOT NULL |
| `recursos` | TEXT | NULL |
| `observaciones` | TEXT | NULL |
| `estado` | VARCHAR(20) | NOT NULL, CHECK (`BORRADOR`, `PREPARADA`, `IMPARTIDA`) |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Restricción: `uq_preparacion_sesion (mancomunidad_id, sesion_id)` (MySQL permite varios `NULL`; se refuerza en el servicio).
Índices: `idx_preparacion_profesor_fecha (mancomunidad_id, profesor_id, fecha)`, `idx_preparacion_asignatura (mancomunidad_id, asignatura_id, estado)`.

### `solicitud_cambio_horario`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `profesor_id` | BIGINT | NOT NULL, FK → `usuario.id` |
| `horario_profesor_id` | BIGINT | NOT NULL, FK → `horario_profesor.id` |
| `franja_id` | BIGINT | NULL, FK → `franja_horaria.id` |
| `tipo` | VARCHAR(20) | NOT NULL, CHECK (`CREAR`, `MODIFICAR`, `ELIMINAR`) |
| `asignatura_id` | BIGINT | NOT NULL, FK → `asignatura.id` |
| `dia_semana_propuesto` | TINYINT | NULL, CHECK (1–7) |
| `hora_inicio_propuesta` | TIME | NULL |
| `hora_fin_propuesta` | TIME | NULL |
| `aula_propuesta` | VARCHAR(100) | NULL |
| `motivo` | TEXT | NOT NULL |
| `estado` | VARCHAR(20) | NOT NULL, CHECK (`PENDIENTE`, `APROBADA`, `RECHAZADA`, `CANCELADA`) |
| `respuesta_admin` | TEXT | NULL |
| `resuelta_por` | BIGINT | NULL, FK → `usuario.id` |
| `resuelta_at` | DATETIME(6) | NULL |
| `created_at` | DATETIME(6) | NOT NULL |
| `updated_at` | DATETIME(6) | NOT NULL |

Índices: `idx_solicitud_estado (mancomunidad_id, estado, created_at)`, `idx_solicitud_profesor (mancomunidad_id, profesor_id)`.
La unicidad de una solicitud pendiente por franja se valida en el servicio. Al reabrir un horario, sus solicitudes `PENDIENTE` pasan a `CANCELADA` en la misma transacción.

## 3. Relaciones

| Relación | Tipo | Implementación |
| --- | --- | --- |
| Mancomunidad → Centro | 1:N | `centro.mancomunidad_id` |
| Mancomunidad → Curso escolar | 1:N | `curso_escolar.mancomunidad_id` |
| Mancomunidad → Instrumento | 1:N | `instrumento.mancomunidad_id` |
| Mancomunidad → UsuarioMancomunidad | 1:N | `usuario_mancomunidad.mancomunidad_id` |
| Usuario → UsuarioMancomunidad | 1:N | `usuario_mancomunidad.usuario_id` |
| Usuario → Token de recuperación | 1:N | `token_recuperacion_password.usuario_id` |
| Usuario → Verificación de email | 1:N | `verificacion_email.usuario_id` |
| Usuario → Refresh token | 1:N | `refresh_token.usuario_id` |
| Refresh token → Refresh token (rotación) | 1:N | `refresh_token.reemplazado_por` |
| Usuario → Evento de seguridad | 1:N | `evento_seguridad.usuario_id` |
| Mancomunidad → Evento de seguridad | 1:N | `evento_seguridad.mancomunidad_id` |
| Usuario → Idempotencia | 1:N | `idempotencia.usuario_id` |
| Usuario → Evento de acceso | 1:N | `evento_acceso.usuario_id` |
| Usuario → Auditoría de plataforma (actor) | 1:N | `auditoria_plataforma.actor_id` |
| Usuario → Auditoría de plataforma (afectado) | 1:N | `auditoria_plataforma.usuario_afectado_id` |
| Mancomunidad → Auditoría de plataforma | 1:N | `auditoria_plataforma.mancomunidad_id` |
| Centro → Asignatura | 1:N | `asignatura.centro_id` |
| Centro → Alumno | 1:N | `alumno.centro_id` |
| Asignatura ↔ Profesor | N:M | `asignatura_profesor` |
| Alumno ↔ Asignatura | N:M | `matricula` (con curso y estado) |
| Alumno ↔ Instrumento | N:M | `alumno_instrumento` |
| Curso → Matrícula | 1:N | `matricula.curso_escolar_id` |
| Curso → Horario del profesor | 1:N | `horario_profesor.curso_escolar_id` |
| Curso → Preparación | 1:N | `preparacion_clase.curso_escolar_id` |
| Curso → Día no lectivo | 1:N | `dia_no_lectivo.curso_escolar_id` |
| Centro → Día no lectivo | 1:N | `dia_no_lectivo.centro_id` (opcional; nulo = toda la mancomunidad) |
| Usuario → Día no lectivo | 1:N | `dia_no_lectivo.creado_por` |
| Curso → Observación | 1:N | `observacion.curso_escolar_id` |
| Curso → Resumen de asistencia | 1:N | `resumen_asistencia.curso_escolar_id` |
| Asignatura → Resumen de asistencia | 1:N | `resumen_asistencia.asignatura_id` |
| Alumno → Resumen de asistencia | 1:N | `resumen_asistencia.alumno_hash` (pseudónima, sin FK) |
| Profesor → Horario del profesor | 1:N | `horario_profesor.profesor_id` |
| Horario del profesor → Franja | 1:N | `franja_horaria.horario_profesor_id` |
| Horario del profesor → Historial de horario | 1:N | `historial_horario.horario_profesor_id` |
| Usuario → Historial de horario | 1:N | `historial_horario.realizado_por` |
| Asignatura → Franja | 1:N | `franja_horaria.asignatura_id` |
| Franja → Sesión | 1:N | `sesion_clase.franja_id` (`ON DELETE SET NULL`) |
| Curso → Sesión | 1:N | `sesion_clase.curso_escolar_id` |
| Curso → Sustitución | 1:N | `sustitucion.curso_escolar_id` |
| Asignatura → Sustitución | 1:N | `sustitucion.asignatura_id` |
| Usuario → Sustitución (titular) | 1:N | `sustitucion.profesor_titular_id` |
| Usuario → Sustitución (sustituto) | 1:N | `sustitucion.profesor_sustituto_id` |
| Sustitución → Sesión | 1:N | `sesion_clase.sustitucion_id` |
| Usuario → Sesión (cancelación) | 1:N | `sesion_clase.cancelada_por` |
| Sesión → Asistencia | 1:N | `asistencia.sesion_id` |
| Alumno → Asistencia | 1:N | `asistencia.alumno_id` |
| Asistencia → Historial | 1:N | `historial_asistencia.asistencia_id` |
| Profesor → Historial | 1:N | `historial_asistencia.cambiado_por` |
| Profesor → Observación | 1:N | `observacion.profesor_id` |
| Alumno → Observación | 1:N | `observacion.alumno_id` |
| Profesor → Preparación | 1:N | `preparacion_clase.profesor_id` |
| Asignatura → Preparación | 1:N | `preparacion_clase.asignatura_id` |
| Horario del profesor → Solicitud | 1:N | `solicitud_cambio_horario.horario_profesor_id` |
| Franja → Solicitud | 1:N | `solicitud_cambio_horario.franja_id` |

## 4. Aislamiento y pruebas

- Todas las consultas de negocio se filtran por el `mancomunidad_id` del `TenantContext`, obtenido del token.
- Las claves únicas y los índices incluyen `mancomunidad_id` para evitar colisiones entre tenants.
- Las FKs son simples; el servicio valida que cada relación une filas de la misma mancomunidad antes de guardar.
- Los tests de integración se ejecutan con **dos mancomunidades** y comprueban que:
  - un administrador de A no ve ni modifica datos de B;
  - un profesor de A no puede abrir una sesión de B cambiando un ID;
  - un usuario con pertenencia en ambas puede cambiar de mancomunidad y ve datos distintos;
  - las búsquedas, contadores y exportaciones respetan el tenant.
- Un recurso de otra mancomunidad devuelve `404`, no `403`, para no revelar su existencia.

> [!info] Límite aceptado: profesor compartido
> Un mismo usuario puede ser profesor en dos mancomunidades, pero el horario está aislado por tenant. **No se validan solapamientos del mismo profesor entre mancomunidades distintas**; solo dentro de la mancomunidad activa ([[ADR-022 - Conflictos de profesor entre mancomunidades]]). Los conflictos de alumno sí se validan dentro del tenant ([[ADR-021 - Validación de conflictos de alumno]]).

> [!danger] Sin row-level security
> MySQL no aplica aislamiento por fila. Una consulta sin filtro puede mezclar mancomunidades. El filtro por tenant es obligatorio en repositorios y `Specification`, y se revisa en cada fase que toque datos de negocio.

## 5. Retención y purga

- El historial de asistencia se conserva mientras la matrícula del alumno esté `ACTIVA` y durante **2 años** desde su baja (`matricula.baja_at`).
- Al pasar la matrícula a `BAJA` se calcula `purga_at = baja_at + 2 años`; al reactivarla se limpian `baja_at`, `purga_at` y `purgada_at`.
- Pasado el plazo, un proceso programado por mancomunidad agrega los contadores en `resumen_asistencia` (`alumno_hash`, asignatura y curso), purga el historial y las asistencias asociadas y marca `purgada_at`. Solo quedan contadores anonimizados ([[ADR-019 - Resumen de asistencia y purga anonimizada]]).
- La purga se ejecuta fuera de la API, en lotes, con registro de filas afectadas y sin tocar matrículas activas ni otros tenants. No borra sesiones, matrículas, observaciones ni preparaciones.
- Si la matrícula se reactiva antes del plazo, la purga se cancela y la traza se conserva. Si se reactiva después de una purga, se abre un nuevo periodo y el resumen acumula los nuevos contadores.
- La reapertura de un horario no elimina datos: solo cambia el estado y deja la traza en `historial_horario`.
- La baja de una matrícula no elimina asistencias ni historial; solo excluye al alumno de las listas futuras. La purga sigue rigiéndose por `purga_at` ([[ADR-026 - Baja de alumno a mitad de curso]]).
- El cierre de un curso no purga ni borra nada; solo cancela sesiones `PROGRAMADA` sin asistencia y congela horarios y matrículas ([[ADR-024 - Cierre y reapertura de curso escolar]]).
- **Seguridad:** `evento_acceso` y las ventanas de `limite_peticion` se purgan a los 30 días y 24 horas respectivamente; `evento_seguridad` se conserva 1 año; `refresh_token` revocado o caducado, 30 días; `token_recuperacion_password` y `verificacion_email` usados o caducados, 7 días; `idempotencia`, 24 horas.
- **Auditoría de plataforma:** se conserva 2 años y se purga después; las cuentas desactivadas no se borran, solo se revocan sus sesiones y tokens.
- **Logs de aplicación:** 30 días, estructurados y sin datos personales completos, contraseñas ni tokens.
- Las copias de seguridad y la exportación lógica se rigen por [[Copias de seguridad y exportación]]: copia completa diaria, 30 días de retención, 12 copias mensuales y prueba de restauración trimestral. La clave privada JWT no forma parte de la copia de la base de datos y se custodia en el gestor de secretos ([[ADR-029 - Claves JWT, rotación y secretos]]).

## 6. Datos iniciales

La migración `V2__datos_iniciales.sql` (o un `CommandLineRunner` de desarrollo) creará:

- Una mancomunidad de ejemplo `demo` con código `demo` y estado activo.
- El curso escolar `2026-2027` en estado `ACTIVO` para esa mancomunidad.
- Un centro de ejemplo.
- Un administrador inicial con email `admin@mancomunidad.local`, contraseña temporal y pertenencia `ADMINISTRADOR` a `demo`. Puede tener además `admin_plataforma = true` en desarrollo para probar el panel. La cuenta se crea con `debe_cambiar_password = true`.
- Un profesor de ejemplo con pertenencia `PROFESOR` a `demo`.
- Varias asignaturas, alumnos e instrumentos para pruebas manuales.

> [!warning] Seguridad
> Las credenciales de ejemplo solo existirán en el perfil `dev`. En producción, la primera mancomunidad y su administrador se crean desde el panel de plataforma, y se fuerza el cambio de contraseña. El primer `SUPERADMIN` se crea con el comando `--bootstrap-superadmin` y `APP_BOOTSTRAP_ENABLED=true`: la contraseña se lee de un fichero secreto montado o de la consola, nunca de una migración, argumento o variable visible. El comando es idempotente, audita `ALTA_SUPERADMIN` y sirve de break-glass ([[ADR-034 - Bootstrap del primer superadministrador]]).

## 7. Pendientes

- Revisar los índices con `EXPLAIN` en la Fase 10 y añadir los que falten mediante una migración nueva.

## Relacionado

[[Modelo de dominio]] · [[Multi-tenencia]] · [[Mancomunidad]] · [[Usuario]] · [[UsuarioMancomunidad]] · [[Centro]] · [[Asignatura]] · [[Alumno]] · [[Instrumento]] · [[Matrícula]] · [[Curso escolar]] · [[Día no lectivo]] · [[Horario del profesor]] · [[Historial de horario]] · [[Franja horaria]] · [[Sustitución]] · [[Sesión de clase]] · [[Asistencia]] · [[Historial de asistencia]] · [[Resumen de asistencia]] · [[Observación]] · [[Preparación de clase]] · [[Solicitud de cambio de horario]] · [[Copias de seguridad y exportación]] · [[ADR-004 - Horario gestionado por el profesor]] · [[ADR-008 - Curso escolar como entidad]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-013 - Generación automática de sesiones desde el horario]] · [[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]] · [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[ADR-018 - Curso escolar en las observaciones]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]] · [[ADR-020 - Borrado de franjas y conservación de sesiones]] · [[ADR-021 - Validación de conflictos de alumno]] · [[ADR-022 - Conflictos de profesor entre mancomunidades]] · [[ADR-023 - Copia de horario al curso siguiente]] · [[ADR-024 - Cierre y reapertura de curso escolar]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[ADR-026 - Baja de alumno a mitad de curso]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-033 - Idempotencia de peticiones sensibles]] · [[ADR-034 - Bootstrap del primer superadministrador]]
