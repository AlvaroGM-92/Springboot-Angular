---
tipo: adr
estado: aceptada
fecha: 2026-10-27
actualizado: 2026-10-27
tags:
  - adr
  - base-de-datos
---

# ADR-002 — Base de datos MySQL y Flyway

## Contexto

Se necesita una base de datos relacional para un dominio con relaciones N:M, histórico por curso y consultas filtradas. El entorno ya tiene un contenedor PostgreSQL de una prueba anterior, pero la decisión del proyecto es usar MySQL/MariaDB.

## Decisión

- **MySQL 8.4 LTS** en contenedor Docker.
- **Flyway** como única vía de creación y evolución del esquema.
- Codificación `utf8mb4`, timestamps en UTC y borrado lógico en datos maestros.
- El contenedor PostgreSQL existente **no se elimina** en esta fase; simplemente no se usa. Se revisará en la Fase 1.

## Consecuencias

### Positivas

- MySQL es conocido por el autor y encaja con la experiencia previa.
- Flyway deja el esquema versionado y reproducible.
- Docker Compose facilita levantar el entorno en cualquier máquina.
- MySQL 8.4 es LTS y tiene buen soporte en Spring Boot.

### Negativas / compromisos

- No hay índices parciales; la unicidad de «una solicitud pendiente por franja» se valida en el servicio.
- Los solapamientos de horario se validan en Java, no con una restricción SQL.
- Hay que mantener migraciones ordenadas y probarlas en limpio.
- El puerto 3306 debe estar libre; el 5432 del PostgreSQL anterior no interfiere.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| PostgreSQL 17 | Ya está instalado, pero la decisión del proyecto es MySQL/MariaDB. |
| MariaDB 11.4 | Compatible; se puede reconsiderar si MySQL da problemas. |
| H2 en desarrollo | Oculta diferencias de SQL y de Flyway. |
| MongoDB | El dominio es claramente relacional. |

## Relacionado

[[Stack tecnológico]] · [[Modelo de datos]] · [[Convenciones]] · [[Roadmap]]
