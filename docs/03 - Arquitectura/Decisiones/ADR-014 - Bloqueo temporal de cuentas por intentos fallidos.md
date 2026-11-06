---
tipo: adr
estado: aceptada
fecha: 2026-10-29
actualizado: 2026-11-04
tags:
  - adr
  - seguridad
  - autenticacion
---

# ADR-014 — Bloqueo temporal de cuentas por intentos fallidos

## Contexto

El login con email y contraseña es la única puerta de entrada. Sin límite de intentos, un atacante puede probar contraseñas sin coste y, si además deduce qué emails existen, dirigir el ataque a cuentas concretas.

La documentación ya decía «limitar intentos y retardo progresivo», pero no concretaba el umbral, la duración, el alcance ni quién podía desbloquear. Tampoco estaba claro dónde se guardaban los contadores: en memoria se pierden al reiniciar y no se comparten entre instancias.

El proyecto no usa Redis en la v1. La solución debe funcionar con MySQL y varias instancias del backend.

## Decisión

- Se cuenta cada intento de login en una tabla **append-only** llamada `evento_acceso`, con email, usuario (si existe), IP, resultado y fecha. No se guardan contraseñas ni tokens.
- **Cuenta:** tras **5 fallos en 15 minutos** para el mismo email, la cuenta queda bloqueada **15 minutos**.
- **IP:** tras **20 fallos en 15 minutos** desde la misma IP, esa IP queda bloqueada **15 minutos**.
- Un login correcto **limpia los fallos de la cuenta**, pero no los de la IP. Un `DESBLOQUEO` explícito también limpia los fallos de la cuenta.
- El bloqueo **no revoca las sesiones abiertas**: solo impide nuevos inicios de sesión. Las sesiones existentes siguen funcionando hasta que caduquen o se cierren.
- Durante el bloqueo, el login responde **`429` con un mensaje genérico**, sin revelar si el email existe.
- El **`SUPERADMIN`** puede desbloquear una cuenta antes de que caduque, con motivo y auditoría en `auditoria_plataforma`. Un administrador de mancomunidad no tiene una acción de desbloqueo: al **resetear la contraseña** de un usuario de su tenant, el bloqueo también se limpia.
- El desbloqueo de IP no existe en la v1: caduca solo a los 15 minutos.
- Los contadores se calculan sobre `evento_acceso`; no hay estado en memoria. La tabla se purga a los **30 días**.

## Consecuencias

### Positivas

- El ataque por fuerza bruta se encarece sin añadir Redis ni estado compartido.
- Los contadores sobreviven a reinicios y funcionan con varias instancias.
- El umbral de IP más alto evita bloquear un centro entero detrás de una misma NAT por los fallos de unos pocos usuarios.
- El mensaje genérico no confirma qué emails están registrados.
- El desbloqueo queda auditado y el reset del administrador resuelve el caso más habitual.
- Las sesiones abiertas no se cortan por un bloqueo de login, que es una decisión menos agresiva.

### Negativas / compromisos

- Cada intento de login escribe una fila; hay que vigilar el volumen y la purga.
- Una IP compartida puede bloquear a usuarios legítimos si se acumulan 20 fallos en 15 minutos.
- El bloqueo no protege a un atacante que ya tenga una sesión abierta; para eso está la revocación de tokens.
- El administrador de tenant no puede desbloquear sin resetear la contraseña, lo que cambia la credencial del usuario.
- La ventana deslizante y el «último éxito» añaden consultas que deben estar bien indexadas.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Solo retardo progresivo, sin bloqueo | Ralentiza, pero no corta un ataque automatizado ni da una señal clara. |
| Bloqueo permanente hasta desbloqueo manual | Deja al usuario fuera y carga al soporte con cada error. |
| Contadores en memoria | Se pierden al reiniciar y no se comparten entre instancias. |
| Redis para los contadores | Añade infraestructura solo para el login; MySQL basta para este volumen. |
| Mismo umbral para cuenta e IP | Un centro con NAT compartido podría bloquearse por los fallos de pocos usuarios. |
| Revocar todas las sesiones al bloquear | Convierte un error de tecleo en un cierre de sesión innecesario; el bloqueo ya impide entrar de nuevo. |
| Devolver `423 Locked` | Distingue cuenta bloqueada de credenciales incorrectas y facilita la enumeración. |

## Relacionado

[[Seguridad y autenticación]] · [[Usuario]] · [[Roles y permisos]] · [[Requisitos funcionales]] · [[Requisitos no funcionales]] · [[Modelo de datos]] · [[API REST]] · [[ADR-003 - JWT con refresh token]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-032 - Rate limiting de extremos sensibles]]
