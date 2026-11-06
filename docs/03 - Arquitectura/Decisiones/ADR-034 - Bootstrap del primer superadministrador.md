---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - seguridad
  - plataforma
  - operacion
---

# ADR-034 — Bootstrap del primer superadministrador

## Contexto

[[ADR-012 - Soporte de cuentas del superadministrador]] exigía que no se sembrara un superadministrador por una migración con contraseña conocida, pero no decía cómo se crea el primero: sin ningún `SUPERADMIN` no se puede entrar al panel de plataforma, y sin panel no se puede crear la primera mancomunidad ni su administrador.

Las opciones peligrosas son conocidas: un `INSERT` en Flyway con `admin@example.com` y una contraseña por defecto, un usuario sembrado en el `data.sql` o una cuenta que se crea sola al arrancar en producción. Cualquiera de ellas deja una credencial adivinable o versionada.

## Decisión

- El primer superadministrador se crea con un **comando de bootstrap explícito**, no con una migración ni con el arranque normal:
  - `java -jar app.jar --bootstrap-superadmin`, o `docker compose run --rm backend --bootstrap-superadmin` en el entorno de despliegue.
- El comando exige:
  - `APP_BOOTSTRAP_ENABLED=true` (por defecto `false`; en producción se activa solo durante la operación);
  - `BOOTSTRAP_SUPERADMIN_EMAIL` con una dirección válida;
  - la contraseña leída de `BOOTSTRAP_SUPERADMIN_PASSWORD_FILE` (fichero montado como secreto) o, si no existe, solicitada por consola. **Nunca** por argumento de línea de comandos ni por una variable de entorno visible en `ps` o `docker inspect`.
- La contraseña debe cumplir la política reforzada de plataforma: mínimo 12 caracteres, con letra y número, y no puede ser igual a la anterior (no hay anterior en el alta).
- El comando:
  1. comprueba si ya existe algún usuario con `admin_plataforma = true` y `activo = true`;
  2. si existe, no crea nada y termina con código `0`, indicando que ya hay un superadministrador;
  3. si no existe, crea el usuario con `admin_plataforma = true`, `email_verificado = true`, `debe_cambiar_password = true` y `password_temporal_expira_at = NULL` (es una contraseña inicial elegida por el operador, no una temporal con caducidad);
  4. escribe una fila `ALTA_SUPERADMIN` en `auditoria_plataforma` con el nuevo usuario como actor y el email como dato nuevo, sin guardar la contraseña;
  5. termina y no deja ningún proceso en segundo plano.
- La primera entrada exige cambiar la contraseña antes de operar, igual que una cuenta con contraseña temporal ([[ADR-011 - Recuperación de contraseña y correo transaccional]]).
- El comando es también el **break-glass** si se pierde el último superadministrador: si no queda ninguno activo, puede volver a crear uno. Nunca modifica ni reactiva una cuenta existente; para eso está el panel o la base de datos con un procedimiento excepcional.
- El email se marca verificado porque lo aporta el operador en el momento del bootstrap y el sistema aún puede no tener SMTP configurado. El operador es responsable de que la dirección sea correcta; si se equivoca, el soporte puede corregirla después con verificación ([[ADR-028 - Verificación y cambio de email]]).
- No hay endpoint HTTP de bootstrap. La primera mancomunidad y su primer administrador se crean desde el panel de plataforma una vez dentro ([[ADR-012 - Soporte de cuentas del superadministrador]]).
- Tras el éxito, el operador retira el secreto, pone `APP_BOOTSTRAP_ENABLED=false` y reinicia el servicio normal. El comando puede quedarse en la imagen, pero no se ejecuta solo.

## Consecuencias

### Positivas

- No hay ninguna contraseña sembrada, versionada ni conocida por defecto.
- El alta del primer superadministrador es un acto consciente y auditado.
- El mismo mecanismo sirve para recuperar el acceso si se pierde el último superadministrador.
- La contraseña no aparece en el historial del shell ni en la lista de procesos.

### Negativas / compromisos

- El despliegue tiene un paso manual más y un secreto que gestionar.
- Si el operador elige una contraseña débil, la política la rechaza, pero no impide una contraseña predecible que la cumpla.
- Marcar el email como verificado confía en el operador; un error de tecleo deja la recuperación apuntando a una dirección equivocada hasta que se corrija.
- El comando debe ejecutarse contra la base de datos correcta; un error de entorno crea el superadministrador en el sitio equivocado.
- Sin MFA, la cuenta sigue dependiendo de una sola credencial ([[ADR-027 - MFA y riesgo aceptado del superadministrador]]).

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Migración Flyway con usuario y contraseña por defecto | Credencial versionada y adivinable; imposible de rotar sin borrar datos. |
| `data.sql` o `CommandLineRunner` que crea la cuenta al arrancar | Se ejecuta en cada entorno y puede dejar una cuenta viva sin querer. |
| Variable de entorno con la contraseña | Visible en `ps`, `docker inspect` y volcados de configuración. |
| Enlace mágico al primer arranque | Requiere correo configurado antes de tener plataforma y añade una ruta pública. |
|Crear el superadministrador desde la primera mancomunidad | La mancomunidad no puede existir sin plataforma; invierte el orden. |
| Dejar la base de datos sin ningún superadministrador y crearlo a mano | Sin auditoría, sin validación y propenso a errores. |

## Relacionado

[[Seguridad y autenticación]] · [[Roles y permisos]] · [[Stack tecnológico]] · [[Roadmap]] · [[Modelo de datos]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]]
