---
tipo: adr
estado: aceptada
fecha: 2026-10-27
actualizado: 2026-11-04
tags:
  - adr
  - multi-tenant
  - seguridad
---

# ADR-009 — Multi-tenant con mancomunidad e identidad global

## Contexto

El proyecto nace para una mancomunidad, pero se quiere dejar preparado para prestar servicio a varias sin rehacer el modelo. Se quiere evitar que cada mancomunidad obligue a crear una instalación y una base de datos independiente, y se quiere que un profesor que trabaje en varias pueda usar **una sola cuenta** y elegir con qué mancomunidad entra.

El requisito introduce un cambio de alcance: ya no es solo «exportar» la aplicación, sino **multi-tenencia real**. Hay que decidir dónde vive la identidad, cómo se aislan los datos y cómo se autoriza cada petición.

## Decisión

- Se usa **una única base de datos compartida** con `mancomunidad_id` en las tablas de negocio. No se crea una base de datos por mancomunidad en la v1.
- **`Mancomunidad` es el tenant**: la unidad de aislamiento, configuración y facturación futura.
- **`Usuario` es global**: una persona, un email y una contraseña. No tiene un rol global.
- **`UsuarioMancomunidad`** es la pertenencia: relaciona usuario y mancomunidad y guarda el rol (`PROFESOR` o `ADMINISTRADOR`) y el estado dentro de esa mancomunidad.
- Un usuario puede pertenecer a varias mancomunidades con **roles distintos** en cada una.
- Se añade el rol de plataforma **`SUPERADMIN`**, que puede crear, suspender y reactivar mancomunidades. No accede por defecto a los datos académicos de un tenant.
- El **inicio de sesión** autentica la identidad global y consulta las pertenencias activas:
  - 0 pertenencias → acceso denegado, salvo que la cuenta tenga `admin_plataforma`, que entra en el panel de plataforma con `scope = PLATFORM`.
  - 1 pertenencia → entra directamente.
  - Varias → pantalla de selección de mancomunidad.
- El **access token** incluye `usuarioId`, `mancomunidadId` y `rol` de la mancomunidad activa, además de un `scope` (`TENANT` o `PLATFORM`). El servidor nunca acepta el tenant desde el cuerpo o la URL para decidir permisos.
- Las **tablas de negocio** llevan `mancomunidad_id` y las claves únicas lo incluyen. Las referencias entre filas deben pertenecer a la misma mancomunidad.
- Cada mancomunidad tiene **su propio curso escolar activo** y su **propio catálogo de instrumentos**.
- El **panel de plataforma** permite crear una mancomunidad y sembrar su primer administrador y su curso inicial. Se implementa en una fase posterior; la primera mancomunidad y su administrador se crean desde el panel, y el primer superadministrador por comando de bootstrap ([[ADR-034 - Bootstrap del primer superadministrador]]).

> [!info] Ampliación posterior
> El alcance de `SUPERADMIN` se amplía al **plano de cuentas** (buscar usuarios globales, resetear contraseñas, activar o desactivar cuentas y pertenencias y cambiar roles, con auditoría), sin acceso a los datos académicos. Véase [[ADR-012 - Soporte de cuentas del superadministrador]].

## Consecuencias

### Positivas

- Un profesor con actividad en varias mancomunidades usa una sola cuenta.
- El alta de una mancomunidad es una operación de datos, no un despliegue completo.
- Hay una visión central para la plataforma y una experiencia separada por mancomunidad.
- Se evita migrar el modelo a multi-tenant más adelante.
- El aislamiento se puede probar y auditar de forma explícita.

### Negativas / compromisos

- **MySQL no tiene row-level security**: el aislamiento depende del código y de las pruebas.
- Todas las consultas, claves únicas y claves foráneas deben incluir la mancomunidad.
- El rol deja de ser global: hay que gestionar pertenencias y cambios de rol por tenant.
- El login y el refresh se complican: aparecen token de selección, cambio de mancomunidad y revocación por pertenencia.
- Un error de filtrado puede mezclar datos de dos mancomunidades. Es el riesgo principal del modelo.
- El panel de plataforma añade una superficie de administración nueva.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Una base de datos por mancomunidad | Aislamiento fuerte, pero exige una base de control para la identidad global y enrutar conexiones; más complejo para el tamaño previsto. |
| Un esquema por mancomunidad | Aislamiento alto, pero migraciones y conexiones por tenant; en MySQL es casi equivalente a una base por mancomunidad. |
| Rol global en `usuario` | Impide que una persona sea profesora en A y administradora en B, y mezcla permisos entre tenants. |
| Cuentas separadas por mancomunidad | Es la opción más simple, pero obliga al usuario a recordar varias cuentas y no cumple el requisito. |
| Mantener una sola mancomunidad | No prepara el crecimiento y obliga a rehacer el modelo después. |

## Relacionado

[[Multi-tenencia]] · [[Mancomunidad]] · [[UsuarioMancomunidad]] · [[Usuario]] · [[Seguridad y autenticación]] · [[Modelo de datos]] · [[Roles y permisos]] · [[Fase 0 - Definición y documentación]] · [[ADR-034 - Bootstrap del primer superadministrador]]
