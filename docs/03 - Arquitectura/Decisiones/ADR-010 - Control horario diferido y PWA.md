---
tipo: adr
estado: aceptada
fecha: 2026-10-28
actualizado: 2026-10-28
tags:
  - adr
  - fichaje
  - movil
  - pwa
  - legal
---

# ADR-010 — Control horario fuera de la v1 y distribución como PWA

## Contexto

Los profesores imparten clase en varios centros de la mancomunidad y necesitan **registrar el inicio y el fin de su jornada laboral**. La administración quiere consultar esos registros para resolver dudas de cobertura y detectar incidencias.

El registro de jornada no es una funcionalidad opcional: el artículo 34.9 del Estatuto de los Trabajadores (introducido por el RD-ley 8/2019) obliga a registrar **diariamente** la hora de inicio y de fin, conservar el registro **4 años** y tenerlo a disposición de la persona trabajadora, la Inspección de Trabajo y la Seguridad Social. Eso incluye el derecho de cada profesor a consultar su propio historial.

La idea inicial era publicar aplicaciones simples para iOS y Android. La distribución resulta ser el punto débil: iOS **no permite instalar una aplicación desde un fichero** descargado de Drive o similar; las vías reales son App Store, TestFlight (builds de 90 días), Ad Hoc (100 dispositivos al año) o Apple Business Manager, todas con cuenta de desarrollador (99 €/año) y revisión. En Android el APK funciona, pero obliga a cada usuario a saltarse avisos de seguridad y a actualizar a mano.

## Decisión

- El control horario **no entra en la v1**. Se planifica como **Fase 12** (fichaje web y panel de administración) y **Fase 13** (PWA y distribución).
- El **empleador es la mancomunidad**, que es también el tenant: los fichajes llevan `mancomunidad_id` y solo los ve su administración y el propio profesor.
- El registro se hace con **hora del servidor**, en una tabla **append-only**. Las correcciones y anulaciones son filas nuevas que referencian el fichaje original, con motivo y autor; nunca se edita ni se borra el original.
- **Verificación de presencia: confianza + auditoría.** No se usa geolocalización. El panel de administración muestra incidencias (jornadas abiertas demasiado tiempo, salidas olvidadas, desfase de reloj) y toda corrección queda auditada. Un QR en el centro se contempla como mejora futura, no como requisito.
- La distribución es una **PWA instalable** construida con el mismo Angular: se abre desde un enlace, se añade a la pantalla de inicio y se actualiza sola. No hay aplicaciones nativas ni pasos por tiendas en el horizonte inicial.
- **Retención de 4 años** y **acceso del trabajador a su propio historial**, con independencia del historial de asistencia de alumnos.
- La PWA debe **funcionar sin conexión** para fichar: la app se cachea, los fichajes se encolan en el dispositivo y se sincronizan al recuperar cobertura, marcando los que llegan con retraso como pendientes de validación.

## Consecuencias

### Positivas

- Una sola base de código para escritorio, Android e iOS, sin cuentas de desarrollador ni revisiones de tienda.
- Las actualizaciones llegan al instante: no hay versiones antiguas instaladas en los móviles.
- El registro cumple el derecho de acceso del trabajador y el plazo de conservación legal.
- El fichaje funciona en centros sin cobertura, que es el escenario realista en pueblos.
- La auditoría sustituye a la geolocalización sin añadir datos personales ni permisos invasivos.

### Negativas / compromisos

- iOS limita la sincronización en segundo plano y puede desalojar almacenamiento local: el envío se produce al abrir la app, no en el instante.
- Sin geocerca ni QR, el registro no prueba criptográficamente la presencia física; la fiabilidad descansa en la hora del servidor, la inmutabilidad y la revisión administrativa.
- Un fichaje hecho sin conexión depende del reloj del dispositivo hasta que el servidor lo recibe; queda marcado y es revisable.
- La PWA no aparece en las tiendas, lo que reduce la percepción de «app oficial».
- Añadir dos fases retrasa el cierre del proyecto más allá de la v1.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Aplicaciones nativas iOS + Android | Dos codebases, dos publicaciones, coste anual y distribución imposible por Drive en iOS. |
| APK por Drive + TestFlight en iOS | Experiencia desigual, builds que caducan, instalación manual y avisos de seguridad en Android. |
| Capacitor/Ionic sobre el Angular actual | Viable, pero sigue exigiendo cuenta de desarrollador y tiendas; se reserva para si algún día se necesita presencia en tiendas o APIs nativas. |
| Fichaje dentro de la v1 | Añade un contexto nuevo, requisitos legales y retención distinta; desplaza el alcance académico ya cerrado. |
| Geocerca GPS | Dato personal de localización, permisos, batería y fallos en interiores; la ley no la exige. |
| QR fijo en cada centro | Buena prueba de presencia, pero se puede fotografiar; solo tendría sentido con QR rotatorio. |

## Relacionado

[[Control horario]] · [[Roadmap]] · [[Visión y alcance]] · [[Roles y permisos]] · [[Requisitos no funcionales]] · [[Multi-tenencia]] · [[ADR-001 - Stack tecnológico]] · [[ADR-003 - JWT con refresh token]] · [[ADR-009 - Multi-tenant con mancomunidad e identidad global]]
