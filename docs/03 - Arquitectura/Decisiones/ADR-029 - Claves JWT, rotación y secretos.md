---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - seguridad
  - autenticacion
  - jwt
  - secretos
---

# ADR-029 — Claves JWT, rotación y secretos

## Contexto

[[ADR-003 - JWT con refresh token]] fijó JWT firmados y validados con Nimbus, pero no dijo con qué clave, cómo se identifica, cómo se rota ni dónde se guarda. Sin `kid`, rotar una clave obliga a invalidar todos los tokens a la vez o a probar todas las claves en cada petición. Sin un procedimiento de rotación, una clave filtrada no se puede retirar sin dejar fuera a todos los usuarios. Y sin una ubicación explícita, es fácil acabar con la clave privada en el repositorio, en una imagen de Docker o en un log.

Tampoco existía una forma de revocar de inmediato todos los access tokens de un usuario. La documentación aceptaba que un cambio de rol o una desactivación tardara hasta 15 minutos en surtir efecto en el token en curso.

## Decisión

- Los access tokens se firman con **RS256** (RSA 2048). El backend guarda la clave privada; los clientes solo necesitan la clave pública. Se rechazan `alg = none`, HS256 y cualquier algoritmo distinto de RS256.
- El encabezado del JWT incluye **`kid`**. El token lleva además `iss`, `aud`, `iat`, `nbf`, `exp`, `jti`, `scope`, `usuarioId`, `mancomunidadId` cuando aplica, `rol` cuando aplica, `tv` (versión de la cuenta) y `tvm` (versión de la pertenencia) cuando aplica.
- Se publica un **JWKS** en `GET /.well-known/jwks.json` con las claves públicas activas para verificación. La clave privada nunca se publica ni se expone en el frontend.
- **Rotación ordinaria cada 90 días:** se genera un par nuevo, se marca como activo para firmar, se publica en el JWKS y la clave anterior se conserva **solo para verificar durante 24 horas** (más que la vida del access token y el margen de reloj). Después se retira. Los refresh tokens son opacos y sobreviven a la rotación.
- **Rotación de emergencia:** si una clave privada se compromete, se retira del JWKS de inmediato; todos los access tokens firmados con ella dejan de validar. Los clientes renuevan con su refresh opaco. Si también se comprometieron los refresh, se revocan todas las familias del usuario o de la plataforma.
- **Revocación masiva de access tokens:** `usuario.token_version` y `usuario_mancomunidad.token_version` son contadores que viajan en el token. El filtro JWT los compara con la base de datos mediante una caché de 60 segundos; si no coinciden, responde `401`. Se incrementan al desactivar la cuenta, al desactivar o cambiar el rol de una pertenencia, al cambiar o restablecer la contraseña, al detectar reutilización de refresh y al ejecutar «cerrar todas las sesiones».
- La comprobación de `tv`/`tvm` no convierte la API en stateful: el token sigue siendo la credencial y la caché solo evita una consulta por petición. La ventana máxima de revocación es de 60 segundos, salvo rotación de emergencia, que es inmediata.
- **Secretos:** la clave privada, la pública, el secreto de purga y las credenciales SMTP viven en un gestor de secretos o en ficheros montados (Docker secrets / Kubernetes secrets). La aplicación los lee por ruta (`APP_JWT_PRIVATE_KEY_PATH`, `APP_JWT_PUBLIC_KEY_PATH`), no por valor en variables de entorno visibles en `ps` o `docker inspect`. En local se generan con un script en una carpeta ignorada; `.env.example` solo lleva rutas y valores ficticios.
- Los secretos **nunca** se guardan en Git, en la imagen, en el frontend, en logs ni en la respuesta de un endpoint. La clave privada no se incluye en copias de seguridad lógicas de la base de datos; su copia se custodia aparte.
- Se valida `iss`, `aud`, `exp`, `nbf` e `iat` con un margen de reloj de 60 segundos. Un `kid` desconocido o un token con firma inválida responde `401` genérico.
- No hay lista negra de access tokens en la v1: la vida de 15 minutos y `tv`/`tvm` cubren la revocación. El `jti` queda disponible para una futura denylist si hiciera falta.

## Consecuencias

### Positivas

- Rotar una clave no obliga a invalidar todos los tokens ni a desplegar a la vez en todos los clientes.
- Una clave comprometida se puede retirar de inmediato.
- La revocación de una cuenta o de una pertenencia se aplica en menos de un minuto, no en 15.
- Los secretos tienen una ubicación explícita y no dependen de que nadie copie una variable a mano.
- El JWKS permite verificar tokens sin compartir la clave privada.

### Negativas / compromisos

- La rotación y el JWKS añaden operación: generar claves, publicarlas, esperar 24 horas y retirar la anterior.
- La caché de versiones introduce hasta 60 segundos de retraso y una dependencia de caché en el filtro.
- Cambiar la contraseña invalida también la sesión actual; el usuario debe volver a entrar.
- Perder la clave privada sin copia obliga a rotar y a que todos los usuarios renueven; no se puede recuperar un token firmado con ella.
- La rotación de emergencia invalida todos los access tokens, incluidos los de usuarios legítimos, y genera una renovación masiva.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Clave simétrica HS256 en variable de entorno | Cualquiera que la lea puede firmar tokens; no permite publicar solo la parte de verificación. |
| Una única clave sin `kid` | Rotar exige probar todas las claves o invalidar todo; no hay identificación de la clave. |
| Rotar en cada despliegue | Invalida sesiones sin necesidad y complica la operación. |
| Guardar la clave en el repositorio o en la imagen | Un secreto versionado es un secreto comprometido. |
| Lista negra de access tokens en base de datos | Añade estado y una consulta por petición; `tv`/`tvm` y la vida corta bastan para la v1. |
| Revocación solo al refrescar | Deja hasta 15 minutos de acceso tras una desactivación; se prefiere la comprobación de versión. |

## Relacionado

[[Seguridad y autenticación]] · [[Stack tecnológico]] · [[Requisitos no funcionales]] · [[API REST]] · [[Modelo de datos]] · [[Usuario]] · [[UsuarioMancomunidad]] · [[ADR-003 - JWT con refresh token]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-034 - Bootstrap del primer superadministrador]]
