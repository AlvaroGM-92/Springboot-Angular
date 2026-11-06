---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - seguridad
  - autenticacion
  - limites
---

# ADR-032 — Rate limiting de extremos sensibles

## Contexto

[[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]] limita el login, pero deja sin protección otros puntos igual de sensibles: la solicitud de recuperación de contraseña puede usarse para inundar de correos o enumerar cuentas; el refresh puede martillearse con tokens robados; las escrituras autenticadas pueden saturar la base de datos o generar correos; y las acciones de soporte pueden repetirse en bucle. El login no es la única puerta.

La v1 no usa Redis. El límite tiene que funcionar con MySQL y con varias instancias del backend, igual que los contadores de intentos fallidos.

## Decisión

- Se añade una tabla `limite_peticion` con `clave`, `ventana_inicio`, `contador` y `created_at`, y una clave única `(clave, ventana_inicio)`. La clave es un texto derivado del ámbito y del sujeto, nunca un email en claro: `recovery:email:<sha256>`, `recovery:ip:<ip>`, `refresh:familia:<id>`, `refresh:ip:<ip>`, `escritura:usuario:<id>`, `soporte:actor:<id>`.
- El incremento es atómico con `INSERT ... ON DUPLICATE KEY UPDATE contador = contador + 1`. No hay estado en memoria ni dependencia de Redis.
- Los límites de la v1 son:

| Ámbito | Clave | Límite | Respuesta |
| --- | --- | --- | --- |
| Recuperación por email | `recovery:email:<hash>` | 3 por hora | `429` |
| Recuperación por IP | `recovery:ip:<ip>` | 10 por hora | `429` |
| Reenvío de verificación de email | `verificacion:usuario:<id>` | 3 por hora | `429` |
| Refresh por familia | `refresh:familia:<id>` | 120 por hora | `429` |
| Refresh por IP | `refresh:ip:<ip>` | 600 por hora | `429` |
| Escrituras autenticadas | `escritura:usuario:<id>` | 60 por minuto y 1.000 por hora | `429` |
| Acciones de soporte | `soporte:actor:<id>` | 60 por hora | `429` |
| Login | `evento_acceso` (ADR-014) | 5 por cuenta / 20 por IP en 15 min | `429` |

- Se consideran **escrituras sensibles** los `POST`, `PUT`, `PATCH` y `DELETE` autenticados bajo `/api/v1`, salvo el refresh, que tiene su propio cubo, y el logout, que solo cierra la sesión actual. Las lecturas no se limitan en la v1.
- La respuesta es `429` con `application/problem+json`, mensaje genérico, `Retry-After` en segundos y, cuando se conoce, `X-RateLimit-Limit`, `X-RateLimit-Remaining` y `X-RateLimit-Reset`. No revela si un email existe ni si una cuenta está bloqueada.
- Los límites son globales por configuración (`APP_RATE_*`); no se configuran por mancomunidad en la v1. Las rutas de salud, el JWKS y los recursos estáticos quedan excluidos.
- La ventana es fija y se alinea al inicio del minuto o de la hora. Un pico en el cambio de ventana puede permitir hasta el doble del límite; se acepta porque el objetivo es frenar abuso, no facturar.
- La tabla se limpia a las **24 horas** con una tarea programada; las ventanas antiguas no se consultan.
- Un `429` por rate limiting no es un bloqueo de cuenta: no altera `evento_acceso` ni impide el login si el cubo afectado es otro. El bloqueo de login sigue rigiéndose por ADR-014.

## Consecuencias

### Positivas

- La recuperación, el refresh, las escrituras y el soporte dejan de ser ilimitados.
- El mecanismo funciona con varias instancias y sobrevive a reinicios, sin añadir Redis.
- La clave hasheada evita guardar emails en una tabla de contadores.
- El `Retry-After` permite al frontend esperar en lugar de reintentar en bucle.

### Negativas / compromisos

- Cada petición sensible escribe o actualiza una fila; hay que vigilar el volumen y el índice.
- La ventana fija permite un pico al cambiar de ventana.
- Un usuario legítimo con automatización puede chocar con el límite de 60 escrituras por minuto.
- El límite por IP puede afectar a un centro con NAT compartido, igual que en el login; los umbrales son más altos que los de cuenta.
- No hay configuración por tenant ni panel de ajuste; un cambio exige despliegue.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Solo limitar el login | Deja sin protección la recuperación, el refresh y las escrituras. |
| Redis con token bucket | Mejor rendimiento, pero añade infraestructura que la v1 no tiene. |
| Contadores en memoria | No se comparten entre instancias y se pierden al reiniciar. |
| Límite global por IP para todo | Bloquea un centro entero por el uso normal de varios usuarios. |
| Ventana deslizante exacta | Más justa, pero más consultas y complejidad; la ventana fija basta para el objetivo. |
| Devolver `423 Locked` o un mensaje específico | Distingue bloqueo de rate limit y facilita la enumeración. |

## Relacionado

[[Seguridad y autenticación]] · [[Requisitos no funcionales]] · [[Modelo de datos]] · [[API REST]] · [[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-033 - Idempotencia de peticiones sensibles]]
