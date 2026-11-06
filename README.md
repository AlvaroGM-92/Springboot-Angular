# Gestión de Mancomunidad · Spring Boot + Angular

Aplicación web para gestionar mancomunidades de centros de enseñanza de cualquier área —música, danza, artes escénicas, idiomas, deportes u otras—: centros, profesorado, asignaturas, alumnos, instrumentos cuando el área los usa, horarios, observaciones, asistencia y preparación de clases. Preparada para alojar varias mancomunidades con identidad global de usuario y aislamiento por `mancomunidad_id`.

> [!note] Estado
> **Fase 0 cerrada.** **Fase 1 en curso:** 1.1 y 1.2 completadas (backend Spring Boot 4.1.2, MySQL 8.4 con Compose, Flyway y datos de desarrollo). La siguiente sub-fase es 1.3: `TenantContext` y filtro de aislamiento.

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

Necesitas Docker y JDK 21 (el JDK del sistema es 27, así que hay que fijar `JAVA_HOME`).

```bash
# 1. Base de datos y migraciones (MySQL 8.4 + Flyway)
docker compose up -d
docker compose ps -a          # mancomunidad-flyway debe terminar en Exited (0)

# 2. Backend
cd backend
export JAVA_HOME=/home/alvarogm/.jdks/ms-21.0.12.1
export PATH="$JAVA_HOME/bin:$PATH"
./mvnw spring-boot:run
curl http://localhost:8080/actuator/health
```

La configuración local se puede ajustar copiando `.env.example` a `.env`.

**Credenciales de desarrollo:** `admin@mancomunidad.local` / `Temporal1234`. La cuenta exige cambiar la contraseña al entrar. Solo se siembran con el perfil `dev`.

## Siguiente paso

Completar la **Fase 1 — Entorno y esqueleto**: aislamiento por tenant (1.3), frontend Angular (1.4) y pipeline de CI (1.5).

Las decisiones y el progreso se registran en el [diario](docs/06%20-%20Diario/Diario%20de%20cambios.md).
