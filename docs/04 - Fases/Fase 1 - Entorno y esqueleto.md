---
tipo: fase
fase: 1
estado: en-curso
inicio: 2026-11-06
actualizado: 2026-11-06
tags:
  - fase/1
---

# Fase 1 — Entorno y esqueleto

> [!info] Objetivo
> Dejar el repositorio, el backend, el frontend y MySQL funcionando juntos, con un esqueleto desplegable y comprobable.

## 1. Alcance

Entra:

- Proyecto `backend/` con Spring Boot 4.1.2, Maven Wrapper, Java 21 y perfil `dev`.
- Proyecto `frontend/` con Angular 22, SCSS, routing y standalone.
- `compose.yaml` con MySQL 8.4 y volumen persistente.
- Flyway con el esquema base y los datos de desarrollo.
- `TenantContext` y filtro base de aislamiento.
- Home de Angular que consulta el health del backend.
- Pipeline de GitHub Actions.

No entra:

- Autenticación, usuarios y negocio (Fase 2 en adelante).
- Datos reales ni importación desde el sistema anterior.

## 2. Requisitos implicados

- [[Requisitos no funcionales|MNT-07]] — cada fase deja el proyecto compilando, con tests en verde y documentación actualizada.
- [[Requisitos no funcionales|MNT-10]] — las migraciones Flyway siembran una mancomunidad de desarrollo.
- [[Requisitos no funcionales|SEG-16]] — toda consulta de negocio se filtra por la mancomunidad activa.
- [[Requisitos no funcionales|DAT-01]] — el esquema se gestiona con Flyway y migraciones versionadas.

## 3. Tareas

- [x] Backend: esqueleto Spring Boot con Web y Actuator, perfiles `dev` y `test`.
- [x] Backend: `/actuator/health` responde `UP`.
- [x] Backend: JPA, Validation, Flyway y MySQL.
- [x] Base de datos: `compose.yaml` con MySQL 8.4, volumen persistente y servicio Flyway.
- [x] Base de datos: migración inicial y seed de desarrollo.
- [ ] Backend: Spring Security (Fase 2).
- [x] Backend: `TenantContext` y filtro base de aislamiento.
- [x] Frontend: Angular 22 con proxy a `/api` y home con el health.
- [x] Documentación: README de arranque en tres comandos.
- [ ] CI/CD: pipeline de build, tests, lint y Flyway.

## 4. Sub-fases

| Sub-fase | Contenido | Criterio de aceptación | Estado |
| --- | --- | --- | --- |
| 1.1 | Esqueleto de backend con Spring Initializr (Web, Actuator), perfiles `dev` y `test` y `/actuator/health`. | `./mvnw spring-boot:run` arranca y `/actuator/health` devuelve `UP` sin base de datos. | ✅ Completada |
| 1.2 | `compose.yaml` con MySQL 8.4, Flyway, esquema base y seed de mancomunidad de tipo `MUSICA`, curso `2026-2027` y administrador. | `docker compose up -d` levanta MySQL y Flyway aplica las migraciones sin errores. | ✅ Completada |
| 1.3 | `TenantContext`, resolución de la mancomunidad activa y filtro base de aislamiento. | Con dos mancomunidades sembradas, una consulta sin tenant activo no devuelve datos. | ✅ Completada |
| 1.4 | Angular 22 con proxy a `/api` (la API vive en `/api/v1`), home que consume el health y README de arranque. | `npm start` sirve la home, muestra el estado del backend y el README arranca en tres comandos. | ✅ Completada |
| 1.5 | Pipeline de GitHub Actions: build y tests de backend y frontend, lint, validación de migraciones Flyway y publicación de imágenes Docker. | Un PR a `main` ejecuta el pipeline en verde y bloquea el merge si falla. | ⬜ Pendiente |

## 5. Criterios de aceptación

- [ ] `docker compose up -d`, `./mvnw spring-boot:run` y `npm start` funcionan.
- [ ] La home carga y muestra el health del backend.
- [ ] `/actuator/health` devuelve `UP`.
- [ ] Existe una mancomunidad sembrada.
- [ ] El pipeline de CI está en verde.

## 6. Decisiones

| Decisión | ADR |
| --- | --- |
| Stack y versiones | [[ADR-001 - Stack tecnológico]] |
| MySQL 8.4 y Flyway | [[ADR-002 - Base de datos MySQL y Flyway]] |
| Multi-tenencia con `mancomunidad_id` | [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] |

## 7. Riesgos

| Riesgo | Mitigación |
| --- | --- |
| Spring Boot 4.1.2 usa starters nuevos (`spring-boot-starter-webmvc`) y rompe tutoriales de Boot 3. | Seguir el proyecto generado por Spring Initializr y fijar la versión `4.1.2` en el `pom.xml`. |
| El wrapper de Maven descarga la distribución en el primer arranque. | Documentar el primer arranque y no versionar la caché de Maven. |
| Angular 22 y Node 26 requieren versiones compatibles. | Node 26.12.1 instalado; usar `npx @angular/cli@22`. |

## 8. Resultado

### 1.1 — Esqueleto de backend (2026-11-06)

- Proyecto generado con Spring Initializr: `com.mancomunidad`, artifact `mancomunidad-backend`, Java 21, Spring Boot 4.1.2.
- Dependencias: `spring-boot-starter-webmvc` y `spring-boot-starter-actuator`; tests con `spring-boot-starter-webmvc-test` y `spring-boot-starter-actuator-test`.
- `application.yml` fija `dev` como perfil por defecto y expone solo el endpoint `health`.
- `application-dev.yml` activa el log `DEBUG` del paquete `com.mancomunidad`.
- `application-test.yml` silencia el banner y baja el log a `WARN`.
- Tests: contexto de aplicación y `GET /actuator/health` con `status = UP`.
- Verificado: `./mvnw test` en verde (2 tests) y `./mvnw spring-boot:run` arranca en el puerto 8080 con `{"status":"UP"}`.

### 1.2 — MySQL, Flyway y datos de desarrollo (2026-11-06)

- `compose.yaml` con `mysql:8.4`, volumen `mancomunidad-mysql-data`, healthcheck y servicio `flyway/flyway:11` que aplica las migraciones al arrancar.
- Variables de entorno con valores por defecto en `application.yml` (`APP_DB_URL`, `APP_DB_USER`, `APP_DB_PASSWORD`) y plantilla en `.env.example`.
- `V1__esquema_inicial.sql`: 31 tablas del modelo de datos, FKs simples, claves únicas por mancomunidad, `CHECK` de enumeraciones, `created_at`/`updated_at` y `version` en las tablas editables.
- `V2__datos_iniciales.sql` (solo perfiles `dev` y `test`, en `db/dev`): mancomunidad `demo` de tipo `MUSICA`, curso `2026-2027` en `ACTIVO` y administrador `admin@mancomunidad.local` con cambio de contraseña obligatorio.
- Flyway corre dos veces de forma segura: el servicio de Compose aplica las migraciones y el backend las valida al arrancar.
- Tests de integración con MySQL 8.4 en Testcontainers (`MigracionesTests`, `HealthEndpointTests` y contexto): **4 tests en verde**.
- Verificado: `docker compose up -d` deja MySQL en `healthy`, Flyway termina con `Exited (0)` y el esquema en la versión 2; `./mvnw spring-boot:run` responde `{"status":"UP"}` con la base de datos conectada.

### 1.3 — TenantContext y aislamiento base (2026-11-07)

- `com.mancomunidad.tenant` con `TenantContext` (contexto por hilo de la mancomunidad activa), `TenantResolver`, `HeaderTenantResolver`, `TenantFilter`, `MancomunidadActualService`, `MancomunidadActual`, `TenantController` y `TenantExceptionHandler`.
- Mecanismo provisional: la mancomunidad se indica con la cabecera `X-Mancomunidad-Id`. Solo se activa con `app.tenant.header-enabled=true`, que está en `dev` y `test`; en producción queda desactivado hasta que la Fase 2 lo sustituya por el JWT.
- La cabecera se valida contra la base de datos: la mancomunidad debe existir y estar activa. Un valor ausente, no numérico o inexistente produce `404` con Problem Details y `code = TENANT_NO_RESUELTO`.
- `GET /api/v1/tenant/actual` devuelve `id`, `nombre`, `codigo` y `tipo` de la mancomunidad activa; solo existe mientras el mecanismo por cabecera está activo.
- `TenantFilter` limpia el contexto antes y después de cada petición a `/api/`; ningún controlador acepta el `mancomunidadId` desde el cuerpo.
- `V3__segunda_mancomunidad_demo.sql` en `db/dev`: mancomunidad `demo-b` (`DANZA`) con su propio curso `2026-2027` activo, sin usuarios, para detectar cruces entre tenants.
- Tests: `TenantContextTests` (contexto y limpieza) y `TenantEndpointTests` (cabecera válida, dos tenants en peticiones seguidas, sin cabecera, inexistente y no numérica). **13 tests en verde**.
- Verificado: `docker compose up -d` deja el esquema en la versión 3 con `demo` y `demo-b`; `GET /api/v1/tenant/actual` con la cabecera correcta responde `200` y sin ella `404`.

### 1.4 — Frontend Angular y home con health (2026-11-07)

- Proyecto `frontend/` generado con Angular CLI 22.2.6: standalone, routing, SCSS, build con `@angular/build` y tests con Vitest.
- Rutas lazy: la home (`features/inicio`) se carga con `loadComponent`; `app.routes.ts` redirige cualquier ruta desconocida a `/`.
- `core/salud`: `SaludService` consulta `GET /actuator/health` y `EstadoSalud` modela la respuesta.
- Home accesible: estado `cargando`/`Disponible`/`No disponible` con `aria-live="polite"`, última comprobación y botón para repetir la consulta. `OnPush` y signals.
- `proxy.conf.json` reenvía `/api` y `/actuator` a `http://localhost:8080`; está configurado en `angular.json` para `npm start`, así que no hay CORS en desarrollo.
- `provideHttpClient()` en `app.config.ts`, `lang="es"` y título de la aplicación.
- Tests: servicio con `HttpTestingController` y home con respuesta `UP` y error `503`. **5 tests en verde**; `npm run build` genera el bundle de producción.
- Verificado con backend y frontend en marcha: `http://localhost:4200` sirve la home, `/actuator/health` responde `UP` a través del proxy y `/api/v1/tenant/actual` devuelve la mancomunidad de la cabecera.
- `README.md` raíz con el arranque en tres comandos y `frontend/README.md` con los comandos del proyecto.

## 9. Siguiente fase

Completar 1.5 y cerrar la Fase 1. Después, Fase 2 — Autenticación y seguridad base.

## Relacionado

[[Roadmap]] · [[Stack tecnológico]] · [[Arquitectura general]] · [[Diario de cambios]]
