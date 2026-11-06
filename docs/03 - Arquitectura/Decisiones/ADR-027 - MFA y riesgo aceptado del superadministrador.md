---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - seguridad
  - autenticacion
  - plataforma
---

# ADR-027 — MFA y riesgo aceptado del superadministrador

## Contexto

El `SUPERADMIN` es la cuenta de mayor valor del sistema: puede crear y suspender mancomunidades y tocar las cuentas de profesorado y administración de cualquier tenant. La documentación ya le prohíbe leer datos académicos y impersonar usuarios, pero sigue bastando una contraseña para entrar en el panel de plataforma.

El proyecto no tiene todavía aplicación ni infraestructura de segundo factor. Añadir MFA en la v1 obliga a elegir TOTP o WebAuthn, diseñar el alta del segundo factor, los códigos de recuperación, el almacenamiento cifrado del secreto y las pruebas de enrolamiento y pérdida del dispositivo. Es un trabajo real que compite con el resto de la Fase 2 y la Fase 11.

## Decisión

- **La MFA no entra en la v1**, ni para el `SUPERADMIN` ni para el resto de usuarios. Se registra como **riesgo de seguridad aceptado**, no como un olvido.
- La aceptación tiene condiciones. Antes de producción debe existir una revisión explícita del riesgo y, si no se implementa MFA, una firma del responsable del producto asumiendo la limitación. La revisión se repite si aparece un incidente, si aumenta el número de superadministradores o si el panel se expone a Internet sin protección adicional.
- Mientras no haya MFA, el `SUPERADMIN` se protege con estos controles compensatorios:
  - **Sin credencial sembrada:** el primer superadministrador se crea por un comando de bootstrap y nunca por una migración con contraseña conocida ([[ADR-034 - Bootstrap del primer superadministrador]]).
  - **Contraseña reforzada:** mínimo 12 caracteres para cuentas con `admin_plataforma`, frente a los 8 del resto; se mantiene la comprobación de letra y número y la prohibición de reutilizar la actual.
  - **Sesiones acotadas:** máximo de sesiones concurrentes y cierre global ([[ADR-031 - Límite de sesiones concurrentes y cierre global]]).
  - **Refresh con detección de reutilización:** un token robado invalida toda la familia ([[ADR-030 - Detección de reutilización de refresh tokens]]).
  - **Límites de peticiones:** login, recuperación, refresh y acciones de soporte tienen umbrales propios ([[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]] y [[ADR-032 - Rate limiting de extremos sensibles]]).
  - **Email verificado:** la recuperación solo funciona sobre una dirección confirmada ([[ADR-028 - Verificación y cambio de email]]).
  - **Claves rotables:** el robo de una clave de firma se corta rotando y retirando la clave comprometida ([[ADR-029 - Claves JWT, rotación y secretos]]).
  - **Auditoría y alcance mínimo:** toda acción de soporte exige motivo y deja traza; el plano de plataforma no tiene rutas académicas ni impersonación ([[ADR-012 - Soporte de cuentas del superadministrador]]).
- Cuando se implemente, el segundo factor será **TOTP (RFC 6238)** obligatorio para `admin_plataforma`, con secreto cifrado en reposo, códigos de recuperación hasheados y enrolamiento obligatorio en la primera entrada. No se añaden columnas ni endpoints de MFA en la v1 para no documentar una funcionalidad que no existe.

## Consecuencias

### Positivas

- La v1 no se bloquea con un flujo de enrolamiento y recuperación de segundo factor que todavía no tiene interfaz.
- El riesgo queda escrito, con condiciones de aceptación y controles concretos, en lugar de quedar implícito.
- La futura MFA tiene una dirección clara y no obliga a rehacer el modelo de cuenta.

### Negativas / compromisos

- Una contraseña filtrada del `SUPERADMIN` sigue siendo suficiente para entrar. Es el riesgo principal aceptado.
- Los controles compensatorios no sustituyen a un segundo factor: reducen la probabilidad y el impacto, pero no eliminan el acceso con una única credencial.
- La contraseña de 12 caracteres solo se aplica a las cuentas de plataforma; el resto mantiene la política de 8.
- La revisión antes de producción es un hito manual y puede retrasar el despliegue si no se planifica.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| MFA TOTP solo para `SUPERADMIN` en la v1 | Es la opción más segura, pero añade enrolamiento, recuperación y pruebas que la fase no contempla; se pospone con revisión obligatoria. |
| WebAuthn/passkeys desde el inicio | Mayor seguridad y mejor experiencia, pero exige compatibilidad, registro de dispositivos y una UI que no existe. |
| MFA para todos los usuarios | Multiplica el soporte de recuperación de segundo factor y no es proporcional al riesgo de un profesor. |
| No documentar nada y confiar en la contraseña | Deja el riesgo invisible y sin criterio para aceptarlo o mitigarlo. |
| Prohibir el acceso remoto al panel | Reduce la superficie, pero no sustituye a la autenticación y complica la operación. |

## Relacionado

[[Seguridad y autenticación]] · [[Roles y permisos]] · [[Usuario]] · [[Requisitos no funcionales]] · [[Roadmap]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-014 - Bloqueo temporal de cuentas por intentos fallidos]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-034 - Bootstrap del primer superadministrador]]
