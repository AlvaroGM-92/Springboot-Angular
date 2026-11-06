# AGENTS.md — reglas del vault

> Este directorio **es** el vault de Obsidian. Se abre `docs/` como vault, nunca
> la raíz del repositorio, para que Obsidian no indexe el código ni `node_modules`.
> Estas reglas se cargan cuando el agente trabaja dentro de `docs/`.
>
> Este fichero es **configuración**, no una nota del vault: no lleva frontmatter
> ni se enlaza desde la portada.

## Estructura

| Carpeta | Contenido |
| --- | --- |
| `00 - Inicio/` | Portada, guía de uso y glosario |
| `01 - Proyecto/` | Visión, roles, requisitos funcionales y no funcionales |
| `02 - Dominio/` | Modelo de dominio y entidades en `Entidades/` |
| `03 - Arquitectura/` | Stack, arquitectura, seguridad, multi-tenencia y ADRs en `Decisiones/` |
| `04 - Fases/` | Roadmap y una nota por fase |
| `05 - Referencia/` | Modelo de datos, API REST y convenciones |
| `06 - Diario/` | Una nota por sesión y el índice `Diario de cambios` |
| `99 - Plantillas/` | Plantillas de fase, entidad, ADR y sesión |
| `99 - Adjuntos/` | Imágenes y ficheros incrustados |

## Frontmatter

Toda nota empieza con:

```yaml
---
tipo: entidad | adr | documento | fase | diario | moc | guia | plantilla | referencia
estado: borrador | activo | en-revision | aceptada | cerrada | obsoleta
fase: <número>
actualizado: YYYY-MM-DD
tags:
  - ...
---
```

Campos adicionales según el tipo: `fecha` en ADR y diario, `inicio` y `fin` en
fases. **`actualizado` se cambia siempre que se toca el contenido.**

## Enlaces

- Usa wikilinks: `[[Modelo de datos]]`, no rutas relativas.
- Al renombrar una nota, actualiza todos los enlaces (Obsidian lo hace solo con
  `alwaysUpdateLinks`, activado en este vault). Comprueba que no queden enlaces rotos.
- No enlaces a notas que no existen ni a encabezados que hayas renombrado.

## Una nota, una responsabilidad

- No dupliques contenido: si un dato ya está en otra nota, enlázala.
- Las entidades van en `02 - Dominio/Entidades/`, una por fichero.
- Los ADRs son inmutables en su decisión: si cambia, se crea otro ADR que
  sustituye al anterior y se anota en el antiguo.
- Las notas de fase no repiten el roadmap: lo enlazan.

## Al crear una nota

1. Parte de la plantilla correspondiente en `99 - Plantillas/`.
2. Nombre en español, descriptivo y sin prefijos numéricos.
3. Rellena el frontmatter completo.
4. Enlázala desde la nota que la contiene: [[Portada]], el índice del área o la
   nota de fase. Una nota huérfana es un error.
5. Si la sesión produjo cambios, añádela al diario.

## Al modificar una nota existente

- Respeta la estructura y el tono de la nota; no reescribas secciones que no
  vienen al caso.
- Mantén las tablas Markdown bien formadas: mismo número de columnas en todas
  las filas, con cabecera y separador.
- Actualiza `actualizado` y, si cambia una decisión, el ADR correspondiente.
- Si cambia el estado de una fase, actualiza [[Portada]], [[Roadmap]] y
  [[Diario de cambios]].

## Glosario y lenguaje

- Si aparece un término nuevo del dominio, se añade a [[Glosario]].
- Español para el dominio; sufijos técnicos en inglés (`Controller`, `Service`,
  `DTO`, `Repository`).
- Fechas en formato `YYYY-MM-DD`; horas en ISO 8601.

## Verificación

Antes de cerrar un cambio en el vault:

- Todos los `[[enlaces]]` resuelven a una nota existente.
- No hay tablas con columnas descuadradas ni bloques de código sin cerrar.
- Las notas tocadas tienen `actualizado` con la fecha del día.
- La nota es alcanzable desde [[Portada]] o desde el índice de su área.

## Qué no hacer

- No edites `docs/.obsidian/` a mano ni versiones `workspace*.json`.
- No muevas ficheros por la terminal si Obsidian está abierto: usa la aplicación
  para que los enlaces se actualicen.
- No conviertas una nota en un cajón de sastre: si crece, se divide y se enlaza.
