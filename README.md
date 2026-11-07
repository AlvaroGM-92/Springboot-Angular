# Gestión de Mancomunidad · Spring Boot + Angular

Aplicación web para gestionar mancomunidades de centros de enseñanza de cualquier área —música, danza, artes escénicas, idiomas, deportes u otras—: centros, profesorado, asignaturas, alumnos, instrumentos cuando el área los usa, horarios, observaciones, asistencia y preparación de clases. Preparada para alojar varias mancomunidades con identidad global de usuario y aislamiento por `mancomunidad_id`.

> [!note] Estado
> **Fases 0 y 1 cerradas.** Backend Spring Boot 4.1.2 con MySQL 8.4, Flyway y aislamiento por tenant; frontend Angular 22; CI/CD con GitHub Actions y publicación de imágenes en GHCR. La siguiente fase es la 2: autenticación y seguridad base.

## Documentación

Toda la documentación está en el vault de Obsidian:

```text
docs/
├── 00 - Inicio/          Portada, guía y glosario
├── 01 - Proyecto/        Visión, roles, requisitos
├── 02 - Dominio/         Modelo y entidades
├── 03 - Arquitectura/    Stack, arquitectura, seguridad y ADRs
├── 04 - Fases/           Roadmap y notas de fase
├── 05 - Referencia/      Modelo de datos, API y convenciones
├── 06 - Diario/          Registro de sesiones
├── 99 - Adjuntos/        Imágenes y ficheros incrustados
└── 99 - Plantillas/      Patrones de documentación
```

Abre `docs/` como vault de Obsidian o empieza por [`docs/00 - Inicio/Portada.md`](docs/00%20-%20Inicio/Portada.md).

> [!tip] El vault es `docs/`, no la raíz
> Así Obsidian no indexa `backend/`, `frontend/` ni `node_modules`, y la configuración del vault (`.obsidian`) viaja junto a la documentación. Si abres la raíz del repositorio como vault, Obsidian creará una carpeta `.obsidian` que el `.gitignore` ya excluye.

## Stack decidido

| Capa | Tecnología |
| --- | --- |
| Backend | Java 21 + Spring Boot 4.1 + Maven |
| Frontend | Angular 22 + Angular Material + SCSS |
| Base de datos | MySQL 8.4 LTS + Flyway |
| Autenticación | JWT de acceso + refresh token rotatorio; identidad global y selector de mancomunidad; recuperación por enlace de un solo uso y cambio obligatorio tras una contraseña temporal |
| Multi-tenencia | Base compartida con `mancomunidad_id`; roles por mancomunidad y plataforma `SUPERADMIN` con soporte de cuentas; tipo de mancomunidad informativo y módulo de instrumentos opcional por asignatura |
| Correo | SMTP con Spring Mail; Mailpit en desarrollo y proveedor con SPF/DKIM en producción |
| Entorno | Docker Compose |

## Estructura prevista

```text
SpringbootAngular/
├── AGENTS.md           Instrucciones para agentes de código
├── docs/               Vault de Obsidian (abrir esta carpeta como vault)
├── .opencode/agents/   Subagentes de OpenCode
├── backend/            API Spring Boot (Fase 1)
├── frontend/           SPA Angular (Fase 1)
├── compose.yaml        MySQL 8.4 (Fase 1)
└── README.md
```

## Arranque en local

Necesitas Docker, JDK 21 y Node 26. El JDK del sistema es 27, así que hay que fijar `JAVA_HOME`.

**Tres comandos, en tres terminales:**

```bash
# 1. Base de datos y migraciones (MySQL 8.4 + Flyway)
docker compose up -d

# 2. Backend (http://localhost:8080)
cd backend
export JAVA_HOME=/home/alvarogm/.jdks/ms-21.0.12.1
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw spring-boot:run

# 3. Frontend (http://localhost:4200)
cd frontend
npm install        # solo la primera vez
npm start
```

Abre `http://localhost:4200`: la home muestra si el backend está disponible. El proxy de Angular reenvía `/api` y `/actuator` al backend, así que no hay CORS en desarrollo.

La configuración local se puede ajustar copiando `.env.example` a `.env`.

**Credenciales de desarrollo:** `admin@mancomunidad.local` / `Temporal1234`. La cuenta exige cambiar la contraseña al entrar. Solo se siembran con el perfil `dev`.

Mientras no existe autenticación, `dev` resuelve el tenant con la cabecera `X-Mancomunidad-Id` (IDs sembrados: `demo = 1`, `demo-b = 2`):

```bash
curl -H 'X-Mancomunidad-Id: 1' http://localhost:8080/api/v1/tenant/actual
```

## Integración y entrega continua

- `.github/workflows/ci.yml`: en cada `push` y PR a `main` ejecuta `./mvnw -B verify`, `npm run lint`, `npm run format:check`, los tests de ambos módulos, el build de Angular, las migraciones Flyway sobre MySQL efímero y la construcción de ambas imágenes Docker.
- `.github/workflows/cd.yml`: manual o con una etiqueta `v*`; publica las imágenes en GHCR (`mancomunidad-backend` y `mancomunidad-frontend`).
- El despliegue remoto se conectará cuando exista la infraestructura de staging/producción.

```bash
# Equivalente local del CI
cd backend && ./mvnw -B verify
cd ../frontend && npm ci && npm run lint && npm run format:check && npm test -- --watch=false && npm run build
```

## Siguiente paso

Al subir `main`, comprobar la primera ejecución del workflow **CI** en GitHub y activar la protección de rama con ese check como obligatorio. Después, **Fase 2 — Autenticación y seguridad base**.

Las decisiones y el progreso se registran en el [diario](docs/06%20-%20Diario/Diario%20de%20cambios.md).
