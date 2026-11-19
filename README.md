# BackIt — aplicativo de escritorio

Sistema institucional de objetos perdidos (JavaFX + Spring Boot + PostgreSQL).
Estado: Sprints 0 a 5 implementados sobre la base de Sprints 0-2 existente.

## Requisitos

- JDK 21 o superior (el proyecto compila con `--release 21`; se ha verificado con JDK 24).
- Maven (o el incluido en IntelliJ IDEA).
- PostgreSQL con una base de datos `BackIt`.

## Configuración de la base de datos

Las credenciales **no** están en el código ni en Git. Hay dos formas (prioridad 1 > 2):

1. **Variables de entorno del proceso** (recomendado en servidores):
   ```powershell
   $env:DB_URL      = "jdbc:postgresql://localhost:5433/BackIt"
   $env:DB_USER     = "usuario_app"
   $env:DB_PASSWORD = "password_seguro"
   ```
2. **Archivo externo local** (más cómodo en IntelliJ / desarrollo): copia
   `application-local.properties.example` a `application-local.properties` y ajusta
   los valores. Ese archivo está en `.gitignore`, así que nunca se sube.

Si no existe ninguna de las dos fuentes, `${DB_URL}` queda sin resolver y el arranque
falla con `Driver org.postgresql.Driver claims to not accept jdbcUrl, ${DB_URL}`.

Opcionales:

| Variable | Default | Uso |
|---|---|---|
| `BACKIT_ARCHIVOS_DIR` | `~/.backit/archivos` | Carpeta privada de fotos/evidencias/QR |
| `BACKIT_MATCHING_UMBRAL` | `70` | Puntaje mínimo para sugerir una coincidencia |

## Base de datos

El esquema con claves foráneas, índices y restricciones `CHECK` está en
`src/main/resources/db/schema.sql`:

```powershell
psql -U postgres -d BackIt -f src\main\resources\db\schema.sql
```

Durante el desarrollo `spring.jpa.hibernate.ddl-auto=update` crea/actualiza tablas.
En producción, después de aplicar el script, cambie ese valor a `validate` y
otorgue al usuario de aplicación solo `SELECT/INSERT` en las tablas operativas
(y **sin** `UPDATE/DELETE` sobre `auditoria_sistema`).

## Ejecutar

```powershell
mvn javafx:run        # app real (AppLauncher)
mvn test              # 44 pruebas unitarias
mvn package           # genera target/BackIt.jar
java -jar target\BackIt.jar
```

> `mvn javafx:run` abre la aplicación funcional. El bosquezo (`PrototipoLauncher`)
> sigue disponible en el paquete `com.backit.desktop.prototipo`, pero ya no es el
> `mainClass` por defecto.

## Etiquetas QR de objetos encontrados

En la aplicación funcional, abra **Objetos encontrados**, complete el formulario
y guarde el hallazgo. BackIt muestra la etiqueta y guarda el PNG en
`qr/qr_<UUID>.png` dentro de `BACKIT_ARCHIVOS_DIR` (por defecto,
`~/.backit/archivos`). Puede abrir ese archivo para imprimir la etiqueta.

Para volver a generarla, seleccione un objeto de la tabla y pulse **Generar
Etiqueta QR**. El QR conserva el UUID asignado al hallazgo y contiene únicamente
ese identificador. Si falla la generación o el guardado del PNG después del
registro, puede reintentar desde la tabla sin registrar el objeto otra vez.

## Qué se añadió por sprint

| Sprint | Contenido |
|---|---|
| 0 | Credenciales por entorno, `db/schema.sql` con CHECK/índices, empaquetado jar, compatibilidad Lombok con JDK 23+ |
| 1 | Términos versionados con aceptación bloqueante y hash, siembra automática v1.0, recuperación de cuenta con token CADUCABLE, desbloqueo al reactivar cuentas |
| 2 | `UbicacionService` jerárquico con puntos de entrega y pantalla real de ubicaciones |
| 3 | Catálogo de categorías, registro de hallazgos con foto protegida (SHA-256), QR real con ZXing y etiqueta imprimible |
| 4 | Reportes de pérdida con búsqueda por texto, categoría, rango de fechas y estado |
| 5 | Motor de coincidencias multicriterio (categoria/descripción/ubicación/fecha/señas) con pesos configurables, sugerencias, notificaciones internas, desglose en pantalla y botón «Recalcular» |
| 6 | Reclamaciones desde una coincidencia, evidencias privadas con SHA-256, aprobación/rechazo, cambio de estado del objeto y notificaciones |

Nota: la entrega física verificada, el historial de estados y la pantalla de
auditoría (Sprint 7) siguen como maquetas. Si las coincidencias salen vacías,
use el botón «Recalcular coincidencias» o ajuste `BACKIT_MATCHING_UMBRAL` (por
defecto 70): el umbral exige que categoría, ubicación, fecha y descripción sean
bastante similares entre el hallazgo y el reporte.
