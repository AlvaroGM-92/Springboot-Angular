---
tipo: referencia
estado: activo
fase: 10
actualizado: 2026-11-04
tags:
  - operacion
  - copias
  - datos
  - multi-tenant
---

# Copias de seguridad y exportación

## 1. Objetivo

Definir cómo se protege la base de datos de la mancomunidad, cómo se restaura y cómo se extraen los datos de un tenant concreto sin mezclarlos con otros. La copia es una tarea de **operación**: no se expone como función de usuario en la v1.

## 2. Copia completa diaria

- Una tarea programada ejecuta cada noche un `mysqldump` lógico de toda la base:
  - `--single-transaction` para no bloquear la operación;
  - `--routines --triggers --events`;
  - `--set-gtid-purged=OFF` y `utf8mb4`.
- El resultado se comprime, se cifra en el servidor y se copia a un almacenamiento externo.
- Nunca se guarda una copia solo en el volumen de Docker ni dentro del repositorio.
- El nombre incluye fecha y hora: `mancomunidad-YYYY-MM-DD.sql.gz`.
- Se registra en el log de operación el nombre, tamaño, checksum SHA-256 y resultado; nunca el contenido.

Comando de referencia:

```bash
mysqldump --single-transaction --routines --triggers --events \
  --set-gtid-purged=OFF --default-character-set=utf8mb4 \
  -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME" \
  | gzip > "mancomunidad-$(date +%F).sql.gz"
```

> [!warning] Credenciales
> Las credenciales llegan por variables de entorno. No se escriben en el script, en el repositorio ni en la documentación.

## 3. Retención

| Copia | Frecuencia | Retención |
| --- | --- | --- |
| Diaria | Cada noche | **30 días** |
| Mensual | Primera copia de cada mes | **12 meses** |

- La copia mensual se conserva aunque se purguen las diarias.
- La purga de copias caducadas es automática y deja registro de qué se borró.
- **RPO:** 24 horas; en el peor caso se pierde el trabajo del último día.
- **RTO objetivo:** 4 horas, medido en cada prueba de restauración.

## 4. Restauración

Procedimiento:

1. Aprovisionar una instancia MySQL 8.4 limpia y vacía.
2. Descifrar y descomprimir la copia elegida.
3. Importarla con `mysql`.
4. Ejecutar `flyway validate` y `flyway migrate` para confirmar que el esquema corresponde a la versión esperada.
5. Arrancar el backend contra la base restaurada y comprobar `/actuator/health`.
6. Hacer un smoke test: login de un administrador de prueba, apertura de un horario y consulta de una asistencia.
7. Anotar fecha, fichero, duración, resultado y cualquier incidencia.

```bash
gunzip -c mancomunidad-YYYY-MM-DD.sql.gz \
  | mysql -h "$DB_HOST" -u "$DB_USER" -p"$DB_PASSWORD" "$DB_NAME"
```

### Prueba trimestral

- Cada trimestre se restaura la última copia diaria en un entorno de **staging**.
- Se verifica que la aplicación arranca, que un administrador entra y que los datos de dos mancomunidades siguen aislados.
- Se mide el tiempo real de restauración y se ajusta el RTO si hace falta.
- El resultado se anota en el diario de operación; una prueba fallida bloquea el siguiente despliegue hasta corregirse.

> [!important] Sin binlogs en la v1
> No se activa recuperación a un punto exacto en el tiempo (PITR). La copia diaria es la única garantía; si en el futuro se exige un RPO menor, se añadirán binlogs y una prueba específica.

## 5. Exportación lógica por mancomunidad

- Un **script de operación** extrae los datos de una mancomunidad concreta, filtrando siempre por `mancomunidad_id`.
- Genera JSON o CSV por tabla y un manifiesto con: esquema, fecha, código de mancomunidad, número de filas por tabla y checksum.
- Incluye los datos académicos y las cuentas vinculadas a esa mancomunidad.
- **Excluye** hashes de contraseña, tokens de refresco y recuperación, eventos de acceso y auditoría de plataforma.
- No se ofrece como endpoint ni como botón del panel en la v1; la importación masiva desde la interfaz queda fuera de alcance.
- La exportación se guarda y cifra con las mismas reglas que una copia de seguridad.

## 6. Seguridad

- Copias y exportaciones cifradas en reposo y en tránsito.
- Acceso limitado al personal de operación; nunca a un administrador de mancomunidad.
- Las copias no se suben al repositorio ni se adjuntan a incidencias.
- Los logs no incluyen datos personales ni credenciales.
- Una copia restaurada en staging se destruye al terminar la prueba.

## 8. Clave de cifrado de las copias

- El cifrado es **AES-256-GCM** con cifrado envolvente: cada copia se cifra con una DEK aleatoria y la DEK se envuelve con la **KEK** (clave maestra), que se guarda junto al fichero cifrado.
- La **KEK** vive en un gestor de secretos o en un fichero montado como secreto en el host de operación; nunca en el repositorio, la imagen, la base de datos ni el pipeline. Se lee por ruta (`BACKUP_KEK_PATH`).
- Acceso con **doble control** en producción: ninguna persona sola descifra una copia. Los administradores de mancomunidad no tienen acceso.
- La KEK se **rota al menos una vez al año** y de inmediato si se sospecha compromiso; la rotación re-envuelve las DEK vigentes y conserva la KEK anterior en modo solo lectura hasta que caduquen las copias cifradas con ella.
- Cada copia guarda un `key_id` para saber con qué KEK descifrarla.
- La KEK se guarda también en un soporte offline custodiado por una persona distinta, y se verifica al menos una vez al año.
- La prueba trimestral de restauración incluye el descifrado y la verificación de la etiqueta GCM; un fallo bloquea el siguiente despliegue.
- No se usa la misma clave para copias y para la aplicación, ni se envía por correo o ticket.

Véase [[ADR-035 - Gestión de la clave de cifrado de copias]].

## 9. Derechos del interesado

- **Acceso:** el interesado puede consultar sus datos a través de la mancomunidad responsable; el soporte de plataforma no accede a datos académicos.
- **Rectificación:** se realiza desde el panel administrativo (usuarios, alumnos, centros) o mediante soporte con motivo para cuentas.
- **Supresión:** se atiende cuando no exista obligación de conservación; si el dato debe conservarse (asistencia, jornada, auditoría), se responde con el motivo y el plazo legal y se anonimiza lo posible.
- **Portabilidad:** se cubre con el script de exportación lógica por mancomunidad ([[Copias de seguridad y exportación]] §5), filtrado por `mancomunidad_id`, en JSON/CSV con manifiesto y checksum, sin hashes ni tokens.
- **Limitación y oposición:** se tramitan por la mancomunidad responsable, con apoyo técnico de la plataforma; se registran en la auditoría cuando interviene el soporte.
- **Plazos:** se responde en los plazos legales (1 mes, prorrogable 2 más en casos complejos) y se anota cada solicitud y su resolución.

[[Modelo de datos]] · [[Multi-tenencia]] · [[Mancomunidad]] · [[Seguridad y autenticación]] · [[Requisitos no funcionales]] · [[Roadmap]] · [[Stack tecnológico]] · [[ADR-035 - Gestión de la clave de cifrado de copias]]
