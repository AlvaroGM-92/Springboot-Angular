---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - seguridad
  - autenticacion
  - usuarios
---

# ADR-028 — Verificación y cambio de email

## Contexto

El email es la identidad de login y el único canal de recuperación de contraseña. La documentación ya decía que un email mal escrito deja la cuenta sin recuperación y que el soporte puede corregirlo, pero no existía un flujo de verificación. Sin él, cualquiera puede escribir una dirección inexistente o ajena al dar de alta una cuenta, y la recuperación se convierte en una promesa que no se puede cumplir.

Tampoco estaba decidido quién puede cambiar el email. Si un administrador de mancomunidad puede cambiarlo sin más, puede redirigir la recuperación de un profesor hacia una dirección que controle. Si el usuario puede cambiarlo sin confirmar la nueva dirección, un error de tecleo lo deja fuera y la cuenta anterior queda expuesta a que otra persona la registre.

## Decisión

- El [[Usuario]] incorpora `email_verificado` y `email_verificado_at`. Una cuenta recién creada nace con `email_verificado = false`.
- **Alta:** al crear una cuenta, el sistema envía un correo de verificación a la dirección indicada con un enlace de un solo uso, hasheado y válido **24 horas**. La cuenta puede iniciar sesión y operar tras el cambio obligatorio de contraseña, pero **la recuperación autoservicio permanece deshabilitada** hasta que el email esté verificado; la petición de recuperación sigue respondiendo `202` genérico para no revelar el estado.
- **Cambio por el propio usuario:** `PATCH /auth/email` exige la contraseña actual y la dirección nueva. No cambia el email de inmediato: crea una verificación pendiente y envía el enlace a la dirección nueva. Hasta que se confirme, el email anterior sigue siendo el de login y el de recuperación.
- **Cambio por soporte:** `PATCH /plataforma/usuarios/{id}/email` exige motivo de 10–500 caracteres, queda en `auditoria_plataforma` y aplica el mismo doble paso: deja la dirección nueva como pendiente y envía la verificación. El soporte **no confirma** el cambio por su cuenta ni puede saltarse el enlace.
- **El administrador de mancomunidad no puede cambiar el email** de un usuario de su tenant. Puede consultarlo y ver si está verificado, pero la dirección solo la cambia el titular o el soporte. Así se evita que un administrador redirija la recuperación de una cuenta.
- Solo puede haber **una verificación pendiente por usuario**. Pedir otra invalida la anterior. La dirección nueva debe ser única globalmente y no puede estar pendiente en otra cuenta; si ya existe, responde `409` con un mensaje que no revela datos de la otra cuenta.
- **Al confirmar** el enlace:
  - `email` pasa a la dirección nueva, `email_verificado = true` y se registra `email_verificado_at`;
  - se revocan **todas** las sesiones del usuario ([[ADR-031 - Límite de sesiones concurrentes y cierre global]]);
  - se escribe un evento `CAMBIO_EMAIL` en `evento_seguridad` y una entrada en la auditoría si lo inició el soporte;
  - se envía un aviso a la dirección anterior, si existe y está verificada, para que el titular detecte un cambio que no reconoce.
- **Al verificar el alta** solo se marca `email_verificado = true`; no se revocan sesiones ni se cambia la dirección.
- El enlace se construye con la URL pública configurada, nunca con la cabecera `Host`. El token en claro solo viaja en el correo; en base de datos queda su SHA-256.
- Reenviar la verificación está limitado a **3 peticiones por hora y usuario** y responde siempre `202`, exista o no una verificación pendiente ([[ADR-032 - Rate limiting de extremos sensibles]]).
- Si la cuenta no tiene email verificado, el administrador o el soporte pueden resetear la contraseña y entregarla fuera de banda; la recuperación autoservicio no se habilita hasta que el titular confirme la dirección.

## Consecuencias

### Positivas

- La recuperación de contraseña solo se apoya en una dirección que el titular ha demostrado controlar.
- Un error de tecleo se corrige con un flujo verificable, no con una escritura directa en la base de datos.
- Un administrador de tenant no puede desviar la recuperación de un usuario cambiando su email.
- El cambio de dirección deja rastro y avisa a la dirección anterior.

### Negativas / compromisos

- Hay una tabla y un flujo nuevos que mantener, con envío de correo y limpieza de tokens caducados.
- El alta no puede exigir verificación antes de operar sin bloquear al usuario si el correo falla; se acepta que la recuperación quede deshabilitada hasta confirmar.
- El administrador de mancomunidad depende del soporte para corregir un email mal escrito.
- El aviso a la dirección anterior puede fallar o rebotar; se registra para operación, pero no bloquea el cambio.
- La verificación no impide que alguien cree una cuenta con una dirección que controla temporalmente; reduce el error y el secuestro, no el abuso deliberado.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| No verificar y confiar en el email del alta | Un error de tecleo deja la cuenta sin recuperación y no hay forma de detectarlo. |
| Cambiar el email de inmediato al confirmarlo con la contraseña | Permite dejar la cuenta apuntando a una dirección no confirmada y romper la recuperación. |
| Permitir que el administrador de tenant cambie el email | Convierte al administrador en un vector para redirigir la recuperación de un usuario. |
| Bloquear el login hasta verificar el email | Un fallo de correo deja al usuario fuera y no aporta seguridad al acceso. |
| Enviar un código OTP en lugar de un enlace | Es válido, pero exige teclear el código y no mejora el flujo de verificación; el enlace ya está implementado para la recuperación. |
| Verificación por SMS | No hay proveedor ni teléfono verificado en la v1. |

## Relacionado

[[Usuario]] · [[Seguridad y autenticación]] · [[API REST]] · [[Modelo de datos]] · [[Requisitos funcionales]] · [[Roles y permisos]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-032 - Rate limiting de extremos sensibles]]
