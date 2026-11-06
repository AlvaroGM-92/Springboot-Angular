---
tipo: adr
estado: aceptada
fecha: 2026-10-27
actualizado: 2026-10-27
tags:
  - adr
  - documentacion
---

# ADR-006 — Documentación en Obsidian

## Contexto

El proyecto anterior concentraba toda la documentación en un único `Memoria.md`. Con el crecimiento del dominio, ese archivo se vuelve difícil de navegar y de mantener. El autor usa Obsidian y quiere notas enlazadas.

## Decisión

- La documentación vive en `docs/`, como vault de Obsidian.
- Se usan **notas atómicas** por tema: requisitos, entidades, ADRs, fases, referencia y diario.
- Una nota **MOC** ([[Portada]]) actúa como índice.
- Cada fase tiene su nota y cada decisión relevante un ADR.
- El [[Diario de cambios]] registra las sesiones.
- Los enlaces internos y las etiquetas permiten navegar y buscar.

## Consecuencias

### Positivas

- Navegación rápida y contexto local.
- El grafo muestra relaciones entre dominio, arquitectura y fases.
- Git versiona la documentación junto al código.
- Las decisiones no se pierden en un documento gigante.

### Negativas / compromisos

- Requiere disciplina: actualizar enlaces y fechas.
- Riesgo de duplicar información si no se enlaza.
- Más archivos que mantener.

## Reglas

- No se duplica un dato: se enlaza.
- Los IDs (`RF-XXX`, `ADR-NNN`) son estables.
- Toda nota editada actualiza `actualizado`.
- Una decisión obsoleta se marca, no se borra.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Un único `Memoria.md` | Se vuelve inmanejable y mezcla niveles. |
| Wiki externa | Menos integrada con el repositorio. |
| Solo comentarios en el código | No sirve para requisitos ni decisiones. |

## Relacionado

[[Cómo usar esta documentación]] · [[Portada]] · [[Diario de cambios]]
