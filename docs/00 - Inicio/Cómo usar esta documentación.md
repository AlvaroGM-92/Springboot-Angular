---
tipo: guia
estado: activo
fase: 0
actualizado: 2026-11-04
tags:
  - documentacion
  - guia
---

# Cómo usar esta documentación

## 1. Abrir el vault en Obsidian

La documentación está en la carpeta `docs/` del proyecto:

```text
SpringbootAngular/
├── docs/          ← abrir esta carpeta como vault de Obsidian
├── backend/       ← se creará en la Fase 1
├── frontend/      ← se creará en la Fase 1
└── .gitignore
```

En Obsidian: **Abrir carpeta como vault** → seleccionar `SpringbootAngular/docs`.

> [!tip] Si prefieres abrir la raíz del proyecto
> Puedes abrir `SpringbootAngular/` como vault, pero añade a **Ajustes → Archivos y enlaces → Archivos excluidos**:
> `backend/target`, `frontend/node_modules`, `frontend/dist`, `frontend/.angular` y `.git`.
> Así el grafo y las búsquedas no se llenan de dependencias.

## 2. Estructura del vault

| Carpeta | Contenido |
| --- | --- |
| `00 - Inicio` | Portada, esta guía y glosario. |
| `01 - Proyecto` | Visión, alcance, roles y requisitos. |
| `02 - Dominio` | Modelo conceptual y una nota por entidad. |
| `03 - Arquitectura` | Stack, arquitectura, seguridad y ADRs. |
| `04 - Fases` | Roadmap y nota detallada de cada fase. |
| `05 - Referencia` | Modelo de datos físico, API y convenciones. |
| `06 - Diario` | Registro cronológico de sesiones. |
| `99 - Plantillas` | Plantillas para no improvisar el formato. |

## 3. Tipos de nota

Cada nota declara su tipo en el *frontmatter*:

| `tipo` | Para qué sirve |
| --- | --- |
| `moc` | Mapa de contenido; solo enlaces y estado. |
| `documento` | Visión, alcance, requisitos y documentos de proyecto. |
| `guia` | Guía de uso del propio vault. |
| `entidad` | Una entidad del dominio. |
| `adr` | Una decisión de arquitectura. |
| `fase` | Alcance, tareas y criterios de una fase. |
| `diario` | Sesión de trabajo concreta. |
| `referencia` | Modelo de datos, API, convenciones. |
| `plantilla` | Patrón reutilizable. |

Ejemplo de frontmatter:

```yaml
---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-10-27
tags:
  - dominio
  - entidad
---
```

**Estados:**

| `estado` | Significado | Tipos habituales |
| --- | --- | --- |
| `borrador` | En elaboración; puede cambiar sin aviso y no es fuente de verdad. | Cualquiera |
| `en-revision` | Contenido completo, pendiente de aprobación. | `fase`, `documento` |
| `activo` | Vigente y fuente de verdad actual. | `moc`, `guia`, `documento`, `entidad`, `referencia`, `plantilla`, `diario` |
| `aceptada` | Decisión aprobada. | `adr` |
| `cerrada` | Fase terminada. | `fase` |
| `obsoleta` | Sustituida por otra nota o decisión; se conserva por historial. | `adr`, `documento` |

> [!note] Regla de estado
> Un ADR nunca pasa a `activo`: o está `aceptada` o queda `obsoleta`. Una entidad, referencia o guía vigente está `activo`. Una fase solo usa `en-revision` o `cerrada`.

## 4. Enlaces, etiquetas y callouts

- **Enlaces internos:** `[[Nombre exacto de la nota]]`. Obsidian los resuelve aunque estén en otra carpeta.
- **Enlaces con texto:** `[[Modelo de datos|ver el modelo físico]]`.
- **Etiquetas:** `#fase/1`, `#dominio`, `#seguridad`. Sirven para búsquedas transversales.
- **Callouts:** `> [!info]`, `> [!warning]`, `> [!todo]`, `> [!decision]`.
- **Diagramas:** bloques ` ```mermaid `; Obsidian los renderiza sin plugins.

## 5. Flujo de trabajo por fase

### Antes de empezar

1. Leer la nota de la fase en `04 - Fases`.
2. Revisar los requisitos implicados en [[Requisitos funcionales]].
3. Revisar las entidades del dominio que se van a tocar.
4. Anotar dudas abiertas en la propia nota de la fase.

### Durante

1. Crear/actualizar la nota de fase con el alcance real.
2. Si aparece una decisión técnica relevante, escribir un ADR **antes** de implementarla.
3. Si cambia el modelo, actualizar [[Modelo de datos]] y las notas de entidad.
4. Si cambia un requisito, actualizar [[Requisitos funcionales]].
5. Anotar problemas y soluciones en la sesión del diario.

### Al terminar

1. Marcar tareas y criterios de aceptación de la fase.
2. Actualizar el estado en [[Roadmap]] y en la [[Portada|portada]].
3. Escribir la entrada del día en [[Diario de cambios]].
4. Hacer commit con `docs:` o `feat:` según corresponda.
5. Dejar claro cuál es el siguiente paso.

## 6. Reglas de oro

> [!warning] Evitar que la documentación se pudra
> - **Una nota = una idea.** Si una nota crece demasiado, se divide y se enlaza.
> - **No duplicar.** Si un dato ya está en otra nota, se enlaza.
> - **IDs estables.** `RF-XXX`, `ADR-NNN`, `Fase N` no se renumeran.
> - **No borrar decisiones.** Si una decisión cambia, se marca la anterior como `obsoleta` y se crea otra.
> - **Fecha de actualización.** Toda nota editada actualiza `actualizado`.
> - **Enlaces bidireccionales.** La nota enlazada debe enlazar de vuelta cuando aporte contexto.

## 7. Definition of Done de una fase

Una fase se considera terminada cuando:

- [ ] El alcance está escrito y no hay requisitos huérfanos.
- [ ] Las decisiones nuevas están en un ADR.
- [ ] El modelo de datos y la API están actualizados.
- [ ] El código compila y los tests pasan.
- [ ] Los criterios de aceptación se han verificado.
- [ ] La documentación y el diario están al día.
- [ ] El commit está hecho y la fase es desplegable.

### Criterios mínimos por tipo de cambio

- **Tests:** unitarios de servicios y reglas de negocio; integración con Testcontainers MySQL con dos mancomunidades para cualquier módulo de negocio; pruebas de aislamiento que demuestren que un usuario de A no lee ni escribe datos de B.
- **Cobertura:** no se fija un porcentaje ciego; se exige cobertura de las ramas de negocio y de los casos de error documentados en la API.
- **Seguridad:** revisión de que toda consulta filtra por `mancomunidad_id`, que no se acepta `mancomunidadId` del cliente, que los endpoints nuevos respetan rol y propiedad, y que las escrituras sensibles llevan `Idempotency-Key` y respetan el rate limiting cuando aplique.
- **Documentación:** entidades, modelo de datos, API, ejemplos, requisitos, roles y roadmap actualizados; entrada en el diario y actualización de `Pendientes` si se cierra un punto.
- **Accesibilidad:** verificación WCAG 2.2 AA con Lighthouse y revisión manual por teclado para las pantallas nuevas o modificadas.
- **Despliegue:** `docker compose up -d`, `./mvnw verify` y `npm run build` en verde, y el pipeline de CI en verde.

## 8. Cómo crear notas nuevas

| Quiero... | Plantilla |
| --- | --- |
| Empezar una fase | [[Plantilla - Fase]] |
| Documentar una entidad | [[Plantilla - Entidad]] |
| Registrar una decisión | [[Plantilla - ADR]] |
| Anotar una sesión | [[Plantilla - Sesión de diario]] |

## 9. Relación con el código

La documentación manda sobre el alcance y las decisiones; el código manda sobre el detalle de implementación. Si el código se desvía de lo documentado:

1. Se decide si el cambio es aceptable.
2. Si lo es, se actualiza la documentación y, si procede, se crea un ADR.
3. Si no lo es, se corrige el código.

## Relacionado

[[Portada]] · [[Glosario]] · [[Convenciones]] · [[Roadmap]]
