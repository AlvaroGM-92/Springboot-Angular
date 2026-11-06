---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - horarios
  - sesiones
  - profesorado
---

# ADR-025 — Sustituciones y cambio de titular

## Contexto

El [[Horario del profesor]] pertenece al profesor titular y la [[Sesión de clase]] guarda un `profesor_id`. Cuando un docente se ausenta una temporada, no había forma de reflejar quién imparte realmente sus clases: o se cambiaba el titular del horario, perdiendo la autoría y el histórico, o se dejaban las sesiones a nombre de quien no las imparte. También faltaba un periodo explícito que acotara la sustitución y que permitiera volver al titular sin rehacer el horario.

## Decisión

- Se crea la entidad [[Sustitución]], siempre dentro de una mancomunidad y de un [[Curso escolar]], con `asignatura_id`, `profesor_titular_id`, `profesor_sustituto_id`, `desde`, `hasta` y `motivo` de 10–500 caracteres. Las fechas son inclusivas y deben caer dentro del curso.
- La sustitución **no cambia el horario**: el [[Horario del profesor]] y sus [[Franja horaria]] siguen perteneciendo al titular. La sustitución afecta a las **sesiones** de esa asignatura en el periodo.
- Solo la administración crea, edita y anula sustituciones. El profesor titular y el sustituto las consultan, pero no las modifican.
- El sustituto debe tener pertenencia activa `PROFESOR` en la misma mancomunidad, no puede ser el titular y no puede tener otra sustitución solapada ni una franja de un horario `FINALIZADO` que se pise con las franjas de la asignatura sustituida. Los conflictos se devuelven como `422` con `conflictos`.
- No puede haber dos sustituciones solapadas para la misma asignatura y curso.
- **Al crear la sustitución**, las sesiones `PROGRAMADA` del periodo que aún no tienen lista se reasignan al sustituto y guardan `sustitucion_id`. Las sesiones `ABIERTA` o `CERRADA` no se tocan: conservan al profesor que las impartió.
- **En la generación diaria**, si la fecha cae dentro de una sustitución `ACTIVA`, la sesión nace con `profesor_id = profesor_sustituto_id` y `sustitucion_id` informado. Si no, nace con el titular.
- **Al anular o acortar** una sustitución, las sesiones `PROGRAMADA` futuras sin lista vuelven al titular y limpian `sustitucion_id`. Las sesiones ya abiertas o cerradas se conservan tal cual.
- La sustitución tiene estado `ACTIVA` o `ANULADA`. Anular exige motivo de 10–500 caracteres y no borra la fila. Acortar el periodo se hace editando `hasta`; no se elimina el histórico.
- El sustituto puede abrir, cerrar y pasar lista en las sesiones del periodo; el titular las ve en solo lectura. La administración sigue pudiendo cancelar sesiones `PROGRAMADA`, pero no abrirlas ni cerrarlas.
- El titular conserva su horario y puede consultar la sustitución y las sesiones afectadas, pero no las edita mientras esté vigente.
- Si el sustituto pierde la pertenencia `PROFESOR`, la sustitución debe anularse antes de desactivar la pertenencia. Si no se hace, la generación de sesiones del periodo falla con un error de configuración visible en lugar de crear clases sin profesor válido.
- Todo cambio queda con `creado_por`, `actualizado_por`, `anulada_por` y fechas. No se usa la auditoría de plataforma porque son datos académicos del tenant.

## Consecuencias

### Positivas

- Se sabe quién imparte realmente cada sesión sin romper la titularidad del horario.
- El periodo de sustitución es explícito y reversible, y no obliga a rehacer franjas.
- El titular conserva su histórico y el sustituto no hereda un horario que no le pertenece.
- Las sesiones ya impartidas no cambian de profesor a posteriori.

### Negativas / compromisos

- Hay que validar la disponibilidad del sustituto contra su horario finalizado y sus otras sustituciones.
- La reasignación de sesiones `PROGRAMADA` es una escritura masiva dentro del periodo.
- El titular no puede operar sus sesiones durante la sustitución, aunque siga siendo el dueño del horario.
- Cambiar de titular a mitad de curso exige cerrar una sustitución y abrir otra, no editar la existente.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Cambiar `horario_profesor.profesor_id` al sustituto | Pierde la titularidad, el histórico y la autoría del horario. |
| Crear un horario paralelo para el sustituto | Duplica franjas y rompe la unicidad por profesor y curso. |
| Cambiar solo `sesion_clase.profesor_id` sin entidad | No hay periodo, motivo ni forma de revertir; se pierde la traza. |
| Modelar la sustitución como una ausencia del profesor | No dice qué asignaturas cubre ni quién las imparte. |
| Permitir que el profesor cree su propia sustitución | La cobertura es una decisión organizativa de la administración. |

## Relacionado

[[Sustitución]] · [[Asignatura]] · [[Curso escolar]] · [[Horario del profesor]] · [[Franja horaria]] · [[Sesión de clase]] · [[Usuario]] · [[UsuarioMancomunidad]] · [[Modelo de datos]] · [[API REST]] · [[Roadmap]] · [[ADR-013 - Generación automática de sesiones desde el horario]] · [[ADR-016 - Horario editable por profesor y administrador]]
