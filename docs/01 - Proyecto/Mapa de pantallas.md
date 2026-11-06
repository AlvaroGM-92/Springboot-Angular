---
tipo: documento
estado: borrador
fase: 1
actualizado: 2026-11-04
tags:
  - proyecto
  - ux
  - pantallas
---

# Mapa de pantallas

> [!info] Alcance
> Inventario de las pantallas principales por rol. Los wireframes detallados se añadirán en la Fase 1. Las rutas son orientativas y se ajustarán al implementar.

## 1. Sin autenticar

| Pantalla | Ruta | Descripción |
| --- | --- | --- |
| Login | `/login` | Email y contraseña; enlace a recuperación. |
| Recuperación | `/recuperar` | Solicitud con email; siempre responde igual. |
| Restablecer contraseña | `/restablecer` | Token en la URL, nueva contraseña. |
| Verificar email | `/verificar-email` | Confirma alta o cambio de dirección. |

## 2. Selector de mancomunidad

| Pantalla | Ruta | Descripción |
| --- | --- | --- |
| Selector | `/seleccionar-mancomunidad` | Lista de pertenencias y opción Plataforma; token de selección. |

## 3. Plataforma (solo `SUPERADMIN`)

| Pantalla | Ruta | Descripción |
| --- | --- | --- |
| Mancomunidades | `/plataforma/mancomunidades` | Listado con métricas bajo demanda, alta y edición. |
| Detalle de mancomunidad | `/plataforma/mancomunidades/:id` | Detalle, suspensión/reactivación. |
| Cuentas | `/plataforma/usuarios` | Búsqueda global y soporte de cuentas. |
| Detalle de cuenta | `/plataforma/usuarios/:id` | Pertenencias, reset, desbloqueo, cambio de rol. |
| Auditoría | `/plataforma/auditoria` | Filtros por actor, afectado, acción y fechas. |
| Bootstrap | (comando, sin pantalla) | Alta del primer superadministrador. |

## 4. Profesor

| Pantalla | Ruta | Descripción |
| --- | --- | --- |
| Inicio | `/profesor` | Resumen: mis asignaturas, próximas sesiones, solicitudes pendientes. |
| Mis asignaturas | `/profesor/asignaturas` | Listado con enlace a la ficha. |
| Horario (rejilla) | `/profesor/horario` | Rejilla editable en `BORRADOR`, solo lectura en `FINALIZADO`. |
| Sesión de clase | `/profesor/sesiones/:id` | Lista de asistencia editable. |
| Observaciones | `/profesor/observaciones` | Registro y consulta propias. |
| Preparación | `/profesor/preparaciones` | Planificación de clases. |
| Solicitudes | `/profesor/solicitudes` | Crear y consultar el estado (con contador de resueltas). |
| Mi historial de asistencia | (no visible) | El profesor no consulta el historial de asistencia. |

## 5. Administrador

| Pantalla | Ruta | Descripción |
| --- | --- | --- |
| Inicio | `/admin` | Resumen de la mancomunidad. |
| Usuarios | `/admin/usuarios` | CRUD de usuarios y pertenencias. |
| Centros | `/admin/centros` | CRUD de centros. |
| Cursos escolares | `/admin/cursos` | Crear, activar, cerrar y reabrir. |
| Asignaturas | `/admin/asignaturas` | CRUD y asignación de profesores. |
| Sustituciones | `/admin/sustituciones` | Alta y anulación por asignatura y periodo. |
| Alumnos | `/admin/alumnos` | CRUD, ficha y matrículas. |
| Instrumentos | `/admin/instrumentos` | Catálogo y asignación (solo si el módulo está activo). |
| Horarios (global) | `/admin/horarios` | Vista global editable en `BORRADOR`. |
| Solicitudes | `/admin/solicitudes` | Listado, aprobación y rechazo. |
| Días no lectivos | `/admin/dias-no-lectivos` | CRUD por curso y centro. |
| Consultas | `/admin/consultas` | Observaciones, asistencia e historial (solo lectura). |

## 6. Común

| Pantalla | Ruta | Descripción |
| --- | --- | --- |
| Perfil | `/perfil` | Datos propios, cambio de contraseña y de email. |
| Sesiones | `/sesiones` | Lista de familias activas y cierre global. |
| Configuración | `/configuracion` | Configuración de la mancomunidad activa. |
| Cambio obligatorio | `/cambiar-password` | Tras una contraseña temporal o un reset. |

## Relacionado

[[Roles y permisos]] · [[Requisitos funcionales]] · [[API REST]] · [[Roadmap]] · [[Convenciones]]