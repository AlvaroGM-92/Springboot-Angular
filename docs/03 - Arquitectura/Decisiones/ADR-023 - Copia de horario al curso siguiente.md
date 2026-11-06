---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - horarios
  - curso
---

# ADR-023 — Copia de horario al curso siguiente

## Contexto

Al abrir un curso nuevo, cada profesor vuelve a montar su horario. La mayoría de las franjas se repiten, pero algunas asignaturas cambian de profesor, se desactivan o modifican su aula. Copiar todo automáticamente arrastraría datos obsoletos; empezar siempre de cero obliga a repetir trabajo que ya estaba bien. Además, el horario de un curso no se puede reutilizar directamente porque pertenece a un `curso_escolar_id` concreto y su ciclo de vida es independiente.

## Decisión

- La copia es una **acción explícita**, nunca automática: `POST /horarios/{id}/copiar` con `cursoEscolarDestinoId`.
- Genera un **horario nuevo en `BORRADOR`** para el mismo profesor y el curso destino. No copia sesiones, asistencias, solicitudes, finalización ni historial.
- Copia solo las franjas que siguen siendo válidas en el destino:
  - la asignatura debe estar activa;
  - el profesor debe seguir asignado a la asignatura;
  - el centro y el curso deben pertenecer a la misma mancomunidad.
- Las franjas no válidas se omiten y se devuelven en `franjasOmitidas` con su motivo. Si no queda ninguna, responde `422` y no crea el horario.
- La copia no valida conflictos: el borrador se revisa y se valida al finalizar, como cualquier otro. Si ya existe un horario del profesor en el curso destino, responde `409`.
- El curso destino debe estar en `PLANIFICACION` o `ACTIVO`; un curso `CERRADO` responde `409`.
- Puede lanzarla el profesor titular del horario origen o cualquier administrador de la mancomunidad. La autoría queda en `creado_por` y `actualizado_por`.
- Se registra una fila `COPIA` en [[Historial de horario]] del horario nuevo, con `origen_horario_id` y el autor.
- `GET /horarios/{id}/copia?cursoEscolarDestinoId=` devuelve la previsualización con `franjasCopiables`, `franjasOmitidas` y si el destino ya tiene horario, sin escribir nada.
- La copia es el punto de partida, no una publicación: el horario copiado sigue el flujo normal `BORRADOR → FINALIZADO`.

## Consecuencias

### Positivas

- Se reutiliza el trabajo del curso anterior sin arrastrar asignaturas inactivas ni profesores que ya no imparten.
- El administrador y el profesor ven de antemano qué se copia y qué se omite.
- El nuevo horario conserva el aislamiento por curso y mancomunidad y se valida al finalizar.
- No se duplican sesiones ni solicitudes de un curso a otro.

### Negativas / compromisos

- Hay que mantener la previsualización y el recuento de omisiones.
- La copia puede requerir ajustes manuales si cambian aulas o grupos.
- No resuelve por sí sola el arrastre de matrículas, que es una acción distinta.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Copiar automáticamente al crear el curso | Arrastra franjas obsoletas y sorprende al profesor. |
| Copiar también sesiones y asistencias | Mezcla cursos y duplica datos históricos. |
| Empezar siempre de cero | Repite trabajo y desincentiva la planificación. |
| Reutilizar el mismo horario cambiando el curso | Rompe el histórico y la unicidad por curso. |
| Copiar sin previsualización | El usuario no sabe qué se ha omitido hasta después. |

## Relacionado

[[Horario del profesor]] · [[Curso escolar]] · [[Franja horaria]] · [[Historial de horario]] · [[Asignatura]] · [[API REST]] · [[Roadmap]] · [[ADR-004 - Horario gestionado por el profesor]] · [[ADR-016 - Horario editable por profesor y administrador]] · [[ADR-024 - Cierre y reapertura de curso escolar]]
