---
tipo: documento
estado: borrador
fase: 1
actualizado: 2026-11-04
tags:
  - proyecto
  - pruebas
  - datos-demo
---

# Plan de pruebas manuales y datos de demo

> [!info] Alcance
> Seed estable para desarrollo y pruebas manuales. Se implementa en la Fase 1 junto con la migración inicial.

## 1. Datos de demo

Seed con **dos mancomunidades** para poder probar el aislamiento:

| Mancomunidad | Código | Tipo | Centros | Cursos |
| --- | --- | --- | --- | --- |
| Mancomunidad A | `demo-a` | `MUSICA` | 2 | `2026-2027` activo |
| Mancomunidad B | `demo-b` | `IDIOMAS` | 1 | `2026-2027` activo |

Por mancomunidad:

- 1 administrador de prueba (con `admin_plataforma = true` solo en `dev`).
- 3 profesores, uno compartido entre A y B para probar el aislamiento de conflictos.
- 5 asignaturas por mancomunidad (al menos una musical instrumental en A y una de idiomas en B).
- 20 alumnos en A y 10 en B, con tutores informados.
- Instrumentos solo en A.
- Horarios en `BORRADOR` y `FINALIZADO`, con franjas solapadas y no solapadas.
- Sesiones `PROGRAMADA`, `ABIERTA` y `CANCELADA`, y al menos un día no lectivo.
- Matrículas activas y alguna en `BAJA` para probar la purga.

## 2. Casos de prueba manual mínimos

- Login con 0, 1 y varias pertenencias.
- Cambio de mancomunidad sin volver a introducir la contraseña.
- Contraseña temporal y cambio obligatorio.
- Recuperación por email y bloqueo por intentos fallidos.
- Alta y edición de usuarios, centros, asignaturas, alumnos e instrumentos.
- Matrícula, baja y reactivación.
- Montaje, finalización, solicitud, aprobación/rechazo y reapertura de un horario.
- Conflicto de alumno entre grupos distintos.
- Copia de horario al curso siguiente.
- Sustitución de un profesor y reasignación de sesiones.
- Cancelación de una clase y gestión de días no lectivos.
- Cierre y reapertura de curso.
- Purga de historial tras 2 años de baja.
- Exportación lógica por mancomunidad.
- Aislamiento: un usuario de A no ve datos de B cambiando IDs.

## 3. Reglas del seed

- El seed solo existe en el perfil `dev` y `test`.
- Los datos son deterministas y reproducibles: mismos IDs y mismos correos en cada ejecución.
- No se usan datos personales reales; los nombres son ficticios.
- La restauración de una copia de staging debe dejar el seed intacto.

## Relacionado

[[Roadmap]] · [[Copias de seguridad y exportación]] · [[Modelo de datos]] · [[Requisitos funcionales]] · [[Mapa de pantallas]]