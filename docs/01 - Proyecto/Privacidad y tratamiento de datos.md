---
tipo: documento
estado: borrador
fase: 0
actualizado: 2026-11-04
tags:
  - proyecto
  - privacidad
  - legal
---

# Privacidad y tratamiento de datos

> [!info] Alcance
> Nota de trabajo para el bloque legal de la Fase 0. Se completa y valida antes de la preproducción, junto con PEN-36 a PEN-40. No es asesoramiento jurídico: debe revisarse con el responsable del tratamiento.

## 1. Roles y responsabilidades

- **Responsable del tratamiento:** cada mancomunidad (y, para el panel de plataforma, el titular de la plataforma) respecto de los datos de sus usuarios, alumnos y profesorado.
- **Encargado del tratamiento:** el titular de la plataforma respecto de la infraestructura, la base de datos compartida y las copias de seguridad, en la medida en que trate datos por cuenta de las mancomunidades.
- El reparto se documenta en el contrato de encargado del tratamiento (PEN-38).
- El administrador de mancomunidad es el contacto habitual para derechos de los interesados dentro de su tenant; la plataforma solo interviene para soporte técnico y auditoría.

## 2. Finalidad y base jurídica

| Finalidad | Datos | Base jurídica |
| --- | --- | --- |
| Gestión académica de la mancomunidad (centros, asignaturas, horarios, matrículas) | Datos de identificación de alumnos y profesores, datos académicos | Ejecución de un contrato / misión de interés público (enseñanza) |
| Control de asistencia y registro de jornada del profesorado | Asistencia, fichajes | Obligación legal (art. 34.9 ET) y misión de interés público |
| Gestión de cuentas y autenticación | Email, contraseña hasheada, pertenencias, logs de acceso | Ejecución del contrato y seguridad de la información |
| Recuperación de contraseña y verificación de email | Email, tokens de un solo uso | Ejecución del contrato |
| Soporte de cuentas por la plataforma | Datos de cuenta y pertenencias | Interés legítimo y ejecución del contrato |
| Copias de seguridad y exportación por tenant | Todos los datos del tenant | Obligación legal y seguridad de la información |

No se tratan datos para marketing ni se ceden a terceros con fines comerciales.

## 3. Categorías de datos

- **Identificación:** nombre, apellidos, email, centro.
- **Académicos:** asignaturas, matrículas, horarios, observaciones, asistencia, preparaciones.
- **Técnicos:** logs de acceso (IP, fecha), eventos de seguridad, sesiones de refresh, auditoría de plataforma.
- **Menores:** datos de alumnos menores de edad y de sus tutores, con las restricciones de la sección 6.
- **Jornada laboral:** fichajes de entrada y salida del profesorado.

No se tratan categorías especiales del art. 9 RGPD salvo que una mancomunidad las incorpore expresamente; en ese caso se añade un anexo.

## 4. Registro de actividades de tratamiento

Se mantiene un registro de las actividades de tratamiento con, al menos:

- nombre y datos de contacto del responsable y del encargado;
- finalidades;
- categorías de interesados y de datos;
- destinatarios y transferencias;
- plazos de conservación (sección 5);
- medidas de seguridad técnicas y organizativas ([[Seguridad y autenticación]], [[Multi-tenencia]]).

El registro se actualiza cuando cambia una finalidad o un tratamiento.

## 5. Conservación y supresión

Los plazos están en [[Requisitos no funcionales]] (DAT-04, DAT-14 a DAT-18, DAT-22 a DAT-24) y en [[Modelo de datos]]:

| Dato | Plazo |
| --- | --- |
| Alumnos y matrículas | Mientras la matrícula esté `ACTIVA` y 2 años desde la baja; después purga del historial y la asistencia, conservando contadores anonimizados. |
| Observaciones y preparaciones | Conservación académica; no se purgan automáticamente en la v1. |
| Historial de asistencia | Mientras la matrícula esté `ACTIVA` y 2 años desde la baja. |
| Fichajes de jornada | 4 años. |
| Cuentas y pertenencias | No se borran físicamente; se desactivan para conservar la autoría. |
| Tokens y sesiones | Purga por plazo (30 días revocados/caducados). |
| Eventos de acceso | 30 días. |
| Auditoría de plataforma | 2 años. |
| Logs de aplicación | 30 días. |
| Copias de seguridad | 30 días diarias + 12 mensuales. |

La supresión anticipada se gestiona como derecho del interesado (sección 7).

## 6. Datos de menores

- El sistema gestiona datos de alumnos menores de edad y de sus tutores legales.
- El alta de un alumno menor requiere los datos del tutor y su relación.
- El acceso a los datos de menores está restringido por rol y por necesidad; el profesor solo ve los alumnos de sus asignaturas.
- No se permite la búsqueda de menores desde el panel de plataforma; el `SUPERADMIN` no accede a datos académicos.
- La información a las familias y el consentimiento se gestionan fuera del sistema, por la mancomunidad; la aplicación solo almacena lo necesario para la gestión académica.
- Los tutores pueden ejercer los derechos del menor conforme a la sección 7.

## 7. Derechos del interesado

Se garantizan los derechos de acceso, rectificación, supresión, limitación, portabilidad y oposición, con los plazos legales:

- La solicitud se canaliza por la mancomunidad responsable; la plataforma apoya técnicamente.
- La rectificación se puede hacer desde el panel administrativo o mediante soporte con motivo.
- La supresión no puede borrar datos que deban conservarse por obligación legal (asistencia, jornada); en ese caso se responde con el motivo y el plazo.
- La portabilidad se cubre con el script de exportación lógica por mancomunidad (filtrado por `mancomunidad_id`), sin hashes ni tokens.
- Se registra cada solicitud y su resolución en la auditoría de plataforma cuando interviene el soporte.

## 8. Medidas de seguridad y brechas

- Las medidas técnicas y organizativas se describen en [[Seguridad y autenticación]], [[Multi-tenencia]] y [[Copias de seguridad y exportación]].
- Toda brecha de seguridad se notificará a la autoridad de control y a los interesados en los plazos legales, y se documentará internamente.
- Los incidentes de seguridad se registran en `evento_seguridad` y en la auditoría de plataforma.

## 9. Evaluación de impacto (EIPD)

Se realizará una evaluación de impacto cuando el tratamiento pueda entrañar un alto riesgo, en particular por los datos de menores y por el registro de jornada. La EIPD se revisa antes de producción y cuando cambie el alcance.

## 10. Transferencias y subencargados

- Los datos se alojan en la infraestructura elegida por la plataforma o por la mancomunidad, dentro del EEE salvo que se documente una transferencia internacional.
- Los subencargados (proveedor de correo, de infraestructura) se listan en el contrato de encargado.
- No se usan servicios de analítica ni de publicidad sobre los datos personales.

## 11. Contrato de encargado del tratamiento

Reparto de responsabilidades entre la plataforma (encargado) y cada mancomunidad (responsable):

| Aspecto | Mancomunidad (responsable) | Plataforma (encargado) |
| --- | --- | --- |
| Finalidad y base jurídica | Decide la finalidad académica y la informa a los interesados. | Trata por cuenta de la mancomunidad según instrucciones. |
| Datos personales | Titular de los datos de sus alumnos, profesores y tutores. | No es titular; no usa los datos para fines propios. |
| Alta y baja de usuarios | Decide quién accede y con qué rol. | Implementa la cuenta y la pertenencia. |
| Acceso a datos académicos | Administración de la mancomunidad. | Sin acceso en la v1; solo soporte de cuentas. |
| Soporte de cuentas | Solicita reset, activación o cambio de rol con motivo. | Ejecuta con auditoría, sin ver la contraseña. |
| Copias de seguridad | Responsable de decidir la retención y la restauración de su tenant. | Ejecuta las copias cifradas y la restauración en staging. |
| Brechas | Informa a los interesados y a la autoridad. | Notifica a la mancomunidad y colabora en la investigación. |
| Subencargados | Autoriza los proveedores listados. | Contrata y supervisa infraestructura, correo y almacenamiento. |
| Derechos del interesado | Canaliza las solicitudes y responde. | Apoya técnicamente y ejecuta exportaciones. |
| Eliminación al terminar el servicio | Decide qué conservar por obligación legal. | Entrega o destruye los datos y las copias según instrucciones. |

- El contrato se firma antes de la producción y se revisa cuando cambie el alcance o un subencargado.
- La plataforma no puede usar los datos para fines distintos de los instruidos ni cederlos a terceros.
- Las medidas de seguridad técnicas se describen en [[Seguridad y autenticación]] y [[Copias de seguridad y exportación]].

## Relacionado

[[Requisitos no funcionales]] · [[Seguridad y autenticación]] · [[Multi-tenencia]] · [[Copias de seguridad y exportación]] · [[Modelo de datos]] · [[Roles y permisos]] · [[Usuario]] · [[Alumno]] · [[Control horario]] · [[Pendientes]]