---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - api
  - seguridad
  - idempotencia
---

# ADR-033 — Idempotencia de peticiones sensibles

## Contexto

El control de concurrencia por `version` evita pisar cambios, pero no evita **duplicar** una operación: si el cliente envía una matrícula, un cierre de curso o una copia de horario y la respuesta se pierde por un corte de red, el reintento puede crear una segunda fila o ejecutar dos veces una transición. En los fichajes offline ya se había previsto una clave de idempotencia, pero el resto de altas sensibles no tenía nada.

No todas las rutas necesitan lo mismo: un `GET` es idempotente por definición y el login o la recuperación ya están limitados y diseñados para repetirse sin efectos. Añadir la cabecera a todo complica el cliente sin aportar valor.

## Decisión

- Se usa la cabecera **`Idempotency-Key`** en las peticiones que crean o disparan efectos sensibles. El cliente genera una clave única por intento (UUID v4 o cadena de 16–64 caracteres `[A-Za-z0-9_-]`) y **la reutiliza solo al reintentar la misma operación**.
- Es **obligatoria** en:
  - `POST /alumnos/{id}/matriculas`
  - `POST /horarios`, `POST /horarios/{id}/franjas`, `POST /horarios/{id}/finalizar`, `POST /horarios/{id}/copiar`
  - `POST /sesiones`, `POST /sesiones/{id}/abrir`
  - `POST /sustituciones`
  - `POST /cursos-escolares/{id}/cerrar`, `POST /cursos-escolares/{id}/reabrir`
  - `POST /plataforma/mancomunidades`
  - `POST /plataforma/usuarios/{id}/reset-password`, `POST /plataforma/usuarios/{id}/desbloquear`
- Es **opcional** en el resto de `POST` y en los `PATCH`/`PUT` con efecto; si se envía, se aplica el mismo mecanismo. No se admite en `GET`, `HEAD` ni `OPTIONS`.
- Se añade la tabla `idempotencia` con `usuario_id`, `clave`, `metodo`, `ruta`, `hash_peticion`, `estado` (`EN_CURSO` o `COMPLETADA`), `respuesta_status`, `respuesta_body`, `created_at` y `expira_at`. La clave única es `(usuario_id, clave)`.
- El `hash_peticion` es el SHA-256 del cuerpo canónico (JSON con claves ordenadas y sin la propia cabecera). Si la misma clave llega con otro cuerpo o a otra ruta, no es un reintento: es un conflicto.
- Flujo:
  1. Se intenta insertar la fila en estado `EN_CURSO` dentro de la misma transacción que la operación de negocio.
  2. Si la clave ya existe y está `COMPLETADA` con el mismo hash y la misma ruta, se devuelve la respuesta guardada con el mismo código y cuerpo, añadiendo `Idempotent-Replay: true`. No se ejecuta otra vez el caso de uso.
  3. Si está `COMPLETADA` con otro hash o otra ruta, responde `409` con el código `CLAVE_IDEMPOTENCIA_REUTILIZADA`.
  4. Si está `EN_CURSO`, responde `409` con el código `OPERACION_EN_CURSO` y `Retry-After: 1`; el cliente debe esperar y reintentar con la misma clave.
  5. Si la operación de negocio falla y la transacción hace rollback, la fila de idempotencia también desaparece: el reintento vuelve a intentarlo desde cero.
  6. Si la operación termina bien, la fila se marca `COMPLETADA` con el estado y el cuerpo de la respuesta antes de confirmar la transacción.
- Si el servidor cae después del commit y antes de enviar la respuesta, el reintento encuentra la fila `COMPLETADA` y devuelve la misma respuesta.
- Las claves caducan a las **24 horas** y se purgan con una tarea programada. Una clave caducada se trata como nueva; por eso el cliente no debe reutilizarla más allá de un reintento inmediato.
- Un `Idempotency-Key` ausente o inválido en una ruta obligatoria responde `400` con el detalle del campo. No se genera automáticamente en el servidor: la clave debe ser estable entre reintentos.
- La idempotencia **no sustituye** a `version` ni a las reglas de negocio: evita duplicar el efecto de un reintento, pero no resuelve dos operaciones distintas del mismo usuario.

## Consecuencias

### Positivas

- Un corte de red no duplica matrículas, sesiones, cierres de curso ni copias de horario.
- El cliente puede reintentar con seguridad y recibe la misma respuesta.
- La detección de una clave reutilizada con otro cuerpo evita mezclar operaciones distintas.
- El mecanismo convive con `version` y con las validaciones de negocio.

### Negativas / compromisos

- Hay una tabla y una escritura adicional en cada operación protegida.
- El cliente debe generar y conservar la clave; si la pierde, el reintento puede duplicar.
- Guardar el cuerpo de la respuesta puede aumentar el tamaño de la tabla y exponer datos en un almacenamiento temporal; se cifra en reposo y se purga a las 24 horas.
- Un `EN_CURSO` bloqueado por un proceso muerto requiere esperar a la limpieza o a un timeout; se documenta `Retry-After` y se purga.
- No cubre operaciones que el usuario decide repetir a propósito; para eso están las reglas de duplicados.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Confiar solo en `version` | Detecta conflictos, no duplicados de una operación ya aplicada. |
| Clave generada por el servidor | No sirve para reintentos: cada petición obtendría una clave distinta. |
| Guardar solo el resultado y no el cuerpo | El reintento no puede devolver la misma respuesta al cliente. |
| Exigir la cabecera en todos los endpoints | Complica el cliente y no aporta nada en lecturas o login. |
| Usar una tabla en memoria | Se pierde al reiniciar y no se comparte entre instancias. |
| Devolver `409` en todos los reintentos | El cliente no puede distinguir un duplicado legítimo de un conflicto real. |

## Relacionado

[[Convenciones]] · [[API REST]] · [[Ejemplos de API]] · [[Modelo de datos]] · [[Requisitos funcionales]] · [[Seguridad y autenticación]] · [[ADR-003 - JWT con refresh token]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-032 - Rate limiting de extremos sensibles]]
