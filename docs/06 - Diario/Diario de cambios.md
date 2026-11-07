---
tipo: referencia
estado: activo
fase: 0
actualizado: 2026-11-07
tags:
  - diario
  - seguimiento
---

# Diario de cambios

Registro cronológico de sesiones de trabajo. Cada entrada enlaza a una nota con el detalle. El diario no sustituye a los ADRs ni a las notas de fase: sirve para saber **qué se hizo y cuándo**.

## Entradas

| Fecha | Fase | Resumen | Nota |
| --- | --- | --- | --- |
| 2026-10-27 | 0 | Definición del proyecto, stack, revisión de decisiones (MySQL, horario con borrador/finalización y reapertura auditada, administración de solo lectura, aula libre, historial de asistencia con retención de 2 años, curso escolar como entidad, multi-tenant con identidad global) y creación del vault documental. | [[2026-10-27]] |
| 2026-10-28 | 0 | Repositorio local y `.gitignore`; vault movido a `docs/`; control horario diferido a Fases 12–13 con fichaje auditado y distribución como PWA. | [[2026-10-28]] |
| 2026-10-29 | 0 | Instrucciones para agentes (`AGENTS.md` en raíz y en `docs/`), subagente revisor de solo lectura, sub-fases en las diez fases largas, superusuario de cuentas, recuperación de contraseña por enlace de un solo uso, duración opcional de franjas, generación automática de sesiones desde el horario, bloqueo de cuentas por intentos fallidos, rejilla semanal por profesor y por grupo, código de mancomunidad inmutable, limpieza de dudas ya resueltas, cierre de las decisiones técnicas (FK simples, `version`/`409` e índices mínimos) y estrategia de copias y exportación. | [[2026-10-29]] |
| 2026-11-02 | 0 | Modelo abierto a cualquier área de enseñanza: tipo informativo de mancomunidad, tipo de asignatura y módulo de instrumentos opcional que solo se activa con asignaturas musicales instrumentales y se oculta sin borrar datos. Horario editable por el profesor titular y por el administrador mientras está en borrador, con autoría registrada y bloqueo tras finalizar. | [[2026-11-02]] |
| 2026-11-03 | 0 | Revisión crítica de la Fase 0: 48 huecos y decisiones aplazadas recogidos en [[Pendientes]], con identificadores `PEN-01`–`PEN-48`, tipo, momento, destino y estado. Resueltos los seis puntos de coherencia documental (`PEN-01` a `PEN-06`) y los siete de modelo (`PEN-07` a `PEN-13`): sesión cancelada, días no lectivos, curso en observaciones, resumen anonimizado de asistencia, borrado de franjas, conflicto de alumno y límite de conflictos de profesor entre tenants. | [[2026-11-03]] |
| 2026-11-04 | 0 | Tercer lote de modelo (`PEN-14` a `PEN-18`): copia de horario al curso siguiente, cierre y reapertura de curso, sustituciones, baja de alumno y carácter local del alumno. Nuevas entidades, tablas, endpoints, requisitos y autorizaciones. Cuarto lote de seguridad (`PEN-19` a `PEN-27`): MFA fuera de la v1, verificación y cambio de email, RS256 con `kid` y JWKS, familias de refresh con detección de reutilización, límite de sesiones, rate limiting, idempotencia y bootstrap del primer `SUPERADMIN`. | [[2026-11-04]] |
| 2026-11-06 | 1 | Fases 1.1 y 1.2: esqueleto del backend con Spring Boot 4.1.2, perfiles `dev`/`test` y health; MySQL 8.4 con Compose, Flyway con las 31 tablas del modelo, seed de la mancomunidad `demo`, su curso `2026-2027` y el administrador, y tests con Testcontainers. | [[2026-11-06]] |
| 2026-11-07 | 1 | Fase 1.3: `TenantContext` por petición, resolución provisional por cabecera `X-Mancomunidad-Id`, filtro de aislamiento, `GET /api/v1/tenant/actual`, `404` sin tenant, segunda mancomunidad `demo-b` y tests de dos tenants. | [[2026-11-07]] |

## Cómo añadir una entrada

1. Duplica [[Plantilla - Sesión de diario]].
2. Nómbrala con la fecha: `YYYY-MM-DD`.
3. Rellena contexto, hecho, decisiones, problemas y siguiente paso.
4. Añádela a la tabla anterior.
5. Enlaza la fase correspondiente.

## Reglas

- Una entrada por sesión de trabajo, no por commit.
- Las decisiones importantes se enlazan al ADR correspondiente.
- Los problemas resueltos se describen con la solución, no solo con el síntoma.
- Si una sesión no produjo cambios, no se registra.

## Relacionado

[[Portada]] · [[Roadmap]] · [[Cómo usar esta documentación]] · [[2026-10-27]] · [[2026-10-28]] · [[2026-10-29]] · [[2026-11-02]] · [[2026-11-03]] · [[2026-11-04]] · [[2026-11-06]]
