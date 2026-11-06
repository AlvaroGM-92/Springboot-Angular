---
tipo: adr
estado: aceptada
fecha: 2026-11-04
actualizado: 2026-11-04
tags:
  - adr
  - operacion
  - copias
  - seguridad
  - secretos
---

# ADR-035 — Gestión de la clave de cifrado de copias de seguridad

## Contexto

Las copias diarias y mensuales se cifran en reposo antes de salir del servidor (`[[Copias de seguridad y exportación]]`). El cifrado solo sirve si la clave se gestiona con el mismo cuidado que los datos: una clave perdida hace inútiles todas las copias y una clave filtrada expone todos los tenants a la vez, incluidos los datos personales de menores.

La documentación anterior decía «se cifra en el servidor» y «las credenciales llegan por variables de entorno», pero no fijaba dónde vive la clave, quién la ve, cómo se rota ni cómo se prueba que una copia se puede descifrar. PEN-29 pide cerrar ese hueco antes de la preproducción.

## Decisión

### 1. Algoritmo y formato

- Cifrado simétrico **AES-256-GCM** por fichero, con IV aleatorio y etiqueta de autenticación.
- Se usa **cifrado envolvente**: cada copia se cifra con una **clave de datos (DEK)** aleatoria de 256 bits, generada en el servidor en el momento de la copia; la DEK se cifra (envuelve) con la **clave maestra (KEK)** y se guarda junto al fichero (`mancomunidad-YYYY-MM-DD.sql.gz.enc` + `.key`).
- El formato y el comando exacto se fijan en el script de operación, no en la documentación; la documentación solo fija el algoritmo y la longitud.

### 2. Dónde vive la clave

- La **KEK** vive en un **gestor de secretos** (o en un fichero montado como secreto en el servidor de copias), nunca en el repositorio, en la imagen, en la base de datos ni en el pipeline de CI/CD.
- Se lee por ruta (`BACKUP_KEK_PATH`), igual que las claves JWT. No se pasa por argumento de línea de comandos ni por variable visible en `ps` o `docker inspect`.
- La KEK no se replica en el servidor de aplicación: las copias se cifran y descifran en el host de operación, que puede ser el mismo servidor o uno dedicado.
- La DEK de cada copia se guarda cifrada junto al fichero; sin la KEK es inútil.

### 3. Quién accede

- Solo el **personal de operación** designado puede ejecutar copias y restauraciones. Un administrador de mancomunidad no puede acceder a las copias ni a la clave.
- En producción se aplica **doble control**: ninguna persona sola puede descifrar una copia de producción. La KEK se divide en dos partes (Shamir o dos ficheros en custodia distinta) o se exige la presencia de dos operadores (uno con la clave, otro con la contraseña del gestor).
- Toda ejecución de copia, restauración o rotación queda en el log de operación con actor, fecha y resultado. El log no incluye la clave ni el contenido.

### 4. Rotación

- La KEK se rota **al menos una vez al año** y de forma inmediata si se sospecha compromiso.
- La rotación **no re-cifra las copias antiguas**: se genera una KEK nueva, se re-envuelven las DEK de las copias vigentes (diarias + mensuales) con la nueva KEK y se conserva la KEK anterior en modo solo lectura hasta que todas las copias cifradas con ella caduquen por retención.
- Cada copia guarda un identificador de la KEK usada (`key_id`), para saber con qué clave descifrarla.
- La KEK anterior se destruye cuando la última copia cifrada con ella haya caducado (30 días para diarias, 12 meses para mensuales), y la destrucción se anota.
- La rotación se prueba en staging antes de aplicarse en producción.

### 5. Copia de seguridad de la propia clave

- La KEK (o sus partes) se guarda también en un **soporte offline** (por ejemplo, un fichero cifrado en un medio físico guardado en una ubicación distinta del servidor), custodiado por una persona distinta de quien opera las copias.
- Al menos una vez al año se verifica que el soporte offline se puede leer y que la KEK recuperada descifra una copia de prueba.
- La pérdida de la KEK sin copia de seguridad offline equivale a la pérdida de todas las copias: se trata como incidente crítico.

### 6. Prueba de restauración y de descifrado

- La prueba trimestral de restauración (`[[Copias de seguridad y exportación]]` §4) incluye obligatoriamente el descifrado con la KEK vigente y la verificación de la etiqueta de autenticación (GCM).
- Si el descifrado falla, la prueba se considera fallida y bloquea el siguiente despliegue hasta corregirse, igual que un fallo de restauración.
- La prueba se hace con una copia reciente y con una copia antigua, para validar también las KEK anteriores que sigan vigentes.
- El tiempo de descifrado se mide y se suma al RTO.

### 7. Lo que no se hace

- No se usa la misma clave para cifrar copias y para cifrar tokens, contraseñas o datos de la aplicación.
- No se envía la clave por correo ni se pega en un ticket de soporte.
- No se desactiva el cifrado en staging para «ir más rápido»: staging usa una KEK propia, no la de producción.
- El pipeline de CI/CD no toca las copias ni la clave.

## Consecuencias

### Positivas

- Una filtración de la base de datos no implica la filtración de las copias, y viceversa.
- La rotación no obliga a re-cifrar gigabytes de copias antiguas.
- La prueba trimestral demuestra de verdad que se puede recuperar, no solo que el fichero existe.
- El doble control reduce el riesgo de un operador único.

### Negativas / compromisos

- La gestión de la KEK es una responsabilidad operativa permanente; si se descuida, el RTO y el RPO dejan de tener sentido.
- El cifrado envolvente añade un fichero por copia y un paso en el script.
- Mantener KEK anteriores hasta que caduquen las copias obliga a un inventario fiable de `key_id`.
- El soporte offline introduce un medio físico que hay que custodiar y probar.

## Alternativas consideradas

| Alternativa | Motivo de descarte |
| --- | --- |
| Clave única compartida por todas las copias y rotación re-cifrando todo | Costoso y propenso a errores; una rotación fallida puede perder copias. |
| Clave en una variable de entorno del contenedor | Visible en `docker inspect` y en el proceso; no es custodia. |
| Cifrado con la contraseña de la base de datos | Mezcla secretos y no permite rotación independiente. |
| Servicio de gestión de claves externo (KMS) | Válido y preferible si existe; la v1 asume un gestor local o fichero montado y deja la integración con KMS como mejora. |
| Sin cifrado envolvente, una sola clave por copia | Complica la rotación y el inventario; el envolvente es estándar. |

## Relacionado

[[Copias de seguridad y exportación]] · [[Seguridad y autenticación]] · [[Stack tecnológico]] · [[Convenciones]] · [[Requisitos no funcionales]] · [[ADR-029 - Claves JWT, rotación y secretos]] · [[ADR-032 - Rate limiting de extremos sensibles]] · [[ADR-034 - Bootstrap del primer superadministrador]]