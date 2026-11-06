---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-04
tags:
  - dominio
  - entidad
  - seguridad
  - multi-tenant
---

# Usuario

## Propósito

Representa la **identidad global** de una persona que accede al sistema. Contiene las credenciales y el estado de la cuenta. El rol no vive aquí: depende de la mancomunidad en la que esté trabajando.

Una misma persona puede ser profesora en una mancomunidad y administradora en otra sin duplicar la cuenta.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `nombre` | VARCHAR(100) | Sí | 2–100 caracteres. |
| `apellidos` | VARCHAR(150) | Sí | 2–150 caracteres. |
| `email` | VARCHAR(150) | Sí | Único global, formato válido, almacenado en minúsculas. |
| `password_hash` | VARCHAR(100) | Sí | BCrypt; nunca se expone. |
| `debe_cambiar_password` | BOOLEAN | Sí | `false` por defecto; mientras esté activo el usuario solo puede cambiar la contraseña o cerrar sesión. |
| `password_temporal_expira_at` | DATETIME | No | Caducidad de la contraseña temporal, 24 horas desde el reset. |
| `email_verificado` | BOOLEAN | Sí | `false` por defecto; habilita la recuperación autoservicio cuando pasa a `true`. |
| `email_verificado_at` | DATETIME | No | Fecha en que se confirmó la dirección actual. |
| `token_version` | BIGINT | Sí | `0` por defecto; se incrementa para invalidar todos los access tokens de la cuenta. |
| `admin_plataforma` | BOOLEAN | Sí | `false` por defecto; concede acceso al panel de plataforma y al soporte de cuentas. |
| `activo` | BOOLEAN | Sí | Por defecto `true`. |
| `ultimo_acceso` | DATETIME | No | Se actualiza al iniciar sesión. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **1:N con [[UsuarioMancomunidad]]**: pertenencias y rol en cada mancomunidad.
- **N:M con [[Asignatura]]** a través de `asignatura_profesor`, siempre dentro de una mancomunidad.
- **1:N con [[Franja horaria]]** como profesor.
- **1:N con [[Observación]]** como autor.
- **1:N con [[Preparación de clase]]** como autor.
- **1:N con [[Solicitud de cambio de horario]]** como solicitante.
- **1:N con [[Sesión de clase]]** como profesor.
- **1:N con [[Sustitución]]** como titular o sustituto.
- **1:N con `refresh_token`** para la sesión.
- **1:N con `token_recuperacion_password`** para la recuperación de contraseña.
- **1:N con `verificacion_email`** para confirmar el alta o un cambio de dirección.
- **1:N con `evento_seguridad`** para reutilización de refresh, cambios de email y revocaciones masivas.
- **1:N con `idempotencia`** para las peticiones con `Idempotency-Key`.

## Reglas

- El email es único **globalmente** y no distingue mayúsculas.
- Un usuario no se borra: se desactiva.
- Un usuario debe tener al menos una [[UsuarioMancomunidad]] activa para operar en una mancomunidad.
- El rol (`PROFESOR` o `ADMINISTRADOR`) se guarda en la pertenencia, no en el usuario.
- Un usuario puede tener roles distintos en mancomunidades distintas.
- No se puede desactivar el último administrador de plataforma activo.
- Un administrador de mancomunidad no puede desactivarse a sí mismo si es el último de esa mancomunidad.
- Las contraseñas cumplen la política: mínimo 8 caracteres, con letra y número; **12 para cuentas con `admin_plataforma`**.
- Una contraseña temporal caduca a las 24 horas y obliga a cambiarla en la primera entrada.
- Todo cambio o restablecimiento de contraseña revoca los refresh tokens del usuario e incrementa `token_version` ([[ADR-029 - Claves JWT, rotación y secretos]]).
- El reset por administrador o soporte nunca devuelve la contraseña en la API y exige motivo y auditoría.
- El token de recuperación es de un solo uso, se guarda hasheado y caduca en 60 minutos.
- Al desactivar un usuario se revocan todos sus refresh tokens y se incrementa `token_version`.
- Al desactivar una pertenencia se revocan los refresh tokens de ese usuario para esa mancomunidad y se incrementa su `token_version`.
- El email debe estar verificado para usar la recuperación autoservicio; el alta envía una verificación de 24 horas y el cambio de dirección exige confirmar la nueva antes de usarla ([[ADR-028 - Verificación y cambio de email]]).
- El administrador de mancomunidad **no puede cambiar el email** de un usuario de su tenant. Solo el titular, con la contraseña actual, o el soporte, con motivo y auditoría, inician un cambio, que siempre requiere confirmar la dirección nueva.
- Un usuario puede tener como máximo **5 familias de refresh activas**; puede consultarlas y cerrarlas todas con la contraseña actual ([[ADR-031 - Límite de sesiones concurrentes y cierre global]]).
- La MFA queda fuera de la v1 para todas las cuentas, incluida `admin_plataforma`; se registra como riesgo aceptado con controles compensatorios y revisión antes de producción ([[ADR-027 - MFA y riesgo aceptado del superadministrador]]).
- `admin_plataforma` no concede acceso a los datos académicos de una mancomunidad; para eso hace falta una pertenencia.

## Dudas

- ¿Un usuario pertenece a un centro? No directamente: los centros se derivan de las asignaturas que imparte.
- ¿Se permite más de un rol por mancomunidad? No; un rol por pertenencia.
- ¿Puede un usuario no pertenecer a ninguna mancomunidad? Sí, si es administrador de plataforma. Si no lo es, no puede entrar.
- ¿Se notifica al usuario cuando se le añade a una mancomunidad? En la v1 se verá al iniciar sesión; no hay notificaciones de negocio por email.
- ¿Se puede recuperar la contraseña por email? Sí, con un enlace de un solo uso; es correo transaccional, no una notificación ([[ADR-011 - Recuperación de contraseña y correo transaccional]]).
- ¿Puede el soporte de plataforma ver o cambiar la contraseña de un usuario? No la ve nunca; solo puede generar una temporal con cambio obligatorio ([[ADR-012 - Soporte de cuentas del superadministrador]]).
- ¿Puede el administrador de mancomunidad cambiar el email de un profesor? No; solo el titular o el soporte, y el cambio no se aplica hasta confirmar la dirección nueva ([[ADR-028 - Verificación y cambio de email]]).
- ¿La falta de verificación impide entrar? No; permite iniciar sesión y operar, pero deshabilita la recuperación autoservicio.
- ¿El primer superadministrador se siembra en una migración? No; se crea con un comando de bootstrap auditado y sin contraseña versionada ([[ADR-034 - Bootstrap del primer superadministrador]]).

## Relacionado

[[UsuarioMancomunidad]] · [[Mancomunidad]] · [[Multi-tenencia]] · [[Roles y permisos]] · [[Seguridad y autenticación]] · [[Sustitución]] · [[Modelo de datos]] · [[API REST]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-034 - Bootstrap del primer superadministrador]]
