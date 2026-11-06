---
tipo: referencia
estado: activo
fase: 0
actualizado: 2026-11-04
tags:
  - api
  - ejemplos
  - referencia
---

# Ejemplos de API

> [!info] Para qué sirve esta nota
> Pares de petición y respuesta completos para cada módulo, con el fin de fijar el contrato antes de implementarlo. Las convenciones generales, la autenticación, la configuración, la asistencia y la reapertura de horarios están en [[API REST]].

## 1. Convenciones de los ejemplos

- Todas las rutas se sirven bajo `/api/v1`; aquí se muestran sin el prefijo.
- Los `201 Created` incluyen la cabecera `Location` con la ruta del recurso creado.
- Los recursos editables devuelven `version`; las actualizaciones la envían y un desajuste responde `409`.
- Los ejemplos usan datos ficticios y IDs de una misma mancomunidad.
- Un recurso de otra mancomunidad responde `404`, nunca `403`, para no revelar su existencia.

## 2. Usuarios

`POST /usuarios` crea la identidad global si el email no existe y añade la pertenencia en la mancomunidad activa.

```json
{
  "nombre": "Ana",
  "apellidos": "Ruiz",
  "email": "ana@example.com",
  "rol": "PROFESOR"
}
```

```json
{
  "id": 7,
  "nombre": "Ana",
  "apellidos": "Ruiz",
  "email": "ana@example.com",
  "activo": true,
  "pertenencia": {
    "mancomunidadId": 1,
    "rol": "PROFESOR",
    "activo": true
  },
  "version": 0
}
```

## 3. Centros

`POST /centros`

```json
{
  "nombre": "Centro A",
  "direccion": "Calle Mayor 1",
  "telefono": "910000000",
  "email": "centro@example.com"
}
```

```json
{
  "id": 12,
  "nombre": "Centro A",
  "direccion": "Calle Mayor 1",
  "telefono": "910000000",
  "email": "centro@example.com",
  "activo": true,
  "version": 0
}
```

`PATCH /centros/12` con una `version` desactualizada responde `409`:

```json
{
  "nombre": "Centro A",
  "version": 0
}
```

```json
{
  "type": "https://api.mancomunidad.local/errors/conflicto",
  "title": "Conflicto de versión",
  "status": 409,
  "detail": "El recurso ha sido modificado por otra persona",
  "instance": "/api/v1/centros/12",
  "traceId": "..."
}
```

## 4. Asignaturas

`POST /asignaturas` con una combinación inválida de tipo e instrumento responde `422`.

```json
{
  "centroId": 12,
  "nombre": "Coro",
  "curso": "1º",
  "grupo": "A",
  "tipo": "DANZA",
  "requiereInstrumento": true
}
```

```json
{
  "type": "https://api.mancomunidad.local/errors/regla-negocio",
  "title": "Regla de negocio incumplida",
  "status": 422,
  "detail": "Solo las asignaturas de música pueden requerir instrumento",
  "instance": "/api/v1/asignaturas",
  "traceId": "..."
}
```

## 5. Alumnos y matrículas

`POST /alumnos`

```json
{
  "nombre": "Lucía",
  "apellidos": "Gómez",
  "fechaNacimiento": "2014-05-12",
  "centroId": 12,
  "tutorNombre": "Marta Gómez",
  "tutorEmail": "marta@example.com",
  "tutorTelefono": "600000000"
}
```

```json
{
  "id": 40,
  "nombre": "Lucía",
  "apellidos": "Gómez",
  "fechaNacimiento": "2014-05-12",
  "centroId": 12,
  "activo": true,
  "instrumentos": [],
  "version": 0
}
```

`POST /alumnos/40/matriculas`

```json
{
  "asignaturaId": 8,
  "cursoEscolarId": 3
}
```

```json
{
  "id": 55,
  "alumnoId": 40,
  "asignaturaId": 8,
  "cursoEscolarId": 3,
  "estado": "ACTIVA",
  "version": 0
}
```

`PATCH /matriculas/55/baja` conserva el histórico y excluye al alumno de las listas futuras ([[ADR-026 - Baja de alumno a mitad de curso]]):

```json
{
  "bajaAt": "2026-11-04T10:00:00Z",
  "motivo": "Traslado de centro"
}
```

```json
{
  "id": 55,
  "estado": "BAJA",
  "bajaAt": "2026-11-04T10:00:00Z",
  "purgaAt": "2028-11-04T10:00:00Z",
  "purgadaAt": null,
  "version": 1
}
```

`PATCH /matriculas/55/reactivar` limpia las fechas de baja y purga y devuelve el alumno a las listas futuras:

```json
{
  "estado": "ACTIVA",
  "bajaAt": null,
  "purgaAt": null,
  "purgadaAt": null,
  "version": 2
}
```

Una matrícula que genere un **conflicto de alumno** contra un horario `FINALIZADO` responde `422` con el detalle del solape ([[ADR-021 - Validación de conflictos de alumno]]):

```json
{
  "type": "https://api.mancomunidad.local/errors/regla-negocio",
  "title": "Regla de negocio incumplida",
  "status": 422,
  "detail": "El alumno tendría dos clases a la misma hora",
  "instance": "/api/v1/alumnos/40/matriculas",
  "traceId": "...",
  "conflictos": [
    {
      "alumnoId": 40,
      "asignaturaId": 8,
      "diaSemana": "LUNES",
      "horaInicio": "16:00",
      "horaFin": "17:00",
      "horarioId": 21
    }
  ]
}
```

## 6. Horarios

`POST /horarios/21/franjas` crea una franja dentro de un horario en `BORRADOR`.

```json
{
  "asignaturaId": 8,
  "diaSemana": "LUNES",
  "horaInicio": "16:00",
  "horaFin": "17:00",
  "aula": "Aula 3"
}
```

```json
{
  "id": 77,
  "horarioId": 21,
  "asignaturaId": 8,
  "diaSemana": "LUNES",
  "horaInicio": "16:00",
  "horaFin": "17:00",
  "aula": "Aula 3",
  "creadoPor": { "id": 3, "nombre": "Ana" },
  "actualizadoPor": { "id": 3, "nombre": "Ana" },
  "version": 0
}
```

`GET /horarios/21/copia?cursoEscolarDestinoId=4` previsualiza el arrastre al curso siguiente sin escribir nada ([[ADR-023 - Copia de horario al curso siguiente]]):

```json
{
  "horarioId": 21,
  "cursoEscolarDestinoId": 4,
  "destinoYaTieneHorario": false,
  "franjasCopiables": 6,
  "franjasOmitidas": [
    {
      "franjaId": 79,
      "asignaturaId": 12,
      "motivo": "La asignatura está inactiva"
    }
  ]
}
```

`POST /horarios/21/copiar` crea el horario nuevo en `BORRADOR`; si no queda ninguna franja válida, responde `422`, y si el destino ya tiene horario para ese profesor, `409`:

```json
{
  "cursoEscolarDestinoId": 4
}
```

```json
{
  "id": 34,
  "profesorId": 7,
  "cursoEscolarId": 4,
  "estado": "BORRADOR",
  "franjasCopiadas": 6,
  "franjasOmitidas": 1,
  "origenHorarioId": 21,
  "version": 0
}
```

## 7. Solicitudes de horario

`POST /solicitudes-horario/9/resolver`. El cuerpo envía `decision` (`APROBAR` o `RECHAZAR`) y una respuesta opcional para el profesor.

```json
{
  "decision": "APROBAR",
  "respuestaAdmin": "Se cambia al aula 5"
}
```

```json
{
  "id": 9,
  "estado": "APROBADA",
  "respuestaAdmin": "Se cambia al aula 5",
  "resueltaPor": { "id": 3, "nombre": "Ana" },
  "resueltaAt": "2026-11-03T10:15:00Z"
}
```

## 8. Observaciones

`POST /observaciones`

```json
{
  "alumnoId": 40,
  "asignaturaId": 8,
  "tipo": "ACADEMICA",
  "texto": "Ha mejorado la lectura a primera vista.",
  "fecha": "2026-11-03"
}
```

```json
{
  "id": 120,
  "alumnoId": 40,
  "profesorId": 7,
  "asignaturaId": 8,
  "cursoEscolarId": 3,
  "tipo": "ACADEMICA",
  "texto": "Ha mejorado la lectura a primera vista.",
  "fecha": "2026-11-03",
  "version": 0
}
```

Si se omite `cursoEscolarId`, el servicio lo infiere de `fecha`. Cuando la fecha no cae en un único curso, responde `422` y exige indicarlo ([[ADR-018 - Curso escolar en las observaciones]]):

```json
{
  "type": "https://api.mancomunidad.local/errors/regla-negocio",
  "title": "Regla de negocio incumplida",
  "status": 422,
  "detail": "Indica cursoEscolarId: la fecha no pertenece a un único curso",
  "instance": "/api/v1/observaciones",
  "traceId": "..."
}
```

## 9. Preparaciones

`POST /preparaciones`

```json
{
  "asignaturaId": 8,
  "cursoEscolarId": 3,
  "fecha": "2026-11-10",
  "titulo": "Lectura rítmica",
  "objetivos": "Trabajar el compás de 6/8",
  "contenido": "Ejercicios 1 y 2",
  "estado": "BORRADOR"
}
```

```json
{
  "id": 31,
  "profesorId": 7,
  "asignaturaId": 8,
  "cursoEscolarId": 3,
  "fecha": "2026-11-10",
  "titulo": "Lectura rítmica",
  "estado": "BORRADOR",
  "version": 0
}
```

## 10. Plataforma

`POST /plataforma/mancomunidades` crea la mancomunidad, su curso inicial y el primer administrador en una transacción.

```json
{
  "nombre": "Mancomunidad B",
  "codigo": "MB",
  "tipo": "IDIOMAS",
  "duracionMaximaFranjaMin": 90,
  "primerAdministrador": {
    "nombre": "Carlos",
    "apellidos": "Pérez",
    "email": "carlos@example.com"
  }
}
```

```json
{
  "id": 2,
  "nombre": "Mancomunidad B",
  "codigo": "MB",
  "tipo": "IDIOMAS",
  "duracionMaximaFranjaMin": 90,
  "estado": "ACTIVA",
  "cursoInicial": {
    "id": 4,
    "codigo": "2026-2027",
    "estado": "ACTIVO"
  },
  "administrador": {
    "usuarioId": 9,
    "rol": "ADMINISTRADOR"
  },
  "version": 0
}
```

## 11. Aislamiento entre mancomunidades

Con un token de la mancomunidad B, `GET /alumnos/40` —que pertenece a la mancomunidad A— responde `404` con el mismo cuerpo que un recurso inexistente:

```json
{
  "type": "https://api.mancomunidad.local/errors/no-encontrado",
  "title": "Recurso no encontrado",
  "status": 404,
  "detail": "El recurso no existe",
  "instance": "/api/v1/alumnos/40",
  "traceId": "..."
}
```

La respuesta no distingue entre «no existe» y «es de otra mancomunidad». Lo mismo aplica a escrituras, también para el administrador.

## 12. Días no lectivos

`POST /dias-no-lectivos` con `centroId` nulo marca la fecha para toda la mancomunidad y cancela las sesiones `PROGRAMADA` sin asistencia de su ámbito ([[ADR-017 - Cancelación de sesiones y días no lectivos]]):

```json
{
  "cursoEscolarId": 3,
  "fecha": "2026-12-08",
  "centroId": null,
  "nombre": "Festivo local"
}
```

```json
{
  "id": 14,
  "cursoEscolarId": 3,
  "fecha": "2026-12-08",
  "centroId": null,
  "nombre": "Festivo local",
  "sesionesCanceladas": 6,
  "version": 0
}
```

Un día duplicado para el mismo ámbito responde `409`:

```json
{
  "type": "https://api.mancomunidad.local/errors/conflicto",
  "title": "Día no lectivo duplicado",
  "status": 409,
  "detail": "Ya existe un día no lectivo para esa fecha y ámbito",
  "instance": "/api/v1/dias-no-lectivos",
  "traceId": "..."
}
```

`POST /cursos-escolares/3/cerrar` no borra datos: cancela las sesiones `PROGRAMADA` sin asistencia y congela horarios y matrículas ([[ADR-024 - Cierre y reapertura de curso escolar]]):

```json
{
  "sesionesCanceladas": 12,
  "horariosCongelados": 8,
  "estado": "CERRADO"
}
```

`POST /cursos-escolares/3/reabrir` exige motivo y, si hay otro curso activo, lo cierra en la misma transacción:

```json
{
  "motivo": "Se reabre para corregir el cierre anticipado del trimestre"
}
```

```json
{
  "id": 3,
  "estado": "ACTIVO",
  "cursoCerradoEnMismaTransaccion": 4,
  "version": 3
}
```

## 13. Sesiones y asistencia

`POST /sesiones/301/cancelar` cancela una sesión `PROGRAMADA` y exige un motivo de 10–500 caracteres:

```json
{
  "motivo": "El profesor está de baja"
}
```

```json
{
  "id": 301,
  "estado": "CANCELADA",
  "canceladaPor": { "id": 7, "nombre": "Ana" },
  "canceladaAt": "2026-11-03T09:00:00Z",
  "motivoCancelacion": "El profesor está de baja",
  "version": 1
}
```

Intentar guardar la lista en una sesión cancelada responde `409`:

```json
{
  "type": "https://api.mancomunidad.local/errors/conflicto",
  "title": "Estado incompatible",
  "status": 409,
  "detail": "Una sesión cancelada no admite asistencias",
  "instance": "/api/v1/sesiones/301/asistencias",
  "traceId": "..."
}
```

`GET /asistencias/resumen?alumnoId=40&asignaturaId=8&cursoEscolarId=3&historico=true` devuelve los contadores en vivo y, si la matrícula ya fue purgada, los anonimizados de [[Resumen de asistencia]]:

```json
{
  "alumnoId": 40,
  "asignaturaId": 8,
  "cursoEscolarId": 3,
  "sesiones": 28,
  "presentes": 24,
  "ausentes": 2,
  "ausenciasJustificadas": 1,
  "retrasos": 1,
  "minutosRetraso": 10,
  "historico": true
}
```

## 14. Sustituciones

`POST /sustituciones` crea una sustitución y reasigna las sesiones `PROGRAMADA` sin lista del periodo ([[ADR-025 - Sustituciones y cambio de titular]]):

```json
{
  "asignaturaId": 8,
  "cursoEscolarId": 3,
  "profesorTitularId": 7,
  "profesorSustitutoId": 15,
  "desde": "2026-11-10",
  "hasta": "2026-11-21",
  "motivo": "Baja médica del titular"
}
```

```json
{
  "id": 6,
  "asignaturaId": 8,
  "cursoEscolarId": 3,
  "profesorTitularId": 7,
  "profesorSustitutoId": 15,
  "desde": "2026-11-10",
  "hasta": "2026-11-21",
  "motivo": "Baja médica del titular",
  "estado": "ACTIVA",
  "sesionesReasignadas": 4,
  "version": 0
}
```

Un sustituto con un solape en su horario `FINALIZADO` responde `422`:

```json
{
  "type": "https://api.mancomunidad.local/errors/regla-negocio",
  "title": "Regla de negocio incumplida",
  "status": 422,
  "detail": "El sustituto no está disponible en todas las franjas",
  "instance": "/api/v1/sustituciones",
  "traceId": "...",
  "conflictos": [
    {
      "diaSemana": "MARTES",
      "horaInicio": "16:00",
      "horaFin": "17:00",
      "horarioId": 29
    }
  ]
}
```

`POST /sustituciones/6/anular` revierte las sesiones `PROGRAMADA` futuras y conserva las ya abiertas:

```json
{
  "motivo": "El titular se reincorpora antes de lo previsto"
}
```

```json
{
  "id": 6,
  "estado": "ANULADA",
  "anuladaPor": { "id": 3, "nombre": "Ana" },
  "anuladaAt": "2026-11-18T08:00:00Z",
  "motivoAnulacion": "El titular se reincorpora antes de lo previsto",
  "version": 1
}
```

## 15. Seguridad y sesiones

`POST /auth/verificar-email` confirma la dirección del alta o del cambio. El token solo viaja en el correo y aquí se envía en el cuerpo:

```json
{
  "token": "v1_7f3c…"
}
```

```json
{
  "email": "ana@example.com",
  "emailVerificado": true,
  "cambioAplicado": false
}
```

`PATCH /auth/email` inicia un cambio de dirección; no lo aplica hasta confirmar el enlace enviado a la dirección nueva. Exige la contraseña actual:

```json
{
  "password": "ClaveActual1",
  "emailNuevo": "ana.ruiz@example.com"
}
```

```json
{
  "emailActual": "ana@example.com",
  "emailPendiente": "ana.ruiz@example.com",
  "expiraAt": "2026-11-05T10:00:00Z",
  "mensaje": "Confirma la dirección nueva desde el correo recibido"
}
```

Un refresh ya rotado se considera reutilización: se revoca la familia completa y se responde `401` genérico ([[ADR-030 - Detección de reutilización de refresh tokens]]):

```json
{
  "type": "https://api.mancomunidad.local/errors/no-autorizado",
  "title": "Sesión no válida",
  "status": 401,
  "detail": "Vuelve a iniciar sesión",
  "instance": "/api/v1/auth/refresh",
  "traceId": "..."
}
```

Un cubo de rate limiting agotado responde `429` con `Retry-After` y sin revelar si la cuenta existe ([[ADR-032 - Rate limiting de extremos sensibles]]):

```http
HTTP/1.1 429 Too Many Requests
Retry-After: 900
X-RateLimit-Limit: 3
X-RateLimit-Remaining: 0
X-RateLimit-Reset: 1762333200
Content-Type: application/problem+json
```

```json
{
  "type": "https://api.mancomunidad.local/errors/demasiadas-peticiones",
  "title": "Demasiadas peticiones",
  "status": 429,
  "detail": "Espera antes de volver a intentarlo",
  "instance": "/api/v1/auth/solicitar-recuperacion",
  "traceId": "..."
}
```

`POST /alumnos/40/matriculas` con `Idempotency-Key: 8f14e45f-ea4b-4c1e-9d6f-2b7a1c0d9e11`. Si la respuesta se pierde y se reintenta con la misma clave y cuerpo, devuelve la respuesta guardada sin duplicar la matrícula ([[ADR-033 - Idempotencia de peticiones sensibles]]):

```http
HTTP/1.1 201 Created
Location: /api/v1/matriculas/55
Idempotent-Replay: true
Content-Type: application/json
```

```json
{
  "id": 55,
  "alumnoId": 40,
  "asignaturaId": 8,
  "cursoEscolarId": 3,
  "estado": "ACTIVA",
  "version": 0
}
```

Reutilizar la clave con otro cuerpo o en otra ruta responde `409`:

```json
{
  "type": "https://api.mancomunidad.local/errors/conflicto",
  "title": "Clave de idempotencia reutilizada",
  "status": 409,
  "code": "CLAVE_IDEMPOTENCIA_REUTILIZADA",
  "detail": "La clave ya se usó para otra operación",
  "instance": "/api/v1/alumnos/40/matriculas",
  "traceId": "..."
}
```

`GET /auth/sesiones` muestra las familias activas sin tokens. La sesión actual se marca con `actual: true`:

```json
{
  "sesiones": [
    {
      "familiaId": "6c1f0f1e-6b0f-4a7e-9f2a-1d4f0b8c9e21",
      "creadaAt": "2026-11-04T08:30:00Z",
      "ultimoUsoAt": "2026-11-04T09:10:00Z",
      "ip": "192.0.2.10",
      "userAgent": "Mozilla/5.0 …",
      "actual": true
    },
    {
      "familiaId": "b2a9c7d4-2f6e-4c3a-8e51-7a0d9c1b4f33",
      "creadaAt": "2026-11-03T18:00:00Z",
      "ultimoUsoAt": "2026-11-03T19:45:00Z",
      "ip": "198.51.100.7",
      "userAgent": "Mozilla/5.0 …",
      "actual": false
    }
  ],
  "maximo": 5
}
```

`POST /auth/logout-all` exige la contraseña actual, revoca todas las familias e incrementa `tokenVersion`; responde `204`:

```json
{
  "password": "ClaveActual1"
}
```

El bootstrap del primer superadministrador no es un endpoint. Se ejecuta una vez desde el despliegue y la contraseña se lee de un fichero secreto o de la consola ([[ADR-034 - Bootstrap del primer superadministrador]]):

```bash
java -jar app.jar --bootstrap-superadmin
```

```text
Superadministrador creado: admin@example.com
Cambio de contraseña obligatorio en la primera entrada.
```

## Relacionado

[[API REST]] · [[Multi-tenencia]] · [[Seguridad y autenticación]] · [[Modelo de datos]] · [[Requisitos funcionales]] · [[Día no lectivo]] · [[Resumen de asistencia]] · [[Sustitución]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-015 - Tipos de enseñanza y módulo de instrumentos opcional]] · [[ADR-016 - Horario editable por profesor y administrador]] · [[ADR-017 - Cancelación de sesiones y días no lectivos]] · [[ADR-018 - Curso escolar en las observaciones]] · [[ADR-019 - Resumen de asistencia y purga anonimizada]] · [[ADR-020 - Borrado de franjas y conservación de sesiones]] · [[ADR-021 - Validación de conflictos de alumno]] · [[ADR-022 - Conflictos de profesor entre mancomunidades]] · [[ADR-023 - Copia de horario al curso siguiente]] · [[ADR-024 - Cierre y reapertura de curso escolar]] · [[ADR-025 - Sustituciones y cambio de titular]] · [[ADR-026 - Baja de alumno a mitad de curso]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-033 - Idempotencia de peticiones sensibles]] · [[ADR-034 - Bootstrap del primer superadministrador]]
