# AGENTS.md

> Instrucciones permanentes para cualquier agente que trabaje en este repositorio.
> Se cargan al abrir el proyecto. Las reglas específicas de la documentación están
> en [`docs/AGENTS.md`](docs/AGENTS.md).

## Qué es este proyecto

Aplicación de gestión de **mancomunidades de centros de enseñanza de cualquier
área** (música, danza, artes escénicas, idiomas, deportes u otras): centros,
profesorado, asignaturas, alumnos, instrumentos cuando el área los usa, horarios,
observaciones, asistencia y preparación de clases. Preparada para alojar varias
mancomunidades con identidad global de usuario y aislamiento por
`mancomunidad_id`.

Stack: Java 21 + Spring Boot 4.1, Angular 22 + Angular Material, MySQL 8.4 LTS,
Flyway, JWT con refresh token y Docker Compose.

## Estado actual

- **Fase 0 (definición y documentación) cerrada.**
- **Fase 1 (entorno y esqueleto) cerrada.** Backend Spring Boot, MySQL 8.4, Flyway, `TenantContext`, frontend Angular 22 y CI/CD integrados en `main`.
- **Fase 2 (autenticación y seguridad base) es la siguiente.** No adelantar pantallas ni módulos de negocio.
- `main` debe estar protegida con el check `CI` obligatorio. El pipeline lo ejecuta GitHub, no el entorno local.

## La documentación es la fuente de verdad

El vault de Obsidian vive en `docs/`. Antes de tocar nada, lee en este orden:

1. `docs/00 - Inicio/Portada.md` — mapa del proyecto y decisiones clave.
2. `docs/04 - Fases/Roadmap.md` — orden de construcción y criterios de aceptación.
3. `docs/05 - Referencia/Convenciones.md` — nombres, estilo y estructura.
4. `docs/AGENTS.md` — cómo se edita el vault.

Si el código y la documentación discrepan, manda la documentación hasta que se
decida lo contrario; entonces se actualizan las dos en el mismo cambio.

## Comandos

Base de datos (Docker debe estar en marcha):

```bash
docker compose up -d       # MySQL + Flyway (aplica V1 y el seed de dev)
docker compose ps -a       # flyway debe terminar en Exited (0)
docker compose down        # conserva el volumen
```

Backend (requiere JDK 21; el JDK del sistema es 27):

```bash
cd backend
export JAVA_HOME=/home/alvarogm/.jdks/ms-21.0.12.1
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw spring-boot:run     # http://localhost:8080/actuator/health
./mvnw test                # usa MySQL 8.4 en Testcontainers
./mvnw verify
```

Credenciales de desarrollo: `admin@mancomunidad.local` / `Temporal1234` (exige cambio de contraseña).

Tenant provisional en `dev` y `test` (se sustituye por el JWT en la Fase 2):

```bash
# IDs sembrados: demo = 1, demo-b = 2 (comprobar en la base si cambian)
curl -H 'X-Mancomunidad-Id: 1' http://localhost:8080/api/v1/tenant/actual
```

Frontend (Node 26; el backend debe estar en `localhost:8080`):

```bash
cd frontend
npm install                # solo la primera vez
npm start                  # http://localhost:4200, con proxy /api y /actuator
npm run lint
npm run format:check
npm test -- --watch=false  # Vitest
npm run build
```

Imágenes Docker (el backend arranca con perfil `prod`, sin seed de desarrollo):

```bash
docker build -t mancomunidad-backend:local backend
docker build -t mancomunidad-frontend:local frontend
```

CI/CD: `.github/workflows/ci.yml` se ejecuta en `push` y PR a `main`; `cd.yml` publica imágenes en GHCR con etiquetas `v*`. El despliegue remoto aún no está conectado.

**No inventes comandos ni crees proyectos que la fase no pida.**

### CI/CD (Fase 1, PEN-28)

El repositorio se integra y despliega con **GitHub Actions** (ver
[[Arquitectura general]] §11):

- `ci.yml` en cada `push` y `pull request` a `main`: `./mvnw verify`, `npm run lint`,
  `npm test`, `npm run build` y validación de migraciones Flyway.
- `cd.yml` por etiqueta `v*` o acción manual: construye y publica las imágenes Docker
  y despliega con Docker Compose, aplicando Flyway antes de arrancar y ejecutando
  el health check.
- `main` protegida: solo fusiona con PR y CI verde.
- Los secretos (SMTP, claves JWT, `APP_PURGE_SECRET`, clave de copia) viven en
  GitHub Secrets y en el gestor del servidor; nunca en el repositorio.

Comandos previstos (se confirmarán cuando exista el código):

- Backend: `./mvnw spring-boot:run`, `./mvnw test`, `./mvnw verify`.
- Frontend: `npm start`, `npm run lint`, `npm test`, `npm run build`.
- Migraciones: `./mvnw flyway:migrate` (o automáticamente al arrancar).
- Compose: `docker compose up -d`.

## Reglas de trabajo

- Una fase es una porción vertical y desplegable. No adelantes trabajo de fases futuras.
- Primero se documenta el qué y el porqué; después se implementa.
- Cada decisión relevante se registra como ADR en `docs/03 - Arquitectura/Decisiones/`.
- No dupliques información: enlaza la nota que ya la contiene.
- Al terminar una sesión con cambios, añade la entrada del día en `docs/06 - Diario/`.
- Idioma: dominio y documentación en español; sufijos técnicos en inglés.
- Toda consulta de negocio se filtra por la mancomunidad activa. Un recurso de
  otra mancomunidad responde `404`.
- Nada de secretos en el repositorio: `.env` está ignorado; usa `.env.example`.
- No reescribas historia de git, no hagas `push` ni crees repos remotos por tu cuenta.

## Qué no tocar

- `docs/.obsidian/`: configuración del vault. Se edita desde Obsidian, no a mano.
- `docs/.obsidian/workspace*.json` y `graph.json`: estado local, ignorados por git.
- `.gitignore`, `.gitattributes`: solo se cambian con motivo y explicándolo.

## Antes de dar algo por terminado

- Enlaces `[[...]]` resueltos y tablas Markdown bien formadas.
- Campo `actualizado` al día en cada nota tocada.
- `git status` limpio o solo con los cambios previstos.
- Si hay código: compila, tests en verde y migraciones Flyway válidas.

## Dónde mirar según la tarea

| Tarea | Carpeta |
| --- | --- |
| Alcance, roles y requisitos | `docs/01 - Proyecto/` |
| Entidades y reglas de dominio | `docs/02 - Dominio/` |
| Arquitectura, seguridad y ADRs | `docs/03 - Arquitectura/` |
| Fases, roadmap y criterios | `docs/04 - Fases/` |
| API, modelo de datos y convenciones | `docs/05 - Referencia/` |
| Historial de sesiones | `docs/06 - Diario/` |
