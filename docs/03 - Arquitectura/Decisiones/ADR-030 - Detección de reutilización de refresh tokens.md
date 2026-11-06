---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - seguridad
  - autenticacion
  - sesiones
---

# ADR-030 — Detección de reutilización de refresh tokens

## Contexto

[[ADR-003 - JWT con refresh token]] decide que el refresh token rota en cada uso y se guarda hasheado. La rotación, por sí sola, no dice qué hacer si llega un token que ya fue rotado: puede ser un reintento legítimo por una red inestable o la señal de que alguien copió el token y lo está usando en paralelo. Si se acepta sin más, el atacante mantiene la sesión; si se rechaza sin cerrar nada, el token robado puede seguir intentándolo.

La documentación mencionaba «la rotación detecta reutilización», pero no definía la respuesta, el alcance de la revocación ni dónde se auditaba.

## Decisión

- Cada `refresh_token` pertenece a una **familia** identificada por `familia_id` (UUID). Todos los tokens emitidos al rotar una misma sesión comparten familia.
- La rotación es **atómica**: en una transacción se marca el token usado como `revocado = true`, con `revocado_at`, `motivo_revocacion = ROTACION` y `reemplazado_por` apuntando al nuevo token, y se inserta el nuevo. Si dos peticiones compiten, solo una gana.
- **Detección de reutilización:** si llega un token que existe, está revocado y tiene `reemplazado_por` (es decir, ya fue rotado), se considera reutilización. Entonces:
  - se revocan **todos** los tokens de la familia, incluido el sucesor;
  - se incrementa `usuario.token_version` para invalidar los access tokens en curso ([[ADR-029 - Claves JWT, rotación y secretos]]);
  - se escribe un evento `REUTILIZACION_REFRESH` en `evento_seguridad` con usuario, mancomunidad, IP, familia y fecha, sin guardar el token;
  - se responde `401` genérico. El usuario debe volver a iniciar sesión.
- Si el token está revocado por `LOGOUT`, `CAMBIO_PASSWORD`, `CAMBIO_EMAIL`, `DESACTIVACION`, `CAMBIO_ROL` o `LIMITE_SESIONES`, no se trata como reutilización: la familia ya está cerrada y se responde `401` sin volver a revocarla.
- Si el token no existe, está caducado o el hash no coincide, se responde `401` genérico. No se revela si la familia existe.
- **No hay periodo de gracia.** Un reintento con el token anterior se considera reutilización. Para evitarlo, el interceptor de Angular hace **single-flight**: si hay un refresh en curso, las peticiones esperan al mismo resultado en lugar de lanzar varios refresh en paralelo.
- **Logout** revoca la familia completa, no solo el token actual. Así una copia anterior del token tampoco sirve.
- Los tokens caducados o revocados se conservan el tiempo necesario para detectar la reutilización y se purgan a los **30 días**; la familia se puede reconstruir por `familia_id`.
- El evento de seguridad es append-only y se conserva **1 año**. La tabla `evento_seguridad` es global, no de tenant, porque una reutilización puede afectar a un usuario con varias pertenencias.

## Consecuencias

### Positivas

- Un refresh robado deja de ser útil en cuanto el titular lo usa o en cuanto el atacante lo usa después de una rotación.
- La respuesta es determinista y no depende de heurísticas.
- La traza permite investigar un incidente y saber qué familia y qué IP estaban implicadas.
- El logout cierra de verdad la sesión, incluidas copias antiguas del token.

### Negativas / compromisos

- Un cliente que lance refresh en paralelo se cerrará la sesión a sí mismo; hay que implementar single-flight y probarlo.
- Una red que reintenta una petición ya procesada puede provocar un falso positivo y obligar a un nuevo login.
- La revocación de la familia y el incremento de versión son escrituras adicionales en el camino de refresh.
- Conservar tokens revocados 30 días ocupa espacio y exige una purga fiable.
- El evento de seguridad no notifica al usuario en la v1; solo queda para operación y auditoría.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Aceptar el token rotado y devolver el mismo sucesor | Mantiene viva la sesión del atacante y anula la detección. |
| Rechazar solo el token reutilizado, sin cerrar la familia | El sucesor sigue válido y el atacante puede continuar. |
| Periodo de gracia de 30 segundos | Reduce falsos positivos, pero abre una ventana para usar dos copias; se prefiere single-flight en el cliente. |
| Revocar solo el refresh y no el access token | El access token robado sigue vivo hasta 15 minutos; se incrementa la versión. |
| Guardar el token en claro para comparar | Un robo de base de datos expone todas las sesiones. |
| No auditar la reutilización | Impide investigar si fue un ataque o un fallo de cliente. |

## Relacionado

[[Seguridad y autenticación]] · [[Usuario]] · [[Modelo de datos]] · [[API REST]] · [[Requisitos funcionales]] · [[ADR-003 - JWT con refresh token]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]]
