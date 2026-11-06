---
tipo: adr
estado: aceptada
fecha: 2026-10-27
actualizado: 2026-10-27
tags:
  - adr
  - arquitectura
  - stack
---

# ADR-001 — Stack tecnológico

## Contexto

El proyecto es una aplicación de gestión académica con dos roles, SPA y API. Se quiere una base moderna, con demanda en el mercado y adecuada para aprender Spring Boot y Angular. El autor ya tiene experiencia con PHP y quiere reconstruir el dominio con otro stack.

## Decisión

- **Backend:** Java 21 LTS + Spring Boot 4.1.2 + Maven (wrapper).
- **Frontend:** Angular 22 + Angular Material + SCSS.
- **Comunicación:** API REST JSON versionada en `/api/v1`.
- **Repositorio:** monorepo con `backend/`, `frontend/` y `docs/`.

## Consecuencias

### Positivas

- Stack muy demandado y con documentación abundante.
- Spring Security, Validation, JPA y Flyway reducen código repetitivo.
- Angular ofrece routing, formularios, HTTP y Material de forma integrada.
- Monorepo facilita mantener documentación, backend y frontend sincronizados.

### Negativas / compromisos

- Dos despliegues y dos toolchains que mantener.
- CORS y proxy en desarrollo.
- Spring Boot 4 y Angular 22 son versiones recientes; hay que evitar tutoriales antiguos.
- Más complejidad inicial que una aplicación server-side con Thymeleaf.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Spring Boot + Thymeleaf | Menos separación frontend/backend y menos ejercicios de SPA. |
| PHP + Laravel | Ya hay experiencia previa; no aporta el objetivo de aprender Spring/Angular. |
| Next.js + NestJS | Stack válido, pero se quiere practicar Java/Spring. |
| Microservicios | Complejidad innecesaria para el tamaño del dominio. |

## Relacionado

[[Stack tecnológico]] · [[Arquitectura general]] · [[ADR-002 - Base de datos MySQL y Flyway]]
