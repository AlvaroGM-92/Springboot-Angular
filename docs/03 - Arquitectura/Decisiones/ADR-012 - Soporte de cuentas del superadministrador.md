---
tipo: adr
estado: aceptada
fecha: 2026-10-29
actualizado: 2026-11-04
tags:
  - adr
  - seguridad
  - plataforma
  - multi-tenant
---

# ADR-012 — Soporte de cuentas del superadministrador

## Contexto

El rol `SUPERADMIN` se creó para el plano de plataforma: crear, suspender y reactivar mancomunidades, con una prohibición expresa de leer los datos académicos de cualquier tenant ([[ADR-009 - Multi-tenant con mancomunidad e identidad global]]).

En la práctica, el soporte necesita algo más: cuando un administrador pierde el acceso, cambia de email o hay que corregir una pertenencia, alguien tiene que poder **tocar las cuentas de profesor y de administrador de cualquier mancomunidad**. Sin esa capacidad, el soporte depende de acceder a la base de datos a mano, que es peor.

La tentación es convertir al superadministrador en un «dios» que ve y edita todo. Eso rompería dos decisiones ya tomadas: el administrador de mancomunidad es de solo lectura sobre la docencia y el panel de plataforma no expone datos académicos. Una sola cuenta comprometida abriría todos los tenants.

## Decisión

- El `SUPERADMIN` amplía su alcance al **plano de cuentas**, no al académico. Puede, en cualquier mancomunidad:
  - buscar usuarios globales por email o nombre;
  - consultar sus pertenencias, rol y estado;
  - resetear la contraseña generando una temporal con cambio obligatorio;
  - activar o desactivar la cuenta global y cada pertenencia;
  - cambiar el rol de una pertenencia entre `PROFESOR` y `ADMINISTRADOR`.
- **No puede** leer ni modificar alumnos, horarios, asistencias, observaciones, preparaciones ni matrículas. Tampoco puede impersonar a un usuario en la v1.
- No puede conceder ni retirar `admin_plataforma` desde una mancomunidad, ni dejar la plataforma sin ningún superadministrador activo.
- Las operaciones sensibles (reset, cambio de rol, activación o desactivación) exigen **motivo de 10–500 caracteres** y quedan en una auditoría de plataforma con autor, afectado, mancomunidad, acción y valores anterior y nuevo.
- Los endpoints viven bajo `/plataforma/usuarios/**`, exigen `scope = PLATFORM` y se separan de los endpoints de la mancomunidad. Un administrador de tenant no puede alcanzarlos aunque conozca la URL.
- El **primer** superadministrador se crea con el comando de bootstrap de [[ADR-034 - Bootstrap del primer superadministrador]]: sin contraseña sembrada, con auditoría y cambio obligatorio en la primera entrada. Los siguientes se crean desde el panel de plataforma.
- Si en el futuro hace falta ver datos académicos para resolver una incidencia, se hará con una **impersonación explícita, temporal y auditada**, no ampliando este permiso permanente.

## Aclaración 2026-11-04 — El reset no es impersonación

El reset de contraseña es la capacidad de soporte que más se acerca a entrar como otra persona: si el `SUPERADMIN` pudiera elegir o leer la contraseña temporal, podría iniciar sesión como el administrador afectado y, desde ahí, ver sus datos académicos. La decisión no cambia, pero se explicita el límite:

- La contraseña temporal la genera el servidor con un valor aleatorio; **el actor no la elige, no la recibe en la respuesta y no puede recuperarla después**. Se entrega al titular por el canal seguro que use la mancomunidad, nunca al actor para que la use él.
- Si el email del titular está verificado, el canal preferente es el correo a esa dirección; si no lo está, se usa el canal presencial o corporativo que llegue al titular.
- El reset exige motivo de 10–500 caracteres, revoca todas las sesiones, limpia el bloqueo, activa `debe_cambiar_password` y escribe en `auditoria_plataforma` con actor, afectado, mancomunidad y fecha. La contraseña y el token no se guardan.
- **No existe impersonación** en la v1: no hay un endpoint que abra una sesión «como» otro usuario ni un `scope` que mezcle plataforma y tenant. El plano de plataforma no tiene rutas de alumnos, horarios, asistencias, observaciones, preparaciones ni matrículas.
- El riesgo residual es de proceso: si el actor controla el canal de entrega, podría quedarse con la temporal. Por eso la entrega se hace al titular, la acción queda auditada y el uso indebido es detectable. Se revisa junto con el riesgo de MFA del `SUPERADMIN` ([[ADR-027 - MFA y riesgo aceptado del superadministrador]]).
- El primer superadministrador se crea por bootstrap, no por este reset ([[ADR-034 - Bootstrap del primer superadministrador]]).

## Consecuencias

### Positivas

- El soporte resuelve bloqueos de cuenta sin tocar la base de datos ni pedir credenciales.
- Se mantiene la separación entre el plano de cuentas y el plano académico.
- Cada intervención queda trazada y motivada, lo que permite responder ante una disputa.
- El administrador de mancomunidad sigue siendo la autoridad sobre su tenant para el día a día.

### Negativas / compromisos

- El `SUPERADMIN` se convierte en un objetivo de ataque de alto valor; exige contraseña fuerte y revisión de accesos.
- Cambiar un rol o desactivar una cuenta revoca los refresh tokens e incrementa `token_version`, así que el access token en curso deja de valer en menos de 60 segundos ([[ADR-029 - Claves JWT, rotación y secretos]]).
- La auditoría de plataforma es una tabla nueva que hay que consultar y conservar.
- El soporte puede corregir un email mal escrito y, con ello, redirigir una recuperación de contraseña; por eso la acción exige motivo y traza.
- Un superadministrador con pertenencia en una mancomunidad conserva, como miembro, los permisos de su rol; la separación aplica al plano de plataforma, no a su pertenencia.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Superadministrador con acceso total, incluidos datos académicos | Rompe el aislamiento y el diseño de solo lectura; una cuenta comprometida expone todos los tenants. |
| Rol separado `SOPORTE` | Viable, pero añade un tercer rol de plataforma para una capacidad que cabe en el mismo flag; se puede separar más adelante si crece el equipo. |
| Soporte solo con acceso a la base de datos | Sin auditoría, sin validaciones y con riesgo de dejar datos inconsistentes. |
| Impersonación desde el primer día | Útil para incidencias complejas, pero es una superficie mayor; se pospone y se hará auditada y con caducidad. |
| No permitir soporte sobre cuentas | Deja los bloqueos sin resolver y empuja a soluciones manuales peores. |

## Relacionado

[[Roles y permisos]] · [[Seguridad y autenticación]] · [[Multi-tenencia]] · [[Usuario]] · [[Requisitos funcionales]] · [[Requisitos no funcionales]] · [[API REST]] · [[Modelo de datos]] · [[Roadmap]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-034 - Bootstrap del primer superadministrador]]
