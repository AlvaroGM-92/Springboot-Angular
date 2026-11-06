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
- [x] Backend: `/actuator/health` responde `UP` sin base de datos.
- [ ] Backend: JPA, Security, Validation, Flyway y MySQL.
- [ ] Base de datos: `compose.yaml` con MySQL 8.4 y volumen persistente.
- [ ] Base de datos: migración inicial y seed de desarrollo.
- [ ] Backend: `TenantContext` y filtro base de aislamiento.
- [ ] Frontend: Angular 22 con proxy a `/api` y home con el health.
- [ ] Documentación: README de arranque en tres comandos.
- [ ] CI/CD: pipeline de build, tests, lint y Flyway.

## 4. Sub-fases

| Sub-fase | Contenido | Criterio de aceptación | Estado |
| --- | --- | --- | --- |
| 1.1 | Esqueleto de backend con Spring Initializr (Web, Actuator), perfiles `dev` y `test` y `/actuator/health`. | `./mvnw spring-boot:run` arranca y `/actuator/health` devuelve `UP` sin base de datos. | ✅ Completada |
| 1.2 | `compose.yaml` con MySQL 8.4, Flyway, esquema base y seed de mancomunidad de tipo `MUSICA`, curso `2026-2027` y administrador. | `docker compose up -d` levanta MySQL y Flyway aplica las migraciones sin errores. | ⬜ Pendiente |
| 1.3 | `TenantContext`, resolución de la mancomunidad activa y filtro base de aislamiento. | Con dos mancomunidades sembradas, una consulta sin tenant activo no devuelve datos. | ⬜ Pendiente |
| 1.4 | Angular 22 con proxy a `/api` (la API vive en `/api/v1`), home que consume el health y README de arranque. | `npm start` sirve la home, muestra el estado del backend y el README arranca en tres comandos. | ⬜ Pendiente |
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

## 9. Siguiente fase

Completar 1.2–1.5 y cerrar la Fase 1. Después, Fase 2 — Autenticación y seguridad base.

## Relacionado

[[Roadmap]] · [[Stack tecnológico]] · [[Arquitectura general]] · [[Diario de cambios]]
