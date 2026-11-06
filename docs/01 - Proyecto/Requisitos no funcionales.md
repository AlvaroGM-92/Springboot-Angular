---
tipo: documento
estado: activo
fase: 0
actualizado: 2026-11-04
tags:
  - proyecto
  - requisitos
  - calidad
---

# Requisitos no funcionales

> [!info] Criterios transversales
> Aplican a todas las fases. Si una decisión los incumple, debe justificarse en un ADR.

## Seguridad

| ID | Requisito |
| --- | --- |
| SEG-01 | Todo el tráfico en producción irá por HTTPS. |
| SEG-02 | Las contraseñas se almacenarán con BCrypt (coste 12) o Argon2id; nunca en claro ni reversible. |
| SEG-03 | El access token JWT tendrá una vida corta (15 minutos) y no incluirá datos sensibles. |
| SEG-04 | El refresh token durará 7 días, se guardará hasheado, rotará en cada uso y se revocará al cerrar sesión. |
| SEG-05 | Tras 5 fallos de login por cuenta o 20 por IP en 15 minutos, bloquear 15 minutos; el contador se guarda en MySQL y el bloqueo no revela si la cuenta existe. |
| SEG-06 | Toda entrada se validará en el backend con Bean Validation; el frontend valida para mejorar la experiencia, no como única defensa. |
| SEG-07 | CORS estará restringido al origen de Angular en desarrollo y al dominio real en producción. |
| SEG-08 | No habrá secretos en el repositorio; se usarán variables de entorno y `.env.example`. |
| SEG-09 | Se aplicarán cabeceras de seguridad (CSP, `X-Content-Type-Options`, `Referrer-Policy`, `Permissions-Policy`). |
| SEG-10 | Las dependencias se mantendrán actualizadas y se revisarán avisos de seguridad. |
| SEG-11 | Los endpoints comprobarán rol y propiedad del recurso, no solo autenticación. |
| SEG-12 | Los logs no incluirán contraseñas, tokens ni datos personales innecesarios. |
| SEG-13 | El historial de asistencia solo será accesible para `ADMINISTRADOR`; el profesor recibirá `403` aunque sea el autor del cambio. |
| SEG-14 | La administración no dispondrá de endpoints de escritura sobre observaciones, asistencias ni preparaciones. Sobre horarios podrá crear, editar y finalizar borradores y reabrir un horario `FINALIZADO`; nunca editará franjas de un horario finalizado. |
| SEG-15 | El `mancomunidadId` se obtendrá siempre del token validado, nunca de un parámetro del cuerpo o de la URL. |
| SEG-16 | Toda consulta de negocio se filtrará por la mancomunidad activa; un recurso de otra mancomunidad responderá `404`. |
| SEG-17 | El token de selección de mancomunidad tendrá vida corta y solo permitirá elegir entre pertenencias activas del usuario. |
| SEG-18 | Los endpoints de plataforma exigirán `SUPERADMIN` y no expondrán datos académicos de las mancomunidades. |
| SEG-19 | Los refresh tokens estarán ligados a la mancomunidad activa y se revocarán al desactivar la pertenencia. |
| SEG-20 | La reapertura de un horario exigirá `ADMINISTRADOR`, un horario `FINALIZADO`, motivo y traza en el historial; nunca permitirá editar franjas. |
| SEG-21 | El fichaje usará siempre la hora del servidor; la hora del dispositivo se conservará solo como referencia y para detectar desfases. |
| SEG-22 | Los tokens de recuperación de contraseña se guardarán hasheados, serán de un solo uso y caducarán como máximo en 60 minutos. |
| SEG-23 | La solicitud de recuperación responderá siempre `202` con el mismo mensaje y estará limitada por IP y por cuenta para no revelar qué emails existen. |
| SEG-24 | Las contraseñas temporales caducarán en 24 horas y obligarán al cambio en la primera entrada; mientras tanto no permitirán operar. |
| SEG-25 | Todo cambio o restablecimiento de contraseña revocará los refresh tokens del usuario y quedará auditado. |
| SEG-26 | El correo transaccional usará SMTP con TLS, remitente verificado y credenciales por variables de entorno; los enlaces y tokens nunca se registrarán en logs. |
| SEG-27 | El soporte de plataforma solo accederá al plano de cuentas; no leerá ni modificará datos académicos ni impersonará usuarios en la v1. |
| SEG-28 | El bloqueo por intentos fallidos no revocará las sesiones abiertas y solo el `SUPERADMIN` podrá levantarlo antes de que caduque; el reset de contraseña del administrador también lo limpiará. |
| SEG-29 | Los access tokens se firmarán con RS256, incluirán `kid` y se verificarán con un JWKS; la clave rotará cada 90 días con 24 horas de solapamiento y habrá rotación de emergencia inmediata. |
| SEG-30 | Los secretos (claves JWT, `APP_PURGE_SECRET` y credenciales SMTP) vivirán en un gestor de secretos o ficheros montados, nunca en Git, imagen, logs, frontend ni variables visibles en `ps`. |
| SEG-31 | `usuario.token_version` y `usuario_mancomunidad.token_version` permitirán revocar access tokens en curso con una ventana máxima de 60 segundos. |
| SEG-32 | Un refresh ya rotado se tratará como reutilización: se revocará la familia completa, se incrementará `token_version` y se auditará el evento sin guardar el token. |
| SEG-33 | Cada cuenta tendrá como máximo 5 familias de refresh activas, podrá listarlas sin exponer tokens y podrá cerrarlas todas con la contraseña actual. |
| SEG-34 | Recuperación, reenvío de verificación, refresh, escrituras y soporte tendrán límites propios y responderán `429` con `Retry-After`, sin revelar si la cuenta existe. |
| SEG-35 | Las altas y acciones sensibles aceptarán `Idempotency-Key`; el reintento con la misma clave y cuerpo devolverá la respuesta guardada y no duplicará el efecto, con retención de 24 horas. |
| SEG-36 | La recuperación autoservicio exigirá un email verificado; el alta enviará una verificación de 24 horas y el cambio de dirección exigirá confirmar la nueva antes de usarla. |
| SEG-37 | La MFA quedará fuera de la v1, incluida la cuenta de plataforma, con riesgo aceptado, controles compensatorios y revisión/firma obligatoria antes de producción. |
| SEG-38 | El primer `SUPERADMIN` se creará por un comando de bootstrap con secreto montado o consola, sin credencial sembrada ni endpoint HTTP, y la acción quedará auditada. |

## Rendimiento

| ID | Requisito |
| --- | --- |
| REN-01 | Las listas paginadas responderán en menos de 500 ms en el percentil 95 con datos de prueba realistas. |
| REN-02 | Paginación por defecto de 20 elementos y máximo de 100. |
| REN-03 | Las consultas frecuentes tendrán índices y se evitará el problema N+1. |
| REN-04 | El frontend cargará las funcionalidades por rutas (*lazy loading*). |
| REN-01 | Las listas paginadas responderán en menos de 500 ms en el percentil 95 con datos de prueba realistas. |
| REN-02 | Paginación por defecto de 20 elementos y máximo de 100. |
| REN-03 | Las consultas frecuentes tendrán índices y se evitará el problema N+1. |
| REN-04 | El frontend cargará las funcionalidades por rutas (*lazy loading*). |
| REN-05 | Las operaciones masivas de asistencia se resolverán en una sola transacción y un número acotado de consultas. |
| REN-06 | El sistema soportará al menos 50 usuarios concurrentes por mancomunidad y 500 en toda la plataforma sin degradación perceptible; el diseño y las pruebas de carga se dimensionarán para esos valores. |
| REN-07 | La latencia objetivo del percentil 95 será de 500 ms para lecturas y 1 s para escrituras, medidas desde el navegador hasta la respuesta, en condiciones normales de red. |
| REN-08 | El tamaño máximo de respuesta paginada será de 100 elementos; las consultas sin paginación estarán prohibidas salvo en catálogos pequeños (centros, asignaturas de un profesor). |
| REN-09 | Una mancomunidad típica tendrá hasta 500 alumnos, 50 profesores y 100 asignaturas activas; el diseño no debe degradarse hasta 5.000 alumnos y 500 profesores. |
| REN-10 | El bundle inicial del frontend no superará los 500 kB comprimidos; cada funcionalidad se carga por ruta. |

### Presupuesto de rendimiento (valores de referencia)

| Magnitud | Valor |
| --- | --- |
| Usuarios concurrentes por mancomunidad | 50 |
| Usuarios concurrentes en toda la plataforma | 500 |
| Latencia p95 de lectura | < 500 ms |
| Latencia p95 de escritura | < 1 s |
| Tamaño de página por defecto / máximo | 20 / 100 |
| Alumnos por mancomunidad (típico / máximo) | 500 / 5.000 |
| Profesores por mancomunidad (típico / máximo) | 50 / 500 |
| Asignaturas por mancomunidad (típico) | 100 |
| Bundle inicial del frontend (comprimido) | < 500 kB |

## Usabilidad y accesibilidad

| ID | Requisito |
| --- | --- |
| USA-01 | Toda la interfaz estará en español, con terminología coherente con el [[Glosario]]. |
| USA-02 | La interfaz será responsive y usable desde 360 px de ancho. |
| USA-03 | La interfaz cumplirá el nivel **AA de WCAG 2.2**: contraste mínimo 4.5:1 en texto normal y 3:1 en texto grande, navegación completa por teclado, foco visible, etiquetas asociadas, `aria-live` para estados dinámicos y soporte de `prefers-reduced-motion`. Se verificará con Lighthouse y una revisión manual por teclado. |
| USA-04 | Las acciones destructivas pedirán confirmación y explicarán consecuencias. |
| USA-05 | Los errores indicarán qué campo falla y cómo corregirlo. |
| USA-06 | Los listados mostrarán estados de carga, vacío y error. |
| USA-07 | El horario semanal será legible en escritorio y tableta, con scroll horizontal en móvil. |
| USA-08 | Los componentes de Angular Material se configurarán con un tema accesible; no se desactivará el foco por defecto ni se usará color como único indicador. |

## Mantenibilidad

| ID | Requisito |
| --- | --- |
| MNT-01 | Backend organizado por funcionalidad, con separación controller → service → repository. |
| MNT-02 | Frontend con componentes standalone, servicios tipados y separación `core` / `shared` / `features`. |
| MNT-03 | Tests unitarios de reglas de negocio y tests de integración de los flujos críticos. |
| MNT-04 | Commits con Conventional Commits y un commit por cambio lógico. |
| MNT-05 | Sin `any` en TypeScript salvo justificación documentada. |
| MNT-06 | La API se documentará con OpenAPI y ejemplos. |
| MNT-07 | Cada fase deja el proyecto compilando, con tests en verde y documentación actualizada. |
| MNT-08 | El aislamiento entre mancomunidades se probará con dos tenants en los tests de integración de cada módulo de negocio. |
| MNT-09 | Las claves únicas y los índices de las tablas de negocio incluirán `mancomunidad_id` cuando la regla sea por tenant. |
| MNT-10 | Las migraciones Flyway sembrarán una mancomunidad de desarrollo y permitirán crear nuevas sin cambios de esquema. |

## Compatibilidad

| ID | Requisito |
| --- | --- |
| COM-01 | Navegadores modernos: Chrome, Firefox, Edge y Safari en sus dos últimas versiones. |
| COM-02 | Zona horaria de referencia `Europe/Madrid`; almacenamiento en UTC. |
| COM-03 | Fechas y horas en formato ISO 8601 en la API y formato local en la interfaz. |
| COM-04 | La PWA del fichaje abrirá y permitirá fichar sin conexión, sincronizando al recuperar cobertura (Fase 13). |
| COM-05 | El correo transaccional se enviará por SMTP; en desarrollo se usará Mailpit y en producción un proveedor con SPF y DKIM. |

## Datos

| ID | Requisito |
| --- | --- |
| DAT-01 | El esquema se gestionará con Flyway y migraciones versionadas. |
| DAT-02 | Codificación `utf8mb4` y collation compatible con español. |
| DAT-03 | Los timestamps se guardarán en UTC. |
| DAT-04 | Los datos históricos no se borrarán físicamente: se desactivan o se marcan como baja, salvo la purga por retención del historial de asistencia (DAT-14). |
| DAT-05 | Habrá una copia completa diaria cifrada, con 30 copias diarias y 12 mensuales; la restauración estará documentada y se probará cada trimestre en staging ([[Copias de seguridad y exportación]]). |
| DAT-06 | Cada cambio de asistencia se registrará en una tabla de historial inmutable, en la misma transacción que la modificación. |
| DAT-07 | El historial de asistencia no se actualizará ni borrará desde la aplicación, salvo el proceso programado de purga por retención. |
| DAT-08 | Los estados del horario (`BORRADOR`/`FINALIZADO`) se persistirán y se validarán en el backend antes de aceptar cambios en las franjas. |
| DAT-09 | La base de datos garantizará, mediante restricción, que solo exista un curso escolar en estado `ACTIVO` por mancomunidad. |
| DAT-10 | Los cursos cerrados serán de solo lectura: no se aceptarán altas ni modificaciones de matrículas, horarios o preparaciones en ellos. |
| DAT-11 | Todas las tablas de negocio incluirán `mancomunidad_id` con índice y, cuando aplique, formando parte de la clave única. |
| DAT-12 | Las FKs serán simples; el servicio validará que cada relación une filas de la misma mancomunidad antes de guardar y los tests de aislamiento lo comprobarán. |
| DAT-13 | Las exportaciones y copias lógicas por mancomunidad se filtrarán por `mancomunidad_id`. |
| DAT-14 | El historial de asistencia se conservará mientras la matrícula esté `ACTIVA` y 2 años desde su baja (`purga_at`); después se purgará y solo quedarán los contadores anonimizados de `resumen_asistencia` con `alumno_hash`, asignatura y curso. |
| DAT-15 | La purga por retención se ejecutará por mancomunidad, en lotes, con registro de filas afectadas y sin tocar matrículas activas ni otros tenants; marcará `purgada_at` para no reprocesar y respetará la reactivación previa. |
| DAT-16 | Los fichajes de jornada se conservarán 4 años y serán consultables por la persona trabajadora, conforme al art. 34.9 ET. |
| DAT-17 | Los tokens de recuperación usados o caducados se purgarán; no se conservan como histórico. |
| DAT-18 | Los eventos de acceso se conservarán 30 días para diagnosticar bloqueos y se purgarán después; nunca contendrán contraseñas ni tokens. |
| DAT-19 | Toda entidad editable con `updated_at` incluirá `version` para bloqueo optimista; el backend rechazará con `409` una actualización con la versión desactualizada. |
| DAT-20 | Se crearán los índices de claves foráneas y filtros ya documentados; su revisión con `EXPLAIN` se hará en la Fase 10 y los nuevos índices entrarán por migración. |
| DAT-21 | La exportación lógica por mancomunidad será un script de operación filtrado por `mancomunidad_id`, sin hashes de contraseña, tokens ni eventos de acceso. |
| DAT-22 | Las tablas de seguridad se purgarán por plazo: `evento_acceso` y ventanas de `limite_peticion` a 30 días y 24 horas; `evento_seguridad` a 1 año; refresh revocado o caducado, recuperación y verificación a 30 y 7 días; `idempotencia` a 24 horas. |
| DAT-23 | Los logs de aplicación se conservarán 30 días en el servidor, en formato estructurado, sin datos personales completos, contraseñas ni tokens. |
| DAT-24 | La auditoría de plataforma se conservará 2 años y se purgará después; las cuentas desactivadas no se borran, solo se revocan sus sesiones y tokens, para conservar la autoría y la trazabilidad. |

## Observabilidad

| ID | Requisito |
| --- | --- |
| OBSV-01 | Logs estructurados con nivel, timestamp, traza y usuario (sin datos sensibles). |
| OBSV-02 | Endpoint de salud de Spring Boot Actuator. |
| OBSV-03 | Un identificador de correlación por petición para poder seguir un error. |
| OBSV-04 | Métricas básicas de uso y errores en producción. |
| OBSV-05 | Los logs de aplicación se conservarán 30 días, estructurados y sin datos sensibles; la auditoría de plataforma se conservará 2 años. |

## Privacidad

| ID | Requisito |
| --- | --- |
| LEG-01 | Solo se recogerán los datos personales necesarios para la gestión académica. |
| LEG-02 | El acceso a datos de menores y tutores estará restringido por rol y necesidad. |
| LEG-03 | Se documentará el periodo de conservación y el procedimiento de baja. |
| LEG-04 | El registro de jornada no usará geolocalización: la verificación será de confianza más auditoría, con correcciones motivadas. |
| LEG-05 | Existirá una nota de privacidad con finalidad, base jurídica, registro de actividades, derechos del interesado, datos de menores y medidas de seguridad ([[Privacidad y tratamiento de datos]]). |
| LEG-06 | Los datos de menores (fecha de nacimiento y datos del tutor) se tratarán como datos personales del menor; el consentimiento y la información a las familias los gestiona la mancomunidad y la aplicación no envía comunicaciones a las familias en la v1. |
| LEG-07 | Los derechos de acceso, rectificación, supresión, limitación, portabilidad y oposición se tramitarán por la mancomunidad responsable, con plazos legales, apoyo del soporte y registro en la auditoría; la portabilidad se cubrirá con el script de exportación lógica. |
| LEG-08 | Existirá un contrato de encargado del tratamiento que reparta responsabilidades entre la plataforma y cada mancomunidad, liste los subencargados y se revise antes de producción ([[Privacidad y tratamiento de datos]]). |
| LEG-09 | Si se solicita suprimir un dato sujeto a retención legal (asistencia, jornada, auditoría), no se borrará: se responderá con el motivo y el plazo, se anonimizará lo posible y se registrará la decisión. |

## Relacionado

[[Requisitos funcionales]] · [[Seguridad y autenticación]] · [[Convenciones]] · [[Stack tecnológico]] · [[Multi-tenencia]] · [[Control horario]] · [[ADR-010 - Control horario diferido y PWA]] · [[ADR-011 - Recuperación de contraseña y correo transaccional]] · [[ADR-012 - Soporte de cuentas del superadministrador]] · [[ADR-016 - Horario editable por profesor y administrador]] · [[ADR-027 - MFA y riesgo aceptado del superadministrador]] · [[ADR-028 - Verificación y cambio de email]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-030 - Detección de reutilización de refresh tokens]] · [[ADR-031 - Límite de sesiones concurrentes y cierre global]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-033 - Idempotencia de peticiones sensibles]] · [[ADR-034 - Bootstrap del primer superadministrador]]
