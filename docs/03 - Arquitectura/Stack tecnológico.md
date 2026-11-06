---
tipo: documento
estado: activo
fase: 0
actualizado: 2026-11-04
tags:
  - arquitectura
  - stack
---

# Stack tecnológico

## 1. Resumen

| Capa | Tecnología | Versión objetivo | Motivo |
| --- | --- | --- | --- |
| Lenguaje backend | Java | 21 LTS | LTS instalada, estable y soportada por Spring Boot 4.1. |
| Framework backend | Spring Boot | 4.1.2 | Última estable; Spring Framework 7, seguridad y validación integradas. |
| Build backend | Maven (wrapper) | 3.9+ | Estándar, reproducible y sin instalación global. |
| Persistencia | Spring Data JPA + Hibernate | Incluida en Boot | CRUD y consultas tipadas. |
| Base de datos | MySQL | 8.4 LTS | Elegida para la v1; buena integración con JPA y Flyway. |
| Migraciones | Flyway | Incluida en Boot | Esquema versionado y reproducible. |
| Seguridad | Spring Security + Nimbus JOSE | Incluida en Boot | JWT RS256 con `kid` y JWKS, roles y filtros. |
| Secretos | Gestor de secretos o ficheros montados | — | Claves JWT, `APP_PURGE_SECRET` y SMTP fuera del repositorio y de la imagen. |
| Rate limiting e idempotencia | Tablas MySQL (`limite_peticion`, `idempotencia`) | — | Funciona con varias instancias sin añadir Redis. |
| Correo transaccional | Spring Mail (Jakarta Mail) + SMTP | Incluida en Boot | Recuperación, verificación y cambio de email, y avisos operativos. |
| Pruebas de correo | Mailpit | Última | Bandeja local en desarrollo, sin enviar correo real. |
| MFA | Fuera de la v1; TOTP previsto | — | Riesgo aceptado con revisión antes de producción; no se añade dependencia ahora. |
| Documentación API | springdoc-openapi | Compatible con Boot 4.1 | Swagger UI y contrato OpenAPI. |
| Frontend | Angular | 22.2.x | Standalone, signals, router y formularios reactivos. |
| UI | Angular Material | 22.x | Componentes accesibles y consistentes. |
| Estilos | SCSS | — | Tematización y utilidades. |
| Runtime frontend | Node.js | 26.12.1 (instalado) | Requisito de Angular 22. |
| Contenedores | Docker + Compose | 29.x | MySQL local reproducible. |
| Tests backend | JUnit 5 + Mockito + Spring Boot Test + Testcontainers | Incluidas | Unitarios e integración real. |
| Tests frontend | Vitest o Jasmine (según CLI) | 22.x | Tests de componentes y servicios. |
| Control de versiones | Git | — | Conventional Commits. |
| Documentación | Obsidian + Markdown + Mermaid | — | Vault enlazado. |

## 2. Entorno local detectado

| Herramienta | Estado |
| --- | --- |
| Java | JDK 21.0.12.1 en `~/.jdks/ms-21.0.12.1`. |
| Maven | No instalado globalmente; se usará `mvnw`. |
| Node.js | 26.12.1. |
| npm | 12.2.1. |
| Angular CLI | No instalado globalmente; se usará `npx @angular/cli@22`. |
| Docker | 29.9.2. |
| MySQL | No instalado en el sistema; se usará contenedor. |
| PostgreSQL | Contenedor `mancomunidad-db` en el puerto 5432; **no se usará** según la decisión de la v1. No se elimina sin confirmación. |

## 3. Versiones y compatibilidad

- Spring Boot 4.1.2 requiere Java 17 o superior y es compatible hasta Java 27. Se elige **Java 21 LTS** por estabilidad y soporte.
- Angular 22 requiere Node `^22.22.3`, `^24.15.0` o `>=26.0.0`. Node 26.12.1 cumple.
- MySQL 8.4 es LTS; se fija la imagen `mysql:8.4` para evitar sorpresas.
- Las versiones exactas de dependencias se fijarán en el `pom.xml` y `package.json` de la Fase 1.

## 4. Decisiones asociadas

- [[ADR-001 - Stack tecnológico]]
- [[ADR-002 - Base de datos MySQL y Flyway]]
- [[ADR-003 - JWT con refresh token]]
- [[ADR-011 - Recuperación de contraseña y correo transaccional]]
- [[ADR-012 - Soporte de cuentas del superadministrador]]
- [[ADR-027 - MFA y riesgo aceptado del superadministrador]]
- [[ADR-028 - Verificación y cambio de email]]
- [[ADR-029 - Claves JWT, rotación y secretos]]
- [[ADR-030 - Detección de reutilización de refresh tokens]]
- [[ADR-031 - Límite de sesiones concurrentes y cierre global]]
- [[ADR-032 - Rate limiting de extremos sensibles]]
- [[ADR-033 - Idempotencia de peticiones sensibles]]
- [[ADR-034 - Bootstrap del primer superadministrador]]

## 5. Riesgos

| Riesgo | Mitigación |
| --- | --- |
| Spring Boot 4 es muy reciente y algún tutorial usa Boot 3. | Seguir la documentación oficial y fijar versiones; no copiar configuraciones antiguas. |
| Angular 22 introduce cambios respecto a versiones anteriores. | Usar la guía oficial y el CLI; no mezclar patrones de Angular antiguo. |
| MySQL no tiene índices parciales para solicitudes pendientes. | Validar la unicidad en el servicio dentro de una transacción. |
| El contenedor PostgreSQL existente ocupa el puerto 5432. | MySQL usará 3306; no se toca el contenedor antiguo. |
| No hay Maven ni Angular CLI globales. | Usar wrappers y `npx`. |
| La recuperación de contraseña depende de la entrega de correo. | Mailpit en desarrollo, proveedor con SPF/DKIM en producción, rebotes monitorizados y reset por administrador como plan B. |
| La clave privada JWT o los secretos podrían filtrarse. | Ficheros montados o gestor de secretos, rotación con `kid` y JWKS, y rotación de emergencia documentada. |
| El rate limiting y la idempotencia escriben en MySQL en cada petición sensible. | Índices por clave y ventana, purga programada y umbrales configurables; se mide en la Fase 10. |
| Sin MFA, una contraseña de plataforma filtrada da acceso total a las cuentas. | Contraseña de 12+, límite de sesiones, detección de reutilización, rate limiting, auditoría y revisión del riesgo antes de producción. |

## 6. Fuera del stack inicial

- Redis, Kafka, Elasticsearch o microservicios.
- NgRx o estado global complejo.
- GraphQL.
- Kubernetes.
- Aplicación móvil nativa.

## Relacionado

[[Arquitectura general]] · [[Seguridad y autenticación]] · [[Convenciones]] · [[Roadmap]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-033 - Idempotencia de peticiones sensibles]] · [[ADR-034 - Bootstrap del primer superadministrador]]
