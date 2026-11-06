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

# ADR-031 — Límite de sesiones concurrentes y cierre global

## Contexto

El refresh token de 7 días permite mantener la sesión sin volver a escribir la contraseña. No había límite de cuántas sesiones podía tener una misma cuenta ni una forma de cerrarlas todas si se sospechaba que un dispositivo se había perdido. Un usuario podía acumular sesiones indefinidamente y, si una de ellas se comprometía, no tenía una acción única para cortarlas.

Tampoco estaba decidido si el límite debía contar por mancomunidad, por scope o por cuenta. Un usuario con pertenencias en dos mancomunidades y acceso a plataforma podría necesitar varias sesiones legítimas a la vez.

## Decisión

- Cada cuenta puede mantener como máximo **5 familias de refresh activas** (no revocadas y no caducadas), contando todas sus mancomunidades y su acceso a plataforma. El valor es configurable con `APP_MAX_SESIONES`, por defecto 5.
- Al iniciar sesión o al seleccionar una mancomunidad, si ya hay 5 familias activas, se revoca la **más antigua por último uso** (`ultimo_uso_at`) antes de crear la nueva. El login nuevo siempre tiene éxito; la sesión desalojada recibe `401` en su siguiente refresh.
- Se añade `ultimo_uso_at` a `refresh_token`, actualizado en cada rotación. La lista de sesiones se ordena por ese campo.
- `GET /auth/sesiones` devuelve las familias activas del usuario con `familiaId`, fecha de creación, último uso, IP y `userAgent`; no devuelve tokens ni datos académicos. La sesión actual se marca con `actual = true`.
- `POST /auth/logout-all` exige la contraseña actual, revoca todas las familias activas, incrementa `usuario.token_version` ([[ADR-029 - Claves JWT, rotación y secretos]]), borra la cookie de refresh y responde `204`. Es la acción de «cerrar todas las sesiones».
- `POST /auth/logout` sigue cerrando solo la familia actual.
- En la v1 **no hay revocación individual** desde la lista de sesiones: se puede consultar y cerrar todo, pero no cerrar una sesión concreta. Se pospone para no añadir otra ruta y otra prueba de propiedad.
- El administrador de mancomunidad no puede listar ni cerrar las sesiones de otros usuarios. El `SUPERADMIN` tampoco las lista; sus acciones de soporte (reset, desactivación, cambio de rol o de email) ya revocan las sesiones del afectado.
- Cambiar o restablecer la contraseña, desactivar la cuenta, desactivar o cambiar el rol de una pertenencia, cambiar el email confirmado o detectar reutilización de refresh revoca **todas** las familias del usuario, no solo una.
- Las familias revocadas o caducadas se purgan a los **30 días**. La comprobación del límite solo cuenta las activas.

## Consecuencias

### Positivas

- Una cuenta no acumula sesiones sin límite y el desalojo es predecible.
- El usuario tiene una acción clara para cortar todas las sesiones si pierde un dispositivo.
- La lista de sesiones ayuda a detectar accesos que no reconoce sin exponer tokens.
- El cierre global invalida también los access tokens en curso, no solo los refresh.

### Negativas / compromisos

- Un usuario que trabaje en varios dispositivos o mancomunidades puede quedarse fuera de la sesión más antigua sin aviso.
- El desalojo por límite no distingue dispositivos de confianza; la v1 no tiene ese concepto.
- `logout-all` pide la contraseña, así que no sirve si el usuario ya no la recuerda; en ese caso debe usar la recuperación.
- La lista de sesiones guarda IP y `userAgent`, datos que hay que tratar con cuidado y purgar.
- No se puede cerrar una sesión concreta; si se detecta una sospechosa, la única acción es cerrar todas.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Sin límite de sesiones | Permite acumulación indefinida y dificulta cortar un dispositivo perdido. |
| Limitar por mancomunidad | Un usuario con varias pertenencias podría tener 5 por cada una; el riesgo global no se acota. |
| Desalojar la sesión más nueva | Castiga al usuario que acaba de entrar y no reduce el riesgo de la más antigua. |
| Cerrar la sesión individual desde la lista | Útil, pero añade una ruta, una prueba de propiedad y una UI; se pospone. |
| Guardar un identificador de dispositivo de confianza | Exige registro y gestión de dispositivos; fuera de la v1. |
| Revocar solo el refresh al cerrar todas | El access token seguiría vivo hasta 15 minutos; se incrementa la versión. |

## Relacionado

[[Seguridad y autenticación]] · [[Usuario]] · [[Modelo de datos]] · [[API REST]] · [[Requisitos funcionales]] · [[ADR-003 - JWT con refresh token]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-032 - Rate limiting de extremos sensibles]]
