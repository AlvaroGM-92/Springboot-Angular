---
tipo: documento
estado: activo
fase: 12
actualizado: 2026-11-03
tags:
  - dominio
  - fichaje
  - movil
  - pwa
  - legal
  - multi-tenant
---

# Control horario

## 1. Propósito y encaje

Registrar la **jornada laboral del profesorado**: cuándo empieza a trabajar, cuándo termina y en qué centro, para que la mancomunidad pueda justificar las horas y resolver incidencias de cobertura. Es un contexto distinto del académico y **no se mezcla con la asistencia de alumnos**.

Se planifica en **Fase 12** (fichaje web y panel de administración) y **Fase 13** (PWA y funcionamiento sin conexión). La decisión y sus motivos están en [[ADR-010 - Control horario diferido y PWA]].

> [!info] Por qué no está en la v1
> El alcance académico ya está cerrado y este contexto añade obligaciones legales, retención propia, permisos nuevos y una estrategia de distribución móvil. Se documenta ahora para no improvisarlo después.

## 2. Base legal

| Aspecto | Requisito |
| --- | --- |
| Obligación | Registro **diario** de la jornada, con hora concreta de inicio y de fin (art. 34.9 ET, RD-ley 8/2019). |
| Conservación | **4 años**, a disposición de la persona trabajadora, la Inspección de Trabajo y la Seguridad Social. |
| Acceso del trabajador | El profesor puede consultar **su propio** historial en cualquier momento. |
| Fiabilidad | El registro debe ser objetivo y no manipulable; no se exige geolocalización. |
| Empleador | La **mancomunidad**, que coincide con el tenant. |

Consecuencia de diseño: a diferencia del historial de asistencia, aquí el profesor **sí** ve su traza y el administrador **sí** puede corregir, siempre con auditoría.

## 3. Alcance funcional

### Profesor (móvil, PWA)

- Pantalla principal con **tres acciones**: `Entrada`, `Salida` e `Historial`.
- Estado visible: «Jornada abierta desde las 09:12 en Centro A» o «Sin jornada abierta».
- Selector de centro al fichar la entrada; el centro se propone según el horario del día.
- Historial propio por semanas y meses, con total de horas.
- Aviso claro cuando un fichaje queda pendiente de sincronizar.

### Administración

- Consulta de fichajes por profesor, centro y rango de fechas.
- Resumen mensual: horas totales, desglose por centro y número de incidencias.
- Bandeja de incidencias: jornadas abiertas más de 12 horas, salidas sin entrada, entradas sin salida al cierre del día, desfase de reloj.
- Corrección y anulación con **motivo obligatorio**, que genera una fila nueva y auditada.
- Exportación CSV para nóminas.

## 4. Modelo de datos propuesto

### `fichaje`

| Columna | Tipo | Restricciones |
| --- | --- | --- |
| `id` | BIGINT | PK |
| `mancomunidad_id` | BIGINT | NOT NULL, FK → `mancomunidad.id` |
| `profesor_id` | BIGINT | NOT NULL, FK → `usuario.id` con pertenencia `PROFESOR` |
| `centro_id` | BIGINT | NOT NULL, FK → `centro.id` de la misma mancomunidad |
| `tipo` | VARCHAR(20) | NOT NULL, CHECK (`ENTRADA`, `SALIDA`) |
| `ocurrido_at` | DATETIME(6) | NOT NULL; hora recibida por el servidor |
| `ocurrido_at_dispositivo` | DATETIME(6) | NULL; hora local cuando el fichaje se creó sin conexión |
| `origen` | VARCHAR(20) | NOT NULL, CHECK (`WEB`, `PWA`) |
| `estado` | VARCHAR(30) | NOT NULL, CHECK (`VALIDO`, `PENDIENTE_VALIDACION`, `CORREGIDO`, `ANULADO`) |
| `uuid_cliente` | CHAR(36) | NOT NULL, UNIQUE por mancomunidad; idempotencia de la cola offline |
| `corrige_a_id` | BIGINT | NULL, FK → `fichaje.id`; fila de corrección o anulación |
| `motivo` | VARCHAR(500) | NULL; obligatorio si `corrige_a_id` no es nulo |
| `dispositivo` | VARCHAR(255) | NULL; agente de usuario resumido |
| `ip` | VARCHAR(45) | NULL |
| `created_at` | DATETIME(6) | NOT NULL |

Índices: `idx_fichaje_profesor (mancomunidad_id, profesor_id, ocurrido_at)` e `idx_fichaje_centro (mancomunidad_id, centro_id, ocurrido_at)`.

La tabla es **append-only**: no se actualiza ni se borra desde la aplicación. El estado efectivo de un fichaje se calcula siguiendo la cadena de correcciones.

### Reglas

- Solo puede existir **una jornada abierta por profesor**: la última fila válida es `ENTRADA` y no tiene `SALIDA` posterior.
- No se admite `ENTRADA` con jornada abierta ni `SALIDA` sin jornada abierta; el backend responde `409`.
- Al cambiar de centro se cierra la jornada en el centro actual y se abre otra en el nuevo.
- La entrada exige centro; la salida hereda el centro de la entrada abierta.
- Un fichaje corregido o anulado no desaparece: se conserva y se marca.
- La hora que prevalece es la del servidor. La del dispositivo solo se guarda como referencia y para detectar desfases.
- Aislamiento por `mancomunidad_id`: un fichaje de otra mancomunidad se comporta como inexistente (`404`).

## 5. Funcionamiento sin conexión

Es el punto crítico en centros de pueblo sin cobertura. La PWA **abre y permite fichar sin red**, pero con matices:

| Escenario | Comportamiento |
| --- | --- |
| Abrir la app sin conexión | Sí: el *service worker* sirve la aplicación cacheada. |
| Iniciar sesión sin conexión | No: la autenticación necesita el servidor. |
| Sesión ya iniciada | Sí hasta que caduque el access token; el refresco también necesita red. |
| Fichar sin conexión | Sí: el fichaje se guarda en una cola local. |
| Consultar historial sin conexión | Solo los últimos datos cacheados, marcados como «puede no estar al día». |
| Sincronización | Automática al recuperar cobertura y abrir la app. |

Detalles de diseño:

- La cola vive en **IndexedDB** y sobrevive al cierre de la app.
- Cada fichaje lleva un **UUID generado en el dispositivo**, y el `POST` es **idempotente**: si se reintenta, no se duplica.
- Se guardan dos horas: la del dispositivo y la de recepción en el servidor. Si el desfase supera un umbral, el fichaje entra como `PENDIENTE_VALIDACION`.
- iOS **no soporta la API de sincronización en segundo plano**: la cola se envía al abrir la app. Android puede usar *Background Sync* como mejora.
- Safari puede desalojar almacenamiento si el dispositivo va justo de espacio o la PWA no está instalada; por eso se avisa al usuario y se sincroniza cuanto antes.
- Si el profesor lleva más de 7 días sin abrir la app y sin cobertura, tendrá que recuperar conexión una vez para renovar la sesión.

> [!warning] Límite asumido
> Un fichaje creado sin conexión no puede demostrar la hora real. Se acepta porque la verificación elegida es de confianza + auditoría, y porque el panel marca estos casos para revisión.

## 6. Verificación de presencia

Decisión: **confianza + auditoría**, sin geolocalización.

- La entrada y la salida las pulsa el propio profesor en su dispositivo.
- La fiabilidad se apoya en la hora del servidor, la inmutabilidad de la tabla, el registro de autor y la revisión administrativa.
- El panel detecta patrones raros: entradas a la misma hora exacta todos los días, jornadas que superan el horario previsto, ausencia de salidas.
- Las correcciones exigen motivo y quedan visibles en el historial.

Mejora futura posible: **QR rotatorio** en cada centro (pantalla o tablet que cambia el código cada minuto). Prueba presencia física sin recoger localización. No se implementa en la Fase 12.

## 7. Requisitos funcionales

| ID | Requisito | Rol | Fase |
| --- | --- | --- | --- |
| FIC-01 | Registrar entrada y salida de la jornada con un solo toque. | Profesor | 12 |
| FIC-02 | Seleccionar el centro al registrar la entrada, proponiendo el del horario del día. | Profesor | 12 |
| FIC-03 | Impedir doble entrada y salida sin entrada abierta. | Sistema | 12 |
| FIC-04 | Consultar el historial propio por semana y mes, con total de horas. | Profesor | 12 |
| FIC-05 | Consultar fichajes de toda la mancomunidad por profesor, centro y fechas. | Admin | 12 |
| FIC-06 | Ver resumen mensual con horas totales, desglose por centro e incidencias. | Admin | 12 |
| FIC-07 | Corregir o anular un fichaje con motivo obligatorio y traza auditable. | Admin | 12 |
| FIC-08 | Exportar el resumen mensual en CSV. | Admin | 12 |
| FIC-09 | Conservar los fichajes 4 años y permitir su consulta por el trabajador. | Sistema | 12 |
| FIC-10 | Registrar la hora del servidor y conservar la del dispositivo solo como referencia. | Sistema | 12 |
| FIC-11 | Permitir fichar sin conexión, encolando el registro y sincronizándolo después. | Sistema | 13 |
| FIC-12 | Instalar la aplicación desde un enlace, sin tiendas, y actualizarla automáticamente. | Sistema | 13 |

## 8. API prevista

| Método | Ruta | Rol | Descripción |
| --- | --- | --- | --- |
| GET | `/fichajes/estado` | Profesor | Jornada abierta actual, si existe. |
| POST | `/fichajes` | Profesor | Registra entrada o salida; idempotente por `uuidCliente`. |
| GET | `/fichajes/mios` | Profesor | Historial propio con `desde` y `hasta`. |
| GET | `/fichajes` | Admin | Listado con `profesorId`, `centroId`, `desde` y `hasta`. |
| GET | `/fichajes/resumen` | Admin | Totales por profesor y mes. |
| POST | `/fichajes/{id}/corregir` | Admin | Crea una fila de corrección con motivo. |
| POST | `/fichajes/{id}/anular` | Admin | Crea una fila de anulación con motivo. |
| GET | `/fichajes/export` | Admin | CSV del periodo. |

## 9. Impacto en roles y retención

- Añade al profesor el permiso de **fichar** y **consultar su historial**; no puede corregir ni ver el de otros.
- Añade al administrador el permiso de **consultar, corregir y exportar** fichajes de su mancomunidad. Es una excepción más al principio de solo lectura, junto con la edición del horario en borrador y su reapertura, y también está auditada.
- Retención propia de **4 años**, independiente de la del historial de asistencia (matrícula + 2 años).
- La purga, si se implementa, será por mancomunidad y con contadores agregados, como el resto.

## 10. Fases

### Fase 12 — Control horario (web)

Fichaje desde el navegador, panel de administración, correcciones auditadas, resumen mensual y exportación. Tests de jornada abierta, doble entrada, aislamiento y corrección.

**Criterio de aceptación:** un profesor ficha entrada y salida desde el navegador del centro; la administración ve el total mensual, corrige una salida olvidada con motivo y la corrección queda en el historial sin borrar el original.

### Fase 13 — PWA y modo sin conexión

Instalación desde enlace, cacheado de la aplicación, cola local de fichajes, sincronización idempotente y modo offline visible. Pruebas en Android e iOS reales, incluida la instalación desde Safari.

**Criterio de aceptación:** con el móvil en modo avión, el profesor abre la app instalada, ficha la entrada y ve el aviso de pendiente; al recuperar cobertura, el fichaje sube una sola vez y aparece en el panel de administración con su marca de origen.

## 11. Dudas

- ¿Se cruza el fichaje con el horario para avisar de «tienes clase y no has fichado»? Útil, pero añade ruido; se decide en la Fase 12.
- ¿Debe el profesor justificar una corrección solicitada o la hace directamente el administrador? En la v1 de la fase, el administrador corrige con motivo; el flujo de solicitud del profesor queda para más adelante.
- ¿Se necesita un cierre mensual bloqueado? La ley no lo exige; se valorará si nóminas lo pide.
- ¿Qué umbral de desfase de reloj marca un fichaje como pendiente? Propuesta: 5 minutos.

## Relacionado

[[ADR-010 - Control horario diferido y PWA]] · [[Roadmap]] · [[Visión y alcance]] · [[Roles y permisos]] · [[Requisitos no funcionales]] · [[Multi-tenencia]] · [[Centro]] · [[Usuario]] · [[Curso escolar]] · [[Horario del profesor]] · [[Historial de asistencia]]
