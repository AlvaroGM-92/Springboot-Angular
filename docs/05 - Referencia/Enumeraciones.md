---
tipo: referencia
estado: borrador
fase: 0
actualizado: 2026-11-04
tags:
  - referencia
  - enumeraciones
  - modelo
---

# Catálogo de enumeraciones

> [!info] Para qué sirve
> Listado único de los valores cerrados usados en el dominio y en el modelo físico, para que no diverjan entre entidades, API y base de datos. Se actualiza cuando se añade un valor nuevo.

## Roles y ámbitos

| Enumeración | Valores | Nota |
| --- | --- | --- |
| `RolMancomunidad` | `PROFESOR`, `ADMINISTRADOR` | Vive en `usuario_mancomunidad.rol`. |
| `Scope` | `TENANT`, `PLATFORM` | Vive en el access token y en `refresh_token.scope`. |

## Mancomunidad y asignatura

| Enumeración | Valores | Nota |
| --- | --- | --- |
| `TipoMancomunidad` | `MUSICA`, `DANZA`, `ARTES_ESCENICAS`, `IDIOMAS`, `DEPORTES`, `OTRA` | Informativo; no restringe asignaturas. |
| `TipoAsignatura` | `MUSICA`, `DANZA`, `ARTES_ESCENICAS`, `IDIOMAS`, `DEPORTES`, `OTRA` | Independiente del tipo de la mancomunidad. |
| `EstadoMancomunidad` | `ACTIVA`, `SUSPENDIDA` | Suspendida bloquea login y refresh. |

## Curso y horario

| Enumeración | Valores | Nota |
| --- | --- | --- |
| `EstadoCursoEscolar` | `PLANIFICACION`, `ACTIVO`, `CERRADO` | Un único `ACTIVO` por mancomunidad. |
| `EstadoHorario` | `BORRADOR`, `FINALIZADO` | `FINALIZADO` bloquea la edición de franjas. |
| `EstadoSesion` | `PROGRAMADA`, `ABIERTA`, `CERRADA`, `CANCELADA` | `CANCELADA` solo desde `PROGRAMADA`. |
| `OrigenSesion` | `GENERADA`, `PUNTUAL` | Las generadas nacen `PROGRAMADA`. |
| `EstadoSolicitud` | `PENDIENTE`, `APROBADA`, `RECHAZADA`, `CANCELADA` | Cancelada solo si estaba pendiente. |
| `DecisionSolicitud` | `APROBAR`, `RECHAZAR` | Cuerpo de `resolver`. |
| `EstadoSustitucion` | `ACTIVA`, `ANULADA` | No cambia el horario. |
| `OrigenHistorialHorario` | `CREACION`, `EDICION`, `FINALIZACION`, `REAPERTURA`, `COPIA` | Trazabilidad de estados. |

## Matrícula y asistencia

| Enumeración | Valores | Nota |
| --- | --- | --- |
| `EstadoMatricula` | `ACTIVA`, `BAJA` | `BAJA` conserva historial. |
| `EstadoAsistencia` | `PRESENTE`, `AUSENTE`, `RETRASO`, `JUSTIFICADA` | Editable siempre. |
| `OrigenAsistencia` | `CREACION`, `EDICION`, `CORRECCION` | Para el historial. |

## Seguridad

| Enumeración | Valores | Nota |
| --- | --- | --- |
| `TipoEventoAcceso` | `FALLIDO`, `EXITOSO`, `DESBLOQUEO` | Tabla `evento_acceso`. |
| `MotivoRevocacionRefresh` | `LOGOUT`, `ROTACION`, `REUTILIZACION`, `CAMBIO_PASSWORD`, `CAMBIO_EMAIL`, `DESACTIVACION`, `CAMBIO_ROL`, `LIMITE_SESIONES` | Tabla `refresh_token`. |
| `TipoEventoSeguridad` | `REUTILIZACION_REFRESH`, `REVOCACION_MASIVA`, `ROTACION_CLAVE`, `CAMBIO_EMAIL`, `VERIFICACION_EMAIL`, `LIMITE_SESIONES`, `BOOTSTRAP_SUPERADMIN` | Tabla `evento_seguridad`. |
| `AccionAuditoriaPlataforma` | `ALTA_MANCOMUNIDAD`, `EDICION_MANCOMUNIDAD`, `SUSPENSION_MANCOMUNIDAD`, `REACTIVACION_MANCOMUNIDAD`, `ALTA_SUPERADMIN`, `RESET_PASSWORD`, `CAMBIO_ROL`, `ACTIVACION_CUENTA`, `DESACTIVACION_CUENTA`, `ACTIVACION_PERTENENCIA`, `DESACTIVACION_PERTENENCIA`, `CORRECCION_EMAIL`, `DESBLOQUEO_CUENTA` | Tabla `auditoria_plataforma`. |
| `EstadoIdempotencia` | `EN_CURSO`, `COMPLETADA` | Tabla `idempotencia`. |

## Relacionado

[[Modelo de datos]] · [[Requisitos funcionales]] · [[Convenciones]] · [[API REST]]