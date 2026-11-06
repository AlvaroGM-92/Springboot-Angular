---
tipo: referencia
estado: activo
fase: 0
actualizado: 2026-11-04
tags:
  - convenciones
  - referencia
---

# Convenciones

## 1. Idioma

| Ámbito | Idioma | Ejemplo |
| --- | --- | --- |
| Documentación | Español | Esta nota. |
| Interfaz de usuario | Español | «Mis asignaturas». |
| Dominio (clases, tablas, rutas) | Español | `Alumno`, `alumnos`, `/alumnos`. |
| Infraestructura técnica | Inglés | `AlumnoService`, `AlumnoController`, `AlumnoRequest`. |
| Commits | Tipo inglés, descripción español | `feat: alta de asignación de profesor`. |

> [!note] Decisión
> El dominio se nombra en español para que código, base de datos, API y documentación hablen el mismo idioma. Los sufijos y términos técnicos estándar (`Service`, `Controller`, `Repository`, `Request`, `Response`, `DTO`) se mantienen en inglés. Si se prefiere el dominio en inglés, es el momento de cambiarlo: antes de la Fase 1.

### Internacionalización (i18n)

- La v1 se entrega **solo en español**; no se traduce a otros idiomas.
- Los textos de la interfaz se externalizan en ficheros de recursos (Angular i18n) desde el principio, para no hardcodear cadenas en los componentes ni en las plantillas.
- Las claves de traducción se nombran por contexto (`auth.login.title`, `horario.franja.conflicto`), no por el texto.
- La API y los mensajes de error del backend se envían en español; no se negocia idioma por cabecera en la v1.
- Si en el futuro se añade un segundo idioma, se crea un fichero de traducción nuevo sin tocar el código.

## 2. Nomenclatura

### Backend

- Paquetes en minúsculas: `com.mancomunidad.horario`.
- Paquete transversal de tenant: `com.mancomunidad.tenant` (`TenantContext`, filtro y resolución).
- Clases en `PascalCase`; métodos y variables en `camelCase`.
- Entidades en singular y español: `Alumno`, `Asignatura`, `FranjaHoraria`.
- DTOs con sufijo: `AlumnoRequest`, `AlumnoResponse`, `AlumnoResumen`.
- Servicios: `AlumnoService`; repositorios: `AlumnoRepository`.
- Controllers: `AlumnoController`.
- Excepciones de dominio: `ReglaNegocioException`, `RecursoNoEncontradoException`.
- Booleanos con prefijo `es`/`hay`: `esActivo`, `haySolape`.

### Base de datos

- Tablas en singular y `snake_case`: `alumno`, `franja_horaria`.
- Columnas en `snake_case`: `nombre`, `hora_inicio`.
- FK: `<tabla>_id`: `alumno_id`.
- **Tenant:** toda tabla de negocio lleva `mancomunidad_id` y los índices y únicos lo incluyen cuando la regla es por mancomunidad.
- Índices: `idx_<tabla>_<columnas>`.
- Únicos: `uq_<tabla>_<columnas>`.
- Checks: `ck_<tabla>_<regla>`.
- Migraciones: `V1__esquema_inicial.sql`, `V2__datos_iniciales.sql`.

### Frontend

- Componentes: `alumno-list.component.ts`, `alumno-form.component.ts`.
- Servicios: `alumno.service.ts`.
- Modelos: `alumno.model.ts`.
- Guards: `auth.guard.ts`, `role.guard.ts`.
- Signals: `alumnos = signal<Alumno[]>([])`.
- Observables con `$`: `alumnos$`.

## 3. Git

- Rama principal: `main`.
- Ramas por fase o funcionalidad: `feature/fase-1-entorno`, `feature/alumno-instrumentos`.
- Commits con **Conventional Commits** (tipo en inglés, descripción en español):

```text
feat: alta del catálogo de instrumentos
fix: evitar franjas horarias solapadas
docs: documentar el guardado masivo de asistencia
test: cubrir la protección del último administrador
refactor: extraer el validador de conflictos de horario
chore: actualizar dependencias de Angular
```

- Un commit por cambio lógico; no mezclar refactor y funcionalidad.
- Al cerrar una fase: actualizar docs y hacer un commit `docs:` o `feat:` con el cierre.

## 4. Backend

- Inyección por constructor; sin campos `@Autowired`.
- DTOs como `record` cuando sean inmutables.
- Validación con Jakarta Validation en los DTOs.
- `@Transactional` en servicios que modifican varias tablas.
- No exponer entidades JPA en controllers.
- Fechas con `java.time` (`LocalDate`, `LocalTime`, `Instant`).
- Errores con excepciones de dominio y `@RestControllerAdvice`.
- Consultas complejas con JPQL o `Specification`, no SQL concatenado.
- **Toda consulta de negocio filtra por `TenantContext.mancomunidadId()`**; no se acepta el tenant desde el DTO.
- Los repositorios reciben el `mancomunidadId` como parámetro explícito o lo aplican mediante `Specification`.
- Tests de reglas de negocio sin contexto de Spring cuando sea posible.
- Tests de integración con **dos mancomunidades** para detectar fugas de datos.

## 5. Frontend

- Angular standalone; sin `NgModules` salvo necesidad.
- `ChangeDetectionStrategy.OnPush`.
- Formularios reactivos y tipados.
- Estado local con signals; servicios para HTTP.
- `HttpClient` con interceptor de token y refresh.
- Rutas lazy por funcionalidad.
- Componentes pequeños: contenedor (smart) y presentación (dumb).
- Sin lógica de negocio en plantillas.
- Accesibilidad: nivel **AA de WCAG 2.2** — foco visible, navegación por teclado, contraste mínimo 4.5:1, `aria-live` en estados dinámicos y soporte de `prefers-reduced-motion`. Se verifica con Lighthouse y una revisión manual por teclado antes de cada release.

## 6. API

- Rutas en plural y sustantivos: `/alumnos`, `/asignaturas`.
- Verbos HTTP con su significado; no `POST /alumnos/eliminar`.
- `camelCase` en JSON.
- Fechas ISO 8601.
- Errores RFC 7807.
- Paginación homogénea.
- Versionado en la ruta: `/api/v1`.
- Documentación OpenAPI generada desde el código.
- `Idempotency-Key` obligatoria en `POST /alumnos/{id}/matriculas`, `POST /horarios`, `POST /horarios/{id}/franjas`, `POST /horarios/{id}/finalizar`, `POST /horarios/{id}/copiar`, `POST /sesiones`, `POST /sesiones/{id}/abrir`, `POST /sustituciones`, `POST /cursos-escolares/{id}/cerrar`, `POST /cursos-escolares/{id}/reabrir`, `POST /plataforma/mancomunidades`, `POST /plataforma/usuarios/{id}/reset-password` y `POST /plataforma/usuarios/{id}/desbloquear`.
- La misma clave se reutiliza solo al reintentar la misma operación; un cuerpo o una ruta distintos responden `409` ([[ADR-033 - Idempotencia de peticiones sensibles]]).
- Los límites de peticiones responden `429` con `Retry-After`; no se usa `423` ni mensajes que revelen si una cuenta existe ([[ADR-032 - Rate limiting de extremos sensibles]]).

## 7. Pruebas

- Nombres descriptivos: `debeRechazarFranjasSolapadas`.
- Unitarios: reglas de negocio y validaciones.
- Integración: repositorios, seguridad y flujos completos.
- Datos de prueba aislados; nunca contra la base de datos de desarrollo.
- Testcontainers para MySQL en integración.
- Cada suite de integración con datos de negocio crea al menos **dos mancomunidades** y verifica que no se cruzan.
- Cobertura orientativa: 70 % en servicios y reglas críticas.

## 8. Documentación

- Frontmatter con `tipo`, `estado`, `fase`, `actualizado` y `tags`.
- Enlaces internos en lugar de duplicar.
- Callouts para avisos, decisiones y tareas.
- Mermaid para diagramas.
- Al terminar una sesión, actualizar [[Diario de cambios]].
- Al tomar una decisión relevante, crear un ADR.

## 9. Seguridad

- Nunca subir `.env`, contraseñas ni claves.
- Variables sensibles en `.env` y `.env.example` con valores ficticios; los secretos reales van en ficheros montados o gestor de secretos y se leen por ruta (`APP_JWT_PRIVATE_KEY_PATH`, `APP_JWT_PUBLIC_KEY_PATH`).
- No registrar tokens ni datos personales completos; los enlaces de recuperación y verificación no se escriben en logs.
- Revisar permisos en cada endpoint nuevo.
- Validar en backend aunque el frontend ya valide.
- Revisar que toda consulta nueva filtre por la mancomunidad activa.
- No aceptar `mancomunidadId` del cliente para decidir permisos; solo del token.
- Un recurso de otra mancomunidad se trata como inexistente.
- Los access tokens se firman con RS256 e incluyen `kid`; no se acepta `alg = none` ni HS256.
- Los refresh tokens viven en familias; una reutilización revoca la familia y se audita.
- Los `POST`, `PUT`, `PATCH` y `DELETE` sensibles llevan `Idempotency-Key` y respetan los límites de `429`.

## 10. Calidad de una entrega

- [ ] Compila sin errores ni warnings relevantes.
- [ ] Tests en verde.
- [ ] Sin `console.log` ni código comentado sin justificar.
- [ ] Sin `TODO` sin nota asociada.
- [ ] Documentación actualizada.
- [ ] Commit con mensaje claro.

## 11. Sub-fases

- Solo se dividen las fases con **varios entregables demostrables** o que duren más de dos semanas. Las fases cortas se quedan enteras.
- Numeración `N.M`: `6.1`, `6.2`. **Nunca se renumera**; si aparece trabajo nuevo, se añade al final con el siguiente número libre.
- **Dos niveles como máximo.** No existe `6.1.1`: si hace falta, la sub-fase está mal cortada.
- La tabla de sub-fases vive dentro de la nota de la fase con tres columnas: contenido y **criterio de aceptación de una línea**.
- Mientras la fase no tenga nota propia, la tabla vive en el [[Roadmap]].
- Se crea nota propia (`Fase N.M - Título.md`, a partir de [[Plantilla - Fase]]) solo cuando la sub-fase arranca y va a durar más de una sesión.
- Cerrar todas las sub-fases **no cierra la fase**: manda el criterio de aceptación de la fase completa.
- La vista general del [[Roadmap]] sigue con una fila por fase; las sub-fases solo aparecen en el detalle.

## Relacionado

[[Cómo usar esta documentación]] · [[Arquitectura general]] · [[Multi-tenencia]] · [[API REST]] · [[Modelo de datos]] · [[Roadmap]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-033 - Idempotencia de peticiones sensibles]]
