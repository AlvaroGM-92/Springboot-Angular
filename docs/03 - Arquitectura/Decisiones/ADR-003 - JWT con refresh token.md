---
tipo: adr
estado: aceptada
fecha: 2026-10-27
actualizado: 2026-11-04
tags:
  - adr
  - seguridad
  - autenticacion
---

# ADR-003 — JWT con refresh token

## Contexto

Angular es una SPA y Spring Boot una API REST. Hay que mantener la sesión sin pedir credenciales en cada petición y sin exponer un token de larga duración en el navegador. Además, una misma identidad puede pertenecer a varias mancomunidades, así que la sesión debe fijar una mancomunidad activa.

## Decisión

- **Access token JWT** de 15 minutos, en memoria en Angular y en `Authorization: Bearer`.
- El access token incluye `usuarioId`, `mancomunidadId`, `rol` y `scope` (`TENANT` o `PLATFORM`).
- **Refresh token** opaco de 7 días, en cookie `HttpOnly` + `Secure` + `SameSite=Strict`, ligado a la sesión y a la mancomunidad activa.
- **Token de selección** de 5 minutos cuando el usuario tiene varias pertenencias; solo permite elegir una mancomunidad de su lista.
- El refresh token se guarda **hasheado** en MySQL y **rota** en cada uso.
- Al cerrar sesión se revoca el refresh token.
- Al desactivar una pertenencia se revocan los refresh tokens de ese usuario para esa mancomunidad.
- Spring Security valida el JWT con Nimbus JOSE (sin librería externa adicional).
- Los permisos se comprueban por mancomunidad, rol y propiedad del recurso.

> [!info] Actualización 2026-11-04
> La firma, el `kid`, la rotación y la revocación masiva se detallan en [[ADR-029 - Claves JWT, rotación y secretos]]. La reutilización de un refresh rotado invalida toda la familia en [[ADR-030 - Detección de reutilización de refresh tokens]]. El máximo de sesiones y el cierre global están en [[ADR-031 - Límite de sesiones concurrentes y cierre global]].

## Consecuencias

### Positivas

- El access token no queda persistido en el navegador.
- El refresh token no es legible por JavaScript.
- La rotación detecta reutilización de tokens robados.
- La API es stateless para las peticiones normales.

### Negativas / compromisos

- Hay que gestionar la tabla de refresh tokens y su limpieza; ahora incluye mancomunidad y scope.
- El endpoint de refresh usa cookie, así que necesita protección CSRF específica.
- Al recargar la página hay una llamada extra para recuperar el access token.
- La revocación inmediata del access token se apoya en `usuario.token_version` y `usuario_mancomunidad.token_version`, comprobados con una caché máxima de 60 segundos ([[ADR-029 - Claves JWT, rotación y secretos]]).
- El login tiene más estados: 0, 1 o varias pertenencias, y plataforma.
- Un cambio de rol o de pertenencia incrementa `token_version` y revoca los refresh de esa mancomunidad; el access token en curso deja de valer en menos de 60 segundos.
- El token de selección añade un paso y una superficie que hay que validar con cuidado.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Sesión con cookie de servidor | Sencilla, pero menos didáctica para una SPA y requiere sesiones compartidas si escala. |
| JWT de larga duración en localStorage | Riesgo alto ante XSS. |
| OAuth2 / Keycloak | Complejidad innecesaria para el alcance actual. |
| Roles globales en el token sin selector | Impide que una persona sea profesora en A y administradora en B. |

## Relacionado

[[Seguridad y autenticación]] · [[Multi-tenencia]] · [[UsuarioMancomunidad]] · [[API REST]] · [[Requisitos no funcionales]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]]
