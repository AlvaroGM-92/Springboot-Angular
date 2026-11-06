---
tipo: adr
estado: aceptada
fecha: 2026-10-29
actualizado: 2026-11-04
tags:
  - adr
  - seguridad
  - autenticacion
  - correo
---

# ADR-011 — Recuperación de contraseña con enlace de un solo uso

## Contexto

Un usuario que olvida su contraseña no puede entrar y, sin recuperación, depende de que un administrador o el soporte se la resetee a mano. Eso funciona, pero deja al usuario bloqueado fuera del horario de oficina y convierte cada olvido en una intervención manual.

La primera idea era generar una **contraseña aleatoria y enviarla por correo**, obligando a cambiarla al entrar. El mecanismo cumple el objetivo, pero tiene tres problemas:

- La contraseña viaja en claro y permanece en el buzón y en las copias de seguridad.
- Una cadena aleatoria se teclea mal en el móvil, así que se copia y se pega.
- Si el usuario no cambia la contraseña, la temporal sigue siendo válida hasta que alguien la revoque.

El correo es el único canal disponible: no hay SMS ni aplicación móvil. Además, el proyecto había dejado las **notificaciones por email** fuera de la v1. Esto no es una notificación: es un mecanismo de recuperación de acceso.

## Decisión

- La recuperación es **autoservicio desde la pantalla de login**. El usuario introduce su email y recibe un **enlace de un solo uso**.
- El enlace contiene un token **aleatorio, guardado hasheado (SHA-256), de un solo uso y con caducidad de 60 minutos**. Se invalida al usarse, al pedir uno nuevo o al cambiar la contraseña.
- La petición responde **siempre `202`** con el mismo mensaje, exista o no la cuenta, para no revelar qué emails están registrados. Se limita por IP y por cuenta.
- El usuario elige su propia contraseña, que cumple la política existente. Al guardarla se **revocan todos sus refresh tokens** y se registra el cambio.
- La **contraseña temporal** se reserva para cuando un administrador o el soporte resetea una cuenta. Se entrega fuera de banda (en persona o por el canal que use la mancomunidad), **caduca a las 24 horas** y obliga a cambiarla.
- El usuario tiene un flag `debe_cambiar_password`. Mientras esté activo, el login devuelve un estado limitado: solo permite cambiar la contraseña o cerrar sesión; no emite tokens de negocio.
- El correo transaccional se envía por **SMTP** con Spring Mail. En desarrollo se usa **Mailpit**; en producción, un proveedor con remitente verificado, SPF y DKIM. Las credenciales van por variables de entorno.
- Los enlaces se construyen con la URL pública configurada, nunca con la cabecera `Host` de la petición.

> [!info] Actualización 2026-11-04
> La recuperación autoservicio solo se habilita sobre un email verificado. El alta de la cuenta envía una verificación de 24 horas y el cambio de dirección exige confirmar la nueva antes de usarla ([[ADR-028 - Verificación y cambio de email]]). La solicitud de recuperación, el restablecimiento y el reenvío de verificación tienen límites propios ([[ADR-032 - Rate limiting de extremos sensibles]]).

## Consecuencias

### Positivas

- El usuario recupera el acceso sin intervención manual y sin recibir una contraseña en claro.
- El enlace caduca y solo sirve una vez, así que una copia del buzón no basta para entrar.
- El cambio obligatorio cubre también las contraseñas temporales que genera el soporte.
- La respuesta uniforme y el límite de peticiones reducen la enumeración de cuentas.
- Revocar las sesiones al cambiar la contraseña cierra la puerta a sesiones abiertas con la credencial anterior.

### Negativas / compromisos

- Aparece una dependencia nueva: **entrega de correo**. Si el proveedor falla, la recuperación no funciona; el reset por administrador sigue disponible como plan B.
- Un email mal escrito en el alta deja al usuario sin recuperación. El soporte debe poder corregirlo, con auditoría.
- El correo del usuario pasa a ser un dato crítico de seguridad, no solo de contacto.
- Hay que mantener plantillas, configuración SMTP y un entorno de pruebas de correo.
- No se elimina el riesgo de que el buzón del usuario esté comprometido; solo se acota su impacto con caducidad, un solo uso y revocación de sesiones.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Contraseña aleatoria por correo, como se planteó al principio | Credencial en claro en el buzón, difícil de teclear y válida hasta el cambio obligatorio. |
| Enlace de un solo uso (elegida) | No expone credenciales, caduca y se invalida al usarse. |
| Reset solo por administrador | Ya existe, pero deja al usuario bloqueado y carga el soporte con cada olvido. |
| Preguntas de seguridad | Datos adivinables, mantenimiento y mala experiencia; no aportan frente al correo. |
| Código OTP por SMS | Requiere proveedor de SMS, coste por mensaje y número de teléfono verificado; no hay infraestructura. |
| Sin correo, mostrando la contraseña al administrador | Imposible: las contraseñas se guardan con hash y no se pueden recuperar. |

## Relacionado

[[Seguridad y autenticación]] · [[Usuario]] · [[Roles y permisos]] · [[Requisitos funcionales]] · [[Requisitos no funcionales]] · [[API REST]] · [[Modelo de datos]] · [[Roadmap]] · [[ADR-003 - JWT con refresh token]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-032 - Rate limiting de extremos sensibles]]
