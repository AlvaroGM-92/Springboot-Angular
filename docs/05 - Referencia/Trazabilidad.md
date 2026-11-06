---
tipo: referencia
estado: borrador
fase: 0
actualizado: 2026-11-04
tags:
  - referencia
  - trazabilidad
  - requisitos
---

# Trazabilidad

> [!info] Para qué sirve
> Matriz que enlaza cada requisito con su endpoint, su prueba y su fase, para saber qué falta antes de la Fase 10. Se mantiene actualizada al cerrar cada fase; en la Fase 0 solo se fija la estructura.

## Estructura

| Requisito | Endpoint principal | Prueba | Fase | Estado |
| --- | --- | --- | --- | --- |
| `AUTH-01` | `POST /auth/login` | Login correcto/incorrecto, 0/1/varias pertenencias | 2 | Pendiente |
| `AUTH-16` | `POST /auth/login` | Bloqueo por 5/20 fallos en 15 min | 2 | Pendiente |
| `USR-02` | `POST /usuarios` | Alta de profesor y asignación de pertenencia | 3 | Pendiente |
| `HOR-01` | `POST /horarios` | Creación de horario en borrador | 6 | Pendiente |
| `SES-01` | `POST /sesiones/{id}/abrir` | Apertura y carga de alumnos como presentes | 8 | Pendiente |

## Cómo se mantiene

- Cada requisito de [[Requisitos funcionales]] debe tener al menos una fila cuando su fase empiece.
- El estado sigue el de la fase: `Pendiente`, `En curso`, `Hecho`.
- Si un requisito se divide en varios endpoints, se añade una fila por endpoint.
- Las pruebas de aislamiento se referencian en la columna `Prueba` junto a la funcional.
- Al cerrar una fase se revisa que no queden requisitos huérfanos.

## Relacionado

[[Requisitos funcionales]] · [[API REST]] · [[Roadmap]] · [[Convenciones]] · [[Modelo de datos]]