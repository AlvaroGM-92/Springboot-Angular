---
tipo: documento
estado: activo
fase: 0
actualizado: 2026-11-04
tags:
  - pendientes
  - backlog
  - definicion
---

# Pendientes

> [!info] Qué es esta nota
> Índice único de los huecos, contradicciones y decisiones aplazadas que quedaron al revisar la Fase 0. No es una nota de contenido: cada punto remite a la nota o al ADR donde se resuelve.

## 1. Cómo se usa

- **Un punto = un cambio cerrado.** Se resuelve en la nota de `Destino`, no aquí.
- **Estados:** `Pendiente`, `En curso`, `Hecho`, `Descartado`.
- **Tipos:** `Corregir` (error o contradicción), `Decidir` (falta una decisión y suele acabar en ADR), `Documentar` (la decisión existe pero no está escrita), `Ampliar` (alcance o funcionalidad nueva).
- `Cuándo` indica el momento en que hay que resolverlo para no bloquear; no obliga a implementarlo entonces.
- Al resolver un punto: cambiar el estado, añadir una fila al registro de avance, actualizar `actualizado` en las notas tocadas y anotarlo en el diario.
- Al descartar un punto: marcarlo `Descartado`, escribir el motivo y enlazar la decisión que lo deja fuera.
- Los identificadores no se renumeran. Un punto nuevo entra con el siguiente libre (`PEN-49`).

> [!warning] No duplicar
> Esta nota solo lleva el título, el detalle mínimo y el destino. La regla, el campo o el requisito se escribe en la nota de destino, nunca aquí.

## 2. Resumen

| Bloque | Puntos | Total |
| --- | --- | --- |
| A. Coherencia documental | `PEN-01`–`PEN-06` | 6 |
| B. Dominio y modelo de datos | `PEN-07`–`PEN-18` | 12 |
| C. Seguridad y cuentas | `PEN-19`–`PEN-27` | 9 |
| D. Operación y despliegue | `PEN-28`–`PEN-34` | 7 |
| E. Legal y privacidad | `PEN-35`–`PEN-40` | 6 |
| F. Producto, UX y documentación | `PEN-41`–`PEN-48` | 8 |

## 3. A. Coherencia documental

| ID | Punto | Tipo | Cuándo | Destino | Estado |
| --- | --- | --- | --- | --- | --- |
| `PEN-01` | **Recuento de sub-fases de la Fase 6.** La vista general decía 4 y el detalle tiene 6.1–6.5. Corregido a 5. | Corregir | Fase 0 | [[Roadmap]] | Hecho |
| `PEN-02` | **Prefijos duplicados.** `MAN-01..10` y `OBS-01..04` existían en requisitos funcionales y no funcionales. Renombrados los no funcionales: `MAN` → `MNT` y `OBS` → `OBSV`. | Corregir | Fase 0 | [[Requisitos no funcionales]] · [[Requisitos funcionales]] | Hecho |
| `PEN-03` | **Rutas `/api` y `/api/v1`.** [[Convenciones]] fija `/api/v1`; el roadmap y la seguridad hablaban del proxy `/api` y [[Roles y permisos]] filtraba por `/api/**`. Aclarado el prefijo del proxy y unificado el filtro en `/api/v1/**`. | Corregir | Fase 0 | [[API REST]] · [[Roles y permisos]] · [[Roadmap]] · [[Seguridad y autenticación]] | Hecho |
| `PEN-04` | **Definición de Administrador desactualizada.** El glosario decía que solo consulta la actividad docente; desde [[ADR-016 - Horario editable por profesor y administrador]] construye, finaliza y reabre horarios. Actualizado. | Corregir | Fase 0 | [[Glosario]] · [[Roles y permisos]] | Hecho |
| `PEN-05` | **`estado` del frontmatter sin criterio.** 22 notas seguían en `borrador` (18 entidades, [[Modelo de dominio]], [[Control horario]], [[Modelo de datos]] y [[API REST]]) mientras el resto se declaraba activo. Definidos los estados y actualizadas a `activo`. | Corregir | Fase 0 | [[Cómo usar esta documentación]] · notas afectadas | Hecho |
| `PEN-06` | **Ejemplos de la API.** La sección 13 de [[API REST]] dejaba sin escribir los ejemplos completos y los de aislamiento. Trasladados a [[Ejemplos de API]] y enlazados. | Documentar | Fase 0 | [[API REST]] · [[Ejemplos de API]] | Hecho |

## 4. B. Dominio y modelo de datos

| ID | Punto | Tipo | Cuándo | Destino | Estado |
| --- | --- | --- | --- | --- | --- |
| `PEN-07` | **Sesión cancelada o no impartida.** Resuelto con el estado `CANCELADA` desde `PROGRAMADA`, motivo de 10–500 caracteres, autor y fecha, sin lista de asistencia ni reapertura. | Decidir | Fase 0 | [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[Sesión de clase]] · [[Modelo de datos]] · [[Requisitos funcionales]] · [[API REST]] | Hecho |
| `PEN-08` | **Festivos y días no lectivos.** Resuelto con [[Día no lectivo]] por curso y centro opcional; el generador omite esas fechas y se cancelan las sesiones `PROGRAMADA` sin asistencia. | Decidir | Fase 0 | [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[Día no lectivo]] · [[Curso escolar]] · [[Sesión de clase]] · [[Roadmap]] | Hecho |
| `PEN-09` | **`Observación` sin curso escolar.** Resuelto con `curso_escolar_id` obligatorio y la fecha dentro del curso; la API puede inferirlo y responde `422` si no es único. | Decidir | Fase 0 | [[ADR-018 - Curso escolar en las observaciones]] · [[Observación]] · [[Modelo de datos]] · [[Requisitos funcionales]] · [[API REST]] | Hecho |
| `PEN-10` | **Agregados de asistencia para la purga.** Resuelto con `resumen_asistencia` anonimizado por `alumno_hash`, asignatura y curso, y con `purga_at`/`purgada_at` en la matrícula. | Decidir | Fase 0 | [[ADR-019 - Resumen de asistencia y purga anonimizada]] · [[Resumen de asistencia]] · [[Modelo de datos]] · [[Historial de asistencia]] · [[Roadmap]] | Hecho |
| `PEN-11` | **Franja borrada con sesión asociada.** Resuelto con `ON DELETE SET NULL`: se eliminan las sesiones `PROGRAMADA` sin asistencia y se conservan las que tienen lista con su instantánea. | Decidir | Fase 0 | [[ADR-020 - Borrado de franjas y conservación de sesiones]] · [[Franja horaria]] · [[Sesión de clase]] · [[Modelo de datos]] | Hecho |
| `PEN-12` | **Conflicto de alumno entre asignaturas.** Resuelto: dos franjas solapadas del mismo curso que comparten un alumno `ACTIVA` son inválidas aunque sean grupos distintos; se valida al crear o editar, al finalizar y al matricular. | Decidir | Fase 0 | [[ADR-021 - Validación de conflictos de alumno]] · [[Franja horaria]] · [[Matrícula]] · [[Requisitos funcionales]] | Hecho |
| `PEN-13` | **Conflicto de profesor entre mancomunidades.** Resuelto por escrito: no se valida en la v1 para no romper el aislamiento; solo se comprueba `HOR-04` dentro del tenant. | Decidir | Fase 0 | [[ADR-022 - Conflictos de profesor entre mancomunidades]] · [[Franja horaria]] · [[Multi-tenencia]] | Hecho |
| `PEN-14` | **Arrastre del horario al curso siguiente.** Resuelto con copia explícita a `BORRADOR`, previsualización, omisión de franjas no válidas y sin sesiones ni solicitudes. | Decidir | Fase 6 | [[ADR-023 - Copia de horario al curso siguiente]] · [[Horario del profesor]] · [[Roadmap]] | Hecho |
| `PEN-15` | **Cierre de curso escolar.** Resuelto: no se borra nada, se cancelan las sesiones `PROGRAMADA` sin lista, se congelan horarios y matrículas y la reapertura es administrativa y motivada. | Decidir | Fase 3 | [[ADR-024 - Cierre y reapertura de curso escolar]] · [[Curso escolar]] · [[Matrícula]] · [[Horario del profesor]] | Hecho |
| `PEN-16` | **Sustituciones y cambio de titular.** Resuelto con la entidad [[Sustitución]] por asignatura y periodo; no cambia el horario y reasigna las sesiones al sustituto. | Decidir | Fase 4 | [[ADR-025 - Sustituciones y cambio de titular]] · [[Sustitución]] · [[Asignatura]] · [[Sesión de clase]] | Hecho |
| `PEN-17` | **Baja de alumno a mitad de curso.** Resuelto: conserva asistencias e historial, excluye de listas futuras y la reactivación lo reincorpora. | Decidir | Fase 5 | [[ADR-026 - Baja de alumno a mitad de curso]] · [[Matrícula]] · [[Alumno]] · [[Sesión de clase]] · [[Asistencia]] | Hecho |
| `PEN-18` | **Identidad de alumno entre mancomunidades.** Resuelto por escrito: el alumno es local al tenant, sin identidad global ni deduplicación entre mancomunidades. | Documentar | Fase 0 | [[Alumno]] · [[Multi-tenencia]] · [[Visión y alcance]] | Hecho |

## 5. C. Seguridad y cuentas

| ID | Punto | Tipo | Cuándo | Destino | Estado |
| --- | --- | --- | --- | --- | --- |
| `PEN-19` | **MFA, al menos para `SUPERADMIN`.** Resuelto: queda fuera de la v1 para todas las cuentas; riesgo aceptado con controles compensatorios y revisión/firma antes de producción. | Decidir | Fase 2 | [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[Seguridad y autenticación]] · [[Roadmap]] | Hecho |
| `PEN-20` | **Verificación y cambio de email.** Resuelto: verificación de 24 horas al alta, cambio con doble confirmación y recuperación solo sobre email verificado; el administrador de tenant no puede cambiar emails. | Decidir | Fase 3 | [[ADR-028 - Verificación y cambio de email]] · [[Usuario]] · [[Seguridad y autenticación]] · [[API REST]] | Hecho |
| `PEN-21` | **Claves JWT y gestión de secretos.** Resuelto: RS256 con `kid`, JWKS, rotación cada 90 días con 24 horas de solapamiento, `token_version` y secretos en gestor o ficheros montados. | Decidir | Fase 2 | [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-003 - JWT con refresh token]] · [[Seguridad y autenticación]] · [[Stack tecnológico]] | Hecho |
| `PEN-22` | **Reutilización de refresh token.** Resuelto: se revoca la familia completa, se incrementa `token_version`, se audita `REUTILIZACION_REFRESH` y se responde `401`, sin periodo de gracia. | Decidir | Fase 2 | [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-003 - JWT con refresh token]] · [[Seguridad y autenticación]] | Hecho |
| `PEN-23` | **Reset del `SUPERADMIN` y acceso académico.** Aclarado: el reset no es impersonación; la temporal la genera el servidor, el actor no la elige ni la recibe, exige motivo, revoca sesiones y se audita. | Documentar | Fase 0 | [[ADR-012 - Soporte de cuentas del superadministrador]] · [[Seguridad y autenticación]] · [[Roles y permisos]] | Hecho |
| `PEN-24` | **Límite de sesiones concurrentes.** Resuelto: máximo 5 familias activas, lista de sesiones sin tokens y `logout-all` con la contraseña actual. | Decidir | Fase 2 | [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[Seguridad y autenticación]] | Hecho |
| `PEN-25` | **Rate limiting fuera del login.** Resuelto con la tabla `limite_peticion`: cubos para recuperación, verificación, refresh, escrituras y soporte, con `429` y `Retry-After`. | Decidir | Fase 2 | [[ADR-032 - Rate limiting de extremos sensibles]] · [[Seguridad y autenticación]] · [[Requisitos no funcionales]] | Hecho |
| `PEN-26` | **Idempotencia de `POST`.** Resuelto: `Idempotency-Key` obligatoria en altas y acciones sensibles, opcional en el resto, con tabla `idempotencia` y retención de 24 horas. | Decidir | Fase 3 | [[ADR-033 - Idempotencia de peticiones sensibles]] · [[API REST]] · [[Convenciones]] | Hecho |
| `PEN-27` | **Bootstrap del primer `SUPERADMIN`.** Resuelto con el comando `--bootstrap-superadmin`, secreto montado o consola, sin contraseña sembrada ni endpoint HTTP y con auditoría. | Decidir | Fase 11 | [[ADR-034 - Bootstrap del primer superadministrador]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[Seguridad y autenticación]] · [[Roadmap]] | Hecho |

## 6. D. Operación y despliegue

| ID | Punto | Tipo | Cuándo | Destino | Estado |
| --- | --- | --- | --- | --- | --- |
| `PEN-28` | **CI/CD.** Resuelto: pipeline de GitHub Actions con build y tests de backend y frontend, lint, validación de migraciones Flyway, publicación de imágenes y despliegue por etiqueta; `main` protegida. | Decidir | Fase 1 | [[Arquitectura general]] · [[Roadmap]] · `AGENTS.md` | Hecho |
| `PEN-29` | **Gestión de la clave de copias.** Resuelto: AES-256-GCM con cifrado envolvente, KEK en gestor o fichero montado, doble control, rotación anual re-envolviendo DEKs, copia offline y prueba trimestral de descifrado. | Decidir | Preproducción | [[Copias de seguridad y exportación]] · [[ADR-035 - Gestión de la clave de cifrado de copias]] | Hecho |
| `PEN-30` | **Entornos.** Resuelto: staging permanente con datos semilla y anonimizados, despliegue automático desde `main`, secretos y KEK propios; acceso solo al equipo. | Documentar | Fase 1 | [[Arquitectura general]] | Hecho |
| `PEN-31` | **Presupuesto de rendimiento.** Resuelto: 50 usuarios concurrentes por mancomunidad, p95 < 500 ms lectura y < 1 s escritura, página 20/100, hasta 5.000 alumnos y 500 profesores, bundle inicial < 500 kB. | Decidir | Fase 1 | [[Requisitos no funcionales]] · [[Arquitectura general]] | Hecho |
| `PEN-32` | **Migración de datos existentes.** Resuelto: no hay importación en la v1; si un centro llega con datos se crea vacío y se carga con el script de operación filtrado por `mancomunidad_id`, sin hashes ni tokens. | Decidir | Fase 1 | [[Visión y alcance]] · [[Roadmap]] | Hecho |
| `PEN-33` | **Notificaciones de solicitudes.** Resuelto: aviso interno (badge/contador) en el panel del profesor; sin correo ni push en la v1, conforme al alcance. | Decidir | Fase 6 | [[Requisitos funcionales]] · [[API REST]] · [[Roadmap]] | Hecho |
| `PEN-34` | **Retención de logs y auditoría.** Resuelto: logs 30 días, `evento_acceso` 30 días, `auditoria_plataforma` 2 años, cuentas desactivadas no se borran (solo sesiones/tokens) y `evento_seguridad` 1 año. | Decidir | Fase 10 | [[Modelo de datos]] · [[Seguridad y autenticación]] · [[Requisitos no funcionales]] | Hecho |

## 7. E. Legal y privacidad

| ID | Punto | Tipo | Cuándo | Destino | Estado |
| --- | --- | --- | --- | --- | --- |
| `PEN-35` | **Base jurídica y registro de tratamiento.** Resuelto: creada [[Privacidad y tratamiento de datos]] con finalidad, base jurídica, registro de actividades, conservación, derechos, menores y EIPD; LEG-05 añadido. | Documentar | Preproducción | [[Requisitos no funcionales]] · [[Privacidad y tratamiento de datos]] | Hecho |
| `PEN-36` | **Datos de menores y tutores.** Resuelto: fecha de nacimiento y datos del tutor documentados en [[Alumno]], consentimiento e información a las familias fuera de la aplicación en la v1 y derechos ejercibles por los tutores. | Decidir | Preproducción | [[Requisitos no funcionales]] · [[Alumno]] | Hecho |
| `PEN-37` | **Derechos del interesado.** Resuelto: procedimiento y plazos en [[Privacidad y tratamiento de datos]] §7 y [[Copias de seguridad y exportación]] §9; API documenta que no hay endpoints dedicados en la v1. | Decidir | Preproducción | [[Copias de seguridad y exportación]] · [[API REST]] | Hecho |
| `PEN-38` | **Contrato de encargado del tratamiento.** Resuelto: añadida la sección 11 a [[Privacidad y tratamiento de datos]] con el reparto de responsabilidades entre plataforma y mancomunidad, subencargados y revisión antes de producción. | Documentar | Preproducción | [[Privacidad y tratamiento de datos]] | Hecho |
| `PEN-39` | **Aviso legal, privacidad y condiciones de uso.** Resuelto: sección 11 de [[Visión y alcance]] con las páginas públicas (aviso legal, política de privacidad y condiciones de uso) y su contenido mínimo. | Documentar | Preproducción | [[Visión y alcance]] · frontend | Hecho |
| `PEN-40` | **Supresión frente a retención.** Resuelto: regla en [[Historial de asistencia]] y LEG-09; no se borra lo sujeto a retención, se informa del motivo y plazo, se anonimiza lo posible y se registra la decisión. | Decidir | Preproducción | [[Historial de asistencia]] · [[Requisitos no funcionales]] | Hecho |

## 8. F. Producto, UX y documentación

| ID | Punto | Tipo | Cuándo | Destino | Estado |
| --- | --- | --- | --- | --- | --- |
| `PEN-41` | **Mapas de pantalla y wireframes.** Resuelto: creada [[Mapa de pantallas]] con el inventario por rol y rutas orientativas. | Documentar | Fase 1 | [[Mapa de pantallas]] · [[Roadmap]] | Hecho |
| `PEN-42` | **Matriz de trazabilidad.** Resuelto: creada [[Trazabilidad]] con la estructura y filas de ejemplo; se mantiene al cerrar cada fase. | Documentar | Fase 0 | [[Trazabilidad]] · [[Requisitos funcionales]] · [[API REST]] | Hecho |
| `PEN-43` | **Catálogo de enumeraciones.** Resuelto: creado [[Enumeraciones]] con los valores cerrados del dominio y del modelo físico. | Documentar | Fase 0 | [[Enumeraciones]] · [[Modelo de datos]] | Hecho |
| `PEN-44` | **Manual de usuario y ayuda.** Resuelto: creado [[Manual de usuario]] con el esqueleto por rol y las preguntas frecuentes. | Documentar | Fase 10 | [[Manual de usuario]] · [[Roadmap]] | Hecho |
| `PEN-45` | **Objetivo de accesibilidad.** Resuelto: nivel AA de WCAG 2.2 fijado en USA-03 y USA-08 y en [[Convenciones]]. | Decidir | Fase 0 | [[Requisitos no funcionales]] · [[Convenciones]] | Hecho |
| `PEN-46` | **Estrategia de i18n.** Resuelto: v1 solo en español, con textos externalizados en ficheros de recursos desde el principio. | Decidir | Fase 1 | [[Convenciones]] · [[Arquitectura general]] | Hecho |
| `PEN-47` | **Definition of Done por fase.** Resuelto: criterios mínimos de tests, cobertura, seguridad, documentación, accesibilidad y despliegue añadidos a [[Cómo usar esta documentación]]. | Documentar | Fase 0 | [[Cómo usar esta documentación]] · [[Convenciones]] | Hecho |
| `PEN-48` | **Plan de pruebas manuales y datos de demo.** Resuelto: creado [[Plan de pruebas manuales y datos de demo]] con seed de dos mancomunidades y casos mínimos. | Documentar | Fase 1 | [[Plan de pruebas manuales y datos de demo]] · [[Roadmap]] · [[Copias de seguridad y exportación]] | Hecho |

## 9. Orden sugerido

1. **Coherencia, sin decisiones (PEN-01 a PEN-06, PEN-42, PEN-43, PEN-45 y PEN-47).** Hecho el 2026-11-03 para los seis primeros y el 2026-11-04 para PEN-42, PEN-43, PEN-45 y PEN-47.
2. **Modelo (PEN-07 a PEN-18).** Son las que más condicionan el esquema y la Fase 6; cada decisión puede ser un ADR corto. Hecho el 2026-11-04 con los ADR-017 a ADR-026.
3. **Cuentas y arranque (PEN-21, PEN-22, PEN-25, PEN-19, PEN-20 y PEN-27).** Antes de la Fase 2 y del panel de plataforma. Hecho el 2026-11-04 con los ADR-027 a ADR-034, junto con PEN-23, PEN-24 y PEN-26.
4. **El resto, en la fase que indica `Cuándo`.**

## 10. Registro de avance

| Fecha | Punto | Qué se hizo | Nota |
| --- | --- | --- | --- |
| 2026-11-03 | — | Creada la lista con los 48 puntos detectados en la revisión. | [[2026-11-03]] |
| 2026-11-03 | `PEN-01` | Fase 6 pasa a 5 sub-fases en la vista general. | [[Roadmap]] |
| 2026-11-03 | `PEN-02` | Renombrados los prefijos no funcionales `MAN` → `MNT` y `OBS` → `OBSV`. | [[Requisitos no funcionales]] |
| 2026-11-03 | `PEN-03` | Aclarado el proxy `/api` y unificado el filtro en `/api/v1/**`. | [[API REST]] · [[Roles y permisos]] |
| 2026-11-03 | `PEN-04` | Actualizada la definición de Administrador en el glosario. | [[Glosario]] |
| 2026-11-03 | `PEN-05` | Definidos los estados del frontmatter y actualizadas 22 notas a `activo`. | [[Cómo usar esta documentación]] |
| 2026-11-03 | `PEN-06` | Creados los ejemplos por módulo, de aislamiento y de conflicto de versión. | [[Ejemplos de API]] |
| 2026-11-03 | `PEN-07` | Añadido el estado `CANCELADA` con motivo, autor y sin lista. | [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[Sesión de clase]] |
| 2026-11-03 | `PEN-08` | Creada la entidad [[Día no lectivo]] e integrada en el generador y en la API. | [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[Día no lectivo]] |
| 2026-11-03 | `PEN-09` | Añadido `curso_escolar_id` obligatorio a la observación, inferible por fecha. | [[ADR-018 - Curso escolar en las observaciones]] · [[Observación]] |
| 2026-11-03 | `PEN-10` | Creada [[Resumen de asistencia]] anonimizada y fechas de purga en la matrícula. | [[ADR-019 - Resumen de asistencia y purga anonimizada]] · [[Resumen de asistencia]] |
| 2026-11-03 | `PEN-11` | Definido `ON DELETE SET NULL` y la conservación de sesiones con lista. | [[ADR-020 - Borrado de franjas y conservación de sesiones]] · [[Sesión de clase]] |
| 2026-11-03 | `PEN-12` | Añadida la validación de conflicto de alumno entre grupos distintos. | [[ADR-021 - Validación de conflictos de alumno]] · [[Franja horaria]] |
| 2026-11-03 | `PEN-13` | Documentada la no validación de conflictos de profesor entre mancomunidades. | [[ADR-022 - Conflictos de profesor entre mancomunidades]] · [[Multi-tenencia]] |
| 2026-11-04 | `PEN-14` | Creada la copia explícita de horario al curso siguiente con previsualización y omisión de franjas no válidas. | [[ADR-023 - Copia de horario al curso siguiente]] · [[Horario del profesor]] |
| 2026-11-04 | `PEN-15` | Definido el cierre con preflight y la reapertura administrativa sin borrado de datos. | [[ADR-024 - Cierre y reapertura de curso escolar]] · [[Curso escolar]] |
| 2026-11-04 | `PEN-16` | Creada la entidad [[Sustitución]] y el flujo de reasignación de sesiones al sustituto. | [[ADR-025 - Sustituciones y cambio de titular]] · [[Sustitución]] |
| 2026-11-04 | `PEN-17` | Definida la baja de matrícula sin borrado y su efecto en listas futuras y reactivación. | [[ADR-026 - Baja de alumno a mitad de curso]] · [[Matrícula]] |
| 2026-11-04 | `PEN-18` | Documentado que el alumno es local al tenant y no tiene identidad global. | [[Alumno]] · [[Multi-tenencia]] |
| 2026-11-04 | `PEN-19` | MFA fuera de la v1 con riesgo aceptado, controles compensatorios y revisión antes de producción. | [[ADR-027 - MFA y riesgo aceptado del superadministrador]] |
| 2026-11-04 | `PEN-20` | Verificación de email al alta, cambio con doble confirmación y recuperación solo con dirección verificada. | [[ADR-028 - Verificación y cambio de email]] · [[Usuario]] |
| 2026-11-04 | `PEN-21` | Firma RS256 con `kid`, JWKS, rotación de 90 días y secretos fuera del repositorio. | [[ADR-029 - Claves JWT, rotación y secretos]] |
| 2026-11-04 | `PEN-22` | Detección de reutilización de refresh con revocación de la familia y auditoría. | [[ADR-030 - Detección de reutilización de refresh tokens]] |
| 2026-11-04 | `PEN-23` | Aclarado que el reset no es impersonación y que la temporal la genera el servidor. | [[ADR-012 - Soporte de cuentas del superadministrador]] |
| 2026-11-04 | `PEN-24` | Límite de 5 familias, lista de sesiones y cierre global con contraseña. | [[ADR-031 - Límite de sesiones concurrentes y cierre global]] |
| 2026-11-04 | `PEN-25` | Rate limiting en MySQL para recuperación, verificación, refresh, escrituras y soporte. | [[ADR-032 - Rate limiting de extremos sensibles]] |
| 2026-11-04 | `PEN-26` | Idempotencia con `Idempotency-Key` y tabla `idempotencia` de 24 horas. | [[ADR-033 - Idempotencia de peticiones sensibles]] |
| 2026-11-04 | `PEN-27` | Bootstrap del primer `SUPERADMIN` por comando auditado, sin contraseña sembrada. | [[ADR-034 - Bootstrap del primer superadministrador]] |
| 2026-11-04 | `PEN-28` | Pipeline de GitHub Actions: build, tests, lint, validación de migraciones y despliegue por etiqueta; `main` protegida. | [[Arquitectura general]] · [[Roadmap]] · `AGENTS.md` |
| 2026-11-04 | `PEN-29` | Gestión de la clave de copias: AES-256-GCM envolvente, KEK en gestor o fichero montado, doble control, rotación anual y prueba de descifrado. | [[Copias de seguridad y exportación]] · [[ADR-035 - Gestión de la clave de cifrado de copias]] |
| 2026-11-04 | `PEN-30` | Entornos: staging permanente con datos semilla y anonimizados, despliegue automático y secretos propios. | [[Arquitectura general]] |
| 2026-11-04 | `PEN-31` | Presupuesto de rendimiento: usuarios concurrentes, latencia p95, tamaño de página y volumen por mancomunidad. | [[Requisitos no funcionales]] · [[Arquitectura general]] |
| 2026-11-04 | `PEN-32` | Sin importación en la v1; carga de datos existentes con el script de operación filtrado por `mancomunidad_id`. | [[Visión y alcance]] · [[Roadmap]] |
| 2026-11-04 | `PEN-33` | Aviso interno para solicitudes resueltas; sin correo ni push en la v1. | [[Requisitos funcionales]] · [[API REST]] · [[Roadmap]] |
| 2026-11-04 | `PEN-34` | Retención: logs 30 días, `evento_acceso` 30 días, `auditoria_plataforma` 2 años, cuentas desactivadas sin borrado físico. | [[Modelo de datos]] · [[Seguridad y autenticación]] · [[Requisitos no funcionales]] |
| 2026-11-04 | `PEN-35` | Creada la nota de privacidad con finalidad, base jurídica, registro de actividades, conservación, derechos, menores y EIPD. | [[Privacidad y tratamiento de datos]] · [[Requisitos no funcionales]] |
| 2026-11-04 | `PEN-36` | Datos de menores y tutores documentados en [[Alumno]] y LEG-06; consentimiento e información a las familias fuera de la aplicación. | [[Alumno]] · [[Requisitos no funcionales]] · [[Privacidad y tratamiento de datos]] |
| 2026-11-04 | `PEN-37` | Derechos del interesado: procedimiento y plazos en la nota de privacidad y en copias; sin endpoints dedicados en la v1. | [[Privacidad y tratamiento de datos]] · [[Copias de seguridad y exportación]] · [[API REST]] |
| 2026-11-04 | `PEN-38` | Contrato de encargado: reparto de responsabilidades entre plataforma y mancomunidad y lista de subencargados. | [[Privacidad y tratamiento de datos]] |
| 2026-11-04 | `PEN-39` | Páginas legales públicas: aviso legal, política de privacidad y condiciones de uso con contenido mínimo. | [[Visión y alcance]] |
| 2026-11-04 | `PEN-40` | Supresión frente a retención: no se borra lo obligatorio, se informa del motivo y plazo y se anonimiza lo posible. | [[Historial de asistencia]] · [[Requisitos no funcionales]] |
| 2026-11-04 | `PEN-41` | Creado [[Mapa de pantallas]] con el inventario por rol y rutas orientativas. | [[Mapa de pantallas]] · [[Roadmap]] |
| 2026-11-04 | `PEN-42` | Creada [[Trazabilidad]] con la estructura de la matriz requisito–endpoint–test–fase. | [[Trazabilidad]] · [[Requisitos funcionales]] · [[API REST]] |
| 2026-11-04 | `PEN-43` | Creado [[Enumeraciones]] con los valores cerrados del dominio. | [[Enumeraciones]] · [[Modelo de datos]] |
| 2026-11-04 | `PEN-44` | Creado [[Manual de usuario]] con el esqueleto por rol. | [[Manual de usuario]] · [[Roadmap]] |
| 2026-11-04 | `PEN-45` | Fijado el nivel AA de WCAG 2.2 en USA-03, USA-08 y [[Convenciones]]. | [[Requisitos no funcionales]] · [[Convenciones]] |
| 2026-11-04 | `PEN-46` | Estrategia i18n: v1 solo en español, textos externalizados en ficheros de recursos. | [[Convenciones]] |
| 2026-11-04 | `PEN-47` | Definition of Done ampliada con tests, cobertura, seguridad, documentación, accesibilidad y despliegue. | [[Cómo usar esta documentación]] · [[Convenciones]] |
| 2026-11-04 | `PEN-48` | Creado [[Plan de pruebas manuales y datos de demo]] con seed de dos mancomunidades y casos mínimos. | [[Plan de pruebas manuales y datos de demo]] · [[Roadmap]] · [[Copias de seguridad y exportación]] |

## Relacionado

[[Portada]] · [[Fase 0 - Definición y documentación]] · [[Roadmap]] · [[Requisitos funcionales]] · [[Requisitos no funcionales]] · [[Roles y permisos]] · [[Modelo de dominio]] · [[Modelo de datos]] · [[API REST]] · [[Seguridad y autenticación]] · [[Sustitución]] · [[Convenciones]] · [[Diario de cambios]]
