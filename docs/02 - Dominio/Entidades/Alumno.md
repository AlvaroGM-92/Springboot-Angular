---
tipo: entidad
estado: activo
fase: 1
actualizado: 2026-11-04
tags:
  - dominio
  - entidad
  - multi-tenant
---

# Alumno

## Propósito

Persona matriculada en una o varias asignaturas de un centro. Guarda datos personales, contacto del tutor e instrumentos que practica; los instrumentos solo se muestran si el módulo está activo en la mancomunidad.

## Campos

| Campo | Tipo | Obligatorio | Reglas |
| --- | --- | --- | --- |
| `id` | BIGINT | Sí | Clave primaria. |
| `mancomunidad_id` | BIGINT | Sí | FK a [[Mancomunidad]]; tenant del alumno. |
| `centro_id` | BIGINT | Sí | FK a [[Centro]] activo de la misma mancomunidad. |
| `nombre` | VARCHAR(100) | Sí | 2–100 caracteres. |
| `apellidos` | VARCHAR(150) | Sí | 2–150 caracteres. |
| `fecha_nacimiento` | DATE | No | No puede ser futura. |
| `tutor_nombre` | VARCHAR(150) | No | Contacto principal. |
| `tutor_email` | VARCHAR(150) | No | Formato válido si se informa. |
| `tutor_telefono` | VARCHAR(30) | No | Formato flexible. |
| `activo` | BOOLEAN | Sí | Por defecto `true`. |
| `created_at` | DATETIME | Sí | Auditoría. |
| `updated_at` | DATETIME | Sí | Auditoría. |

## Relaciones

- **N:1 con [[Mancomunidad]]**.
- **N:1 con [[Centro]]**.
- **N:M con [[Asignatura]]** a través de [[Matrícula]].
- **N:M con [[Instrumento]]** a través de `alumno_instrumento`.
- **1:N con [[Observación]]**.
- **1:N con [[Asistencia]]**.

## Reglas

- Un alumno pertenece a un único centro de su mancomunidad.
- **El alumno es local al tenant:** no tiene identidad global ni se deduplica entre mancomunidades. La misma persona en dos mancomunidades son dos registros independientes y no existe búsqueda de menores desde la plataforma ([[ADR-009 - Multi-tenant con mancomunidad e identidad global]]).
- El centro y los instrumentos deben pertenecer a la misma mancomunidad que el alumno.
- No se puede cambiar de centro mientras tenga matrículas activas.
- No se borra físicamente: se desactiva.
- No se puede desactivar un alumno mientras tenga matrículas `ACTIVA`; primero se dan de baja ([[ADR-026 - Baja de alumno a mitad de curso]]).
- La baja de una matrícula no desactiva al alumno ni borra sus datos personales; son decisiones independientes.
- Un alumno inactivo conserva matrículas, observaciones y asistencias como histórico.
- Los instrumentos se normalizan y se reutilizan del catálogo.
- Un alumno puede tener entre 0 y 10 instrumentos.
- La sección de instrumentos solo se muestra y se acepta en la API si el módulo está activo; si no, se conserva lo que ya hubiera guardado sin borrarlo ([[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]]).
- **Datos de menores:** la fecha de nacimiento es opcional pero, si se informa, no puede ser futura. Los datos del tutor (`tutor_nombre`, `tutor_email`, `tutor_telefono`) son el canal de contacto con la familia y se tratan como datos personales del menor.
- La mancomunidad es responsable de recabar el consentimiento y de informar a las familias; la aplicación solo almacena los datos necesarios para la gestión académica y no envía comunicaciones a las familias en la v1.
- Los tutores pueden ejercer los derechos del menor (acceso, rectificación, supresión, portabilidad) a través de la mancomunidad, que canaliza la solicitud al soporte cuando hace falta ([[Privacidad y tratamiento de datos]]).

## Dudas

- ¿Se guarda el DNI/NIE? No en la v1.
- ¿Se guarda una segunda persona de contacto? Fuera de alcance inicial.
- ¿Se permite un alumno sin tutor? Sí, los datos del tutor son opcionales.
- ¿Se informa a las familias desde la aplicación? No en la v1; la comunicación la gestiona la mancomunidad.

## Relacionado

[[Mancomunidad]] · [[Multi-tenencia]] · [[Centro]] · [[Matrícula]] · [[Instrumento]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]] · [[ADR-026 - Baja de alumno a mitad de curso]] · [[Observación]] · [[Asistencia]] · [[Modelo de datos]] · [[Privacidad y tratamiento de datos]]
