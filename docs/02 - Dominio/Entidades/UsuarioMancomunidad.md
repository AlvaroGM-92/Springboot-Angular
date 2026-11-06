---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-04
tags:
  - dominio
  - entidad
  - multi-tenant
  - seguridad
---

# UsuarioMancomunidad

## Propósito

Representa la **pertenencia** de un [[Usuario]] a una [[Mancomunidad]] y guarda el rol que tiene dentro de ella. Es la pieza que permite que una misma persona sea profesora en una mancomunidad y administradora en otra sin duplicar cuentas.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `usuario_id` | BIGINT | Sí | FK a [[Usuario]]. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]. |
| `rol` | VARCHAR(20) | Sí | `PROFESOR` o `ADMINISTRADOR`. |
| `token_version` | BIGINT | Sí | `0` por defecto; se incrementa al cambiar el rol o desactivar la pertenencia. |
| `activo` | BOOLEAN | Sí | Por defecto `true`. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Usuario]]**.
- **N:1 con [[Mancomunidad]]**.

## Reglas

- Único por `(usuario_id, mancomunidad_id)`.
- Un usuario puede tener varias pertenencias, con roles distintos.
- El rol solo tiene efecto dentro de esa mancomunidad.
- Un usuario debe tener al menos una pertenencia activa para entrar en una mancomunidad; el acceso de plataforma es independiente y no la requiere.
- Cada mancomunidad debe conservar al menos un `ADMINISTRADOR` activo.
- Un administrador no puede quitarse a sí mismo el último rol de administrador de su mancomunidad.
- Desactivar una pertenencia revoca los refresh tokens de ese usuario para esa mancomunidad e incrementa `token_version` de la pertenencia.
- Cambiar el rol incrementa `token_version` de la pertenencia y revoca los refresh tokens de esa mancomunidad; el access token en curso deja de valer en menos de 60 segundos ([[ADR-029 - Claves JWT, rotación y secretos]]).
- No se puede desactivar una pertenencia `PROFESOR` mientras el usuario sea sustituto en una [[Sustitución]] `ACTIVA`; antes debe anularse la sustitución ([[ADR-025 - Sustituciones y cambio de titular]]).
- La pertenencia no concede acceso a la plataforma; el acceso de plataforma es independiente.
- El administrador de mancomunidad no puede cambiar el email del usuario; la dirección solo la cambia el titular o el soporte, con doble confirmación ([[ADR-028 - Verificación y cambio de email]]).
- No se borra físicamente si el usuario ha generado datos: se desactiva para conservar la autoría.
- Los cambios de rol y estado quedan auditados.

## Dudas

- ¿Se permite que un usuario sea administrador de plataforma y, a la vez, profesor de una mancomunidad? Sí; son ámbitos distintos.
- ¿Se puede cambiar de rol sin cerrar sesión? El cambio revoca los refresh de esa mancomunidad e incrementa `token_version`, así que el access token en curso deja de valer en menos de 60 segundos; el usuario debe renovar.
- ¿Se guarda histórico de roles? En la v1 se conserva el estado actual y la auditoría básica; un histórico detallado queda para más adelante.

## Relacionado

[[Usuario]] · [[Mancomunidad]] · [[Multi-tenencia]] · [[Roles y permisos]] · [[Seguridad y autenticación]] · [[Sustitución]] · [[Modelo de datos]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]]
