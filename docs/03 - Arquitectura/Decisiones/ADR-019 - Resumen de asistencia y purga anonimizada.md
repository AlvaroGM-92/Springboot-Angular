---
tipo: adr
estado: aceptada
fecha: 2026-11-03
actualizado: 2026-11-03
tags:
  - adr
  - asistencia
  - retencion
  - purga
---

# ADR-019 — Resumen de asistencia y purga anonimizada

## Contexto

La retención de [[Historial de asistencia]] promete conservar «contadores agregados y anonimizados» después de purgar las asistencias y su historial, pero no existía tabla para guardarlos. Además, la purga se calculaba desde `matricula.baja_at` sin una fecha programada explícita, y [[Sesión de clase]] no tenía `curso_escolar_id`, lo que hacía ambiguo a qué curso pertenecía una sesión puntual y complicaba la purga por matrícula.

## Decisión

### Resumen de asistencia

- Se crea la tabla `resumen_asistencia` y la entidad [[Resumen de asistencia]].
- Guarda contadores por **`alumno_hash`, asignatura y curso**: `sesiones`, `presentes`, `ausentes`, `ausencias_justificadas`, `retrasos` y `minutos_retraso`.
- **No guarda `alumno_id` ni datos personales directos.** `alumno_hash` es `SHA-256(mancomunidad_id:alumno_id:APP_PURGE_SECRET)`, calculado en el servidor. El secreto vive en una variable de entorno y no se versiona.
- La clave lógica es `(mancomunidad_id, alumno_hash, asignatura_id, curso_escolar_id)`. La purga hace `INSERT ... ON DUPLICATE KEY UPDATE` para acumular si la matrícula se reactiva y vuelve a causar baja.
- `sesiones` es la suma de los cuatro estados; las sesiones `CANCELADA` no cuentan.
- Es de solo lectura desde la aplicación y se escribe únicamente en la purga. No se expone un endpoint de escritura.

### Fechas de purga en la matrícula

- `matricula.purga_at` guarda la fecha programada: `baja_at + 2 años`. Se calcula al pasar a `BAJA` y se limpia al reactivar.
- `matricula.purgada_at` guarda el momento en que se ejecutó la purga. Evita reprocesar la misma matrícula.
- Al reactivar antes de la purga se limpian `purga_at` y `purgada_at`. Si se reactiva después de una purga, se inicia un nuevo periodo de retención y el resumen acumula los nuevos contadores.
- La purga selecciona matrículas `BAJA` con `purga_at <= ahora` y `purgada_at` nulo, por mancomunidad y en lotes.

### Curso en la sesión

- `sesion_clase.curso_escolar_id` pasa a ser **obligatorio**. Las sesiones generadas lo heredan de la franja; las puntuales usan el curso activo o el indicado al crearlas.
- Esto permite purgar por `(mancomunidad_id, asignatura_id, curso_escolar_id, alumno_id)` sin inferir el curso por fechas.
- La sesión conserva su `franja_id` opcional; el curso no depende de que la franja siga existiendo.

### Proceso de purga

1. Selecciona la matrícula y calcula el resumen desde `asistencia` e `historial_asistencia` de sus sesiones.
2. Escribe o actualiza `resumen_asistencia` con `alumno_hash`, asignatura y curso.
3. Borra el historial de las asistencias y después las asistencias.
4. Marca `purgada_at` y no toca la matrícula, el alumno, las sesiones, las observaciones ni las preparaciones.
5. Se ejecuta por mancomunidad, en lotes y con registro de filas afectadas.

### API

- `GET /asistencias/resumen` acepta `alumnoId`, `asignaturaId` y `cursoEscolarId`.
- Devuelve los contadores en vivo de las matrículas no purgadas y los del resumen cuando ya se purgaron. El campo `historico` indica el origen.
- Como el resumen no guarda `alumno_id`, el servicio calcula el hash del alumno solicitado para localizarlo.

## Consecuencias

### Positivas

- La promesa de conservar contadores deja de ser una intención y tiene tabla y proceso.
- La purga no deja datos personales en el resumen y no rompe el aislamiento por mancomunidad.
- `purga_at` y `purgada_at` hacen la retención explícita, idempotente y auditable.
- El curso obligatorio en la sesión desambigua sesiones puntuales y facilita informes y purgas.

### Negativas / compromisos

- El hash depende de un secreto; si se pierde o se rota, se pierde la vinculación histórica con el alumno.
- La purga debe ejecutarse con cuidado: primero agrega, luego borra y por último marca.
- Una reactivación después de la purga no recupera el historial borrado; solo conserva los contadores.
- El resumen no sirve para auditoría individual detallada, solo para estadística.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Guardar `alumno_id` en el resumen | Mantiene un dato personal después de la purga y contradice la anonimización. |
| Calcular la purga solo con `baja_at + 2 años` en consulta | No deja una fecha programada ni permite marcar ejecuciones; complica los reintentos. |
| Una tabla de contadores por curso y asignatura sin alumno | Pierde el detalle por alumno que se quiere conservar de forma pseudónima. |
| Purga inmediata al dar de baja | Incumple la retención de 2 años y elimina datos que aún deben conservarse. |
| Mantener el resumen en vivo desde cada cambio de asistencia | Añade escritura y riesgo de deriva; la purga es el único punto que lo necesita. |

## Relacionado

[[Resumen de asistencia]] · [[Historial de asistencia]] · [[Asistencia]] · [[Matrícula]] · [[Sesión de clase]] · [[Curso escolar]] · [[Modelo de datos]] · [[API REST]] · [[Roadmap]] · [[ADR-007 - Historial de cambios de asistencia]] · [[ADR-008 - Curso escolar como entidad]]
