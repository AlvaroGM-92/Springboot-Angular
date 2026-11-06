---
tipo: adr
estado: aceptada
fecha: 2026-11-03
actualizado: 2026-11-03
tags:
  - adr
  - horarios
  - multi-tenant
  - conflictos
---

# ADR-022 — Conflictos de profesor entre mancomunidades

## Contexto

La identidad de [[Usuario]] es global y una misma persona puede ser profesor en dos mancomunidades. El horario y las franjas están aislados por `mancomunidad_id`, y la validación `HOR-04` solo impide solapamientos **dentro** de la mancomunidad activa. Nada impide que la misma persona tenga dos franjas simultáneas en dos tenants distintos.

## Decisión

- **No se validan** los solapamientos de un mismo profesor entre mancomunidades distintas en la v1.
- La validación de `HOR-04` sigue siendo obligatoria dentro de cada mancomunidad.
- El motivo es el aislamiento: una consulta de franjas de otra mancomunidad rompería la regla de que toda consulta de negocio se filtra por el `mancomunidad_id` del token, revelaría horarios de otro tenant y obligaría a cruzar datos que cada mancomunidad gestiona por separado.
- Cada mancomunidad desconoce la existencia de las otras y no puede arbitrar la prioridad de un profesor compartido. La persona ve sus dos horarios y es responsable de su disponibilidad.
- La limitación se documenta en [[Multi-tenencia]], [[Franja horaria]] y [[Horario del profesor]], y se prueba que el sistema no hace consultas cruzadas.
- Si en el futuro se necesita, se hará con un mecanismo explícito y consentido: una declaración de disponibilidad global del profesor, una API de plataforma o una comprobación opt-in entre mancomunidades. No se añadirá una consulta cruzada silenciosa.

## Consecuencias

### Positivas

- Se mantiene intacto el aislamiento por mancomunidad.
- No se exponen horarios ni datos de un tenant a otro.
- La regla es simple y no añade latencia ni dependencias entre tenants.
- Cada mancomunidad conserva la autonomía sobre su horario.

### Negativas / compromisos

- Un profesor compartido puede tener dos clases a la misma hora en mancomunidades distintas y el sistema no lo detecta.
- La administración de cada tenant debe coordinarse fuera del sistema.
- Si el problema crece, habrá que diseñar una solución de plataforma con consentimiento y auditoría.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Consultar franjas de todos los tenants del usuario | Rompe el aislamiento y revela horarios de otras mancomunidades. |
| Guardar una disponibilidad global del profesor | Añade un plano de datos global y un consentimiento que la v1 no tiene. |
| Bloquear el alta del profesor en dos mancomunidades | Impide un caso legítimo y frecuente; el problema es el solapamiento, no la pertenencia múltiple. |
| Avisar sin bloquear al crear la franja | Requiere la consulta cruzada que se quiere evitar. |

## Relacionado

[[Multi-tenencia]] · [[Franja horaria]] · [[Horario del profesor]] · [[Usuario]] · [[UsuarioMancomunidad]] · [[Modelo de datos]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-021 - Validación de conflictos de alumno]]
