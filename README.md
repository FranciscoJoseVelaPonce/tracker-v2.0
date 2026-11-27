# Tracker de Candidaturas Laborales

Aplicación Java para hacer seguimiento de las candidaturas a ofertas de empleo o prácticas: a qué empresa se ha enviado el CV, en qué estado está cada proceso, cuándo se envió y qué entrevistas se han hecho.

Ejercicio integrador del 1er trimestre (Acceso a Datos + Desarrollo de Interfaces), realizado en pareja por:

- Francisco José Vela Ponce
- Alejandro López García

## Versiones

| Versión | Tag | Persistencia | Interfaz |
|---|---|---|---|
| 1.0 | [`v1.0`](../../tree/v1.0) | Archivo de texto (`candidaturas.csv`) | Línea de comandos + prototipo en Figma |
| 2.0 | [`v2.0`](../../tree/v2.0) | MySQL con JDBC | JavaFX + Scene Builder |

## Qué incluye la versión 2.0

**Acceso a Datos (JDBC + MySQL)**

- Esquema con las tablas `empresa`, `oferta` y `entrevista`, con claves ajenas y restricciones: [`src/main/resources/schema.sql`](src/main/resources/schema.sql). La aplicación lo ejecuta al arrancar (`CREATE TABLE IF NOT EXISTS`), así que no hay que crear las tablas a mano.
- Todas las operaciones de la v1.0 (listar, filtrar, añadir, ver, borrar e importar) pasan a hacerse con JDBC.
- CRUD completo de candidaturas, empresas y entrevistas, y cambio del estado de una candidatura.
- Filtros que se pueden combinar: texto (título o empresa), estado, empresa, salario mínimo y máximo, y rango de fechas. La consulta se monta con trozos fijos de SQL y los valores van siempre como parámetros de `PreparedStatement`.
- Consultas con `JOIN`: las candidaturas se leen con su empresa, las entrevistas con su candidatura y su empresa (tres tablas) y la pantalla de empresas usa `LEFT JOIN` + `GROUP BY` para contar las candidaturas de cada una.
- Transacciones (`setAutoCommit(false)`, `commit` y `rollback`) en las operaciones que tocan varias tablas: borrar una candidatura con sus entrevistas, borrar una empresa con sus candidaturas y entrevistas, e importar el CSV creando las empresas que falten.
- Importación del CSV de la v1.0: la empresa de cada línea se busca por nombre y, si no existe, se crea con el sector «Sin especificar». Si algo falla no se guarda nada.
- Configuración de la conexión fuera del código: [`config.properties`](config.properties), que se puede sobrescribir con las variables de entorno `TRACKER_DB_URL`, `TRACKER_DB_USER` y `TRACKER_DB_PASSWORD`.

**Desarrollo de Interfaces (JavaFX + Scene Builder)**

Todas las pantallas están maquetadas en archivos `.fxml` (en `src/main/resources/es/tracker/ui/fxml`) y siguen el prototipo de Figma de la v1.0 y el manual de estilo (colores, tipografía y estilos de botones, tablas y formularios en [`tracker.css`](src/main/resources/es/tracker/ui/css/tracker.css)).

| Pantalla | Historia de usuario |
|---|---|
| Listado de candidaturas con `TableView` ordenable, filtros y botones de acción | HU-09, HU-13 |
| Cambio de estado con un desplegable dentro de la propia tabla | HU-12 |
| Detalle de una candidatura con su empresa y un panel desplegable de entrevistas | HU-11 |
| Formulario de alta y edición de candidaturas con validación | HU-09 |
| Gestión de empresas: tabla + formulario de alta y edición | HU-10 |
| Gestión de entrevistas: tabla + formulario, y diálogo desde el detalle | HU-11 |

La navegación se hace con un menú lateral y un controlador de navegación (`Navegador`) que carga cada pantalla con `FXMLLoader` y crea los controladores pasándoles los servicios por el constructor. Los controladores solo hablan con la capa de servicio.

## Tecnologías

- Java 25 y Maven
- MySQL 8.4 en Docker, con JDBC (MySQL Connector/J)
- JavaFX 21 y Scene Builder
- JUnit 5, Mockito y H2 (base de datos en memoria para los tests)
- Figma para el prototipo y el manual de estilo

## Estructura del código

```
src/main/java/es/tracker/
  modelo/        Empresa, Oferta, Entrevista, Estado, TipoEntrevista, ResultadoEntrevista, FiltroOferta
  config/        ConfiguracionBD (config.properties + variables de entorno), FabricaConexiones, InicializadorBD
  repositorio/   Interfaces y repositorios JDBC, lector del CSV de la v1.0
  servicio/      OfertaService, EmpresaService y EntrevistaService (validaciones y reglas)
  ui/            AppTracker, Launcher, Navegador y utilidades comunes
  ui/controlador Un controlador por pantalla
src/main/resources/
  schema.sql
  es/tracker/ui/fxml/   Pantallas de Scene Builder
  es/tracker/ui/css/    Hoja de estilos del manual de estilo
```

## Cómo ejecutar la versión 2.0

Hace falta Java 25 o superior, Maven y Docker Desktop.

1. Arrancar MySQL (la primera vez descarga la imagen):

   ```bash
   docker compose -f docker/docker-compose.yml up -d
   ```

   Crea la base de datos `tracker` con el usuario `tracker` / `tracker` en el puerto **3307** (para no chocar con otro MySQL que ya use el 3306).

2. Ejecutar la aplicación:

   ```bash
   mvn javafx:run
   ```

   En IntelliJ también se puede ejecutar la clase `es.tracker.ui.Launcher`, con la carpeta del proyecto como directorio de trabajo para que encuentre `config.properties`.

3. La primera vez la base de datos está vacía: pulsa **Importar CSV** en la pantalla de candidaturas y elige [`candidaturas.csv`](candidaturas.csv). Se crean las 5 empresas y las 15 candidaturas.

Si se quiere usar otra base de datos, basta con cambiar `config.properties` o definir las variables de entorno, sin recompilar.

Para pasar los tests (no necesitan MySQL, usan H2 en memoria):

```bash
mvn test
```

## Cómo ejecutar la versión 1.0

```bash
git checkout v1.0
mvn package
java -jar target/tracker-candidaturas.jar -listar
```

Los comandos de la v1.0 (`-listar`, `-añadir`, `-ver`, `-borrar` e `-importar`) están explicados en el README de esa versión.

## Datos de prueba

[`candidaturas.csv`](candidaturas.csv) contiene 15 candidaturas de ejemplo generadas con IA (5 empresas, con candidaturas en los cuatro estados: `pendiente`, `entrevista`, `rechazado` y `aceptado`). Las fechas van de agosto a octubre de 2026. En la v2.0 se cargan en MySQL con el botón **Importar CSV**.

Formato de cada línea:

```
id;titulo;empresa;estado;fechaPostulacion;enlace;notas
```

## Prototipo y diseño

Prototipo interactivo con 7 pantallas navegables: candidaturas, detalle, formulario, empresas, entrevistas, estadísticas y asistente IA.

- [Prototipo en Figma (solo lectura)](https://www.figma.com/design/37Zy9om98P5F0cNnEkegDO/Tracker-de-Candidaturas-%E2%80%94-Prototipo-v1.0)
- [PDF de las pantallas](doc/pantallas.pdf)
- [Manual de estilo](doc/Hoja%20del%20manual%20de%20estilo.pdf)

### Paleta de colores

| Uso | Color |
|---|---|
| Primario | `#2B4ACB` |
| Primario claro | `#E6EAFA` |
| Secundario | `#0E7C86` |
| Secundario claro | `#DDF2F3` |
| Texto | `#1B1F2A` |
| Texto suave | `#4A5162` |
| Fondo | `#F5F6FA` |
| Superficie | `#FFFFFF` |
| Borde | `#D5D9E2` |

Cada estado de una candidatura tiene su color de texto y su color de fondo:

| Estado | Texto | Fondo |
|---|---|---|
| Pendiente | `#8A5A00` | `#FFF1CC` |
| Entrevista | `#1D4ED8` | `#E0E8FB` |
| Rechazado | `#B42318` | `#FDE7E5` |
| Aceptado | `#1F7A45` | `#E3F4EA` |

### Tipografía

| Estilo | Fuente |
|---|---|
| Títulos | Lexend (Bold 30 y 22, SemiBold 16) |
| Texto | Inter (Regular 14 y 12, SemiBold 14) |
| IDs y fechas | JetBrains Mono (Regular 13) |

En la aplicación JavaFX se usan estas fuentes si están instaladas; si no, se usa la fuente del sistema.

## Contribuciones

| Entrega | Miembro | Aportaciones |
|---|---|---|
| v1.0 | Francisco José Vela Ponce | Proyecto Maven, `enum Estado`, `record Oferta`, interfaz `RepositorioOfertas`, lectura y escritura del CSV (`RepositorioOfertasArchivo`) y sus tests. README: descripción, tecnologías y datos de prueba. PDF de las pantallas y enlace al prototipo de Figma. Tabla de contribuciones y checklist |
| v1.0 | Alejandro López García | `OfertaService` y sus tests, `Cli`, `Main`, `candidaturas.csv`, `AGENTS.md` y `planning.md`. README: compilación, ejecución y uso de cada comando. Manual de estilo en PDF y apartado de diseño. Organización de los PDF en `doc` |
| v1.0 | Los dos | Prototipo de Figma y manual de estilo, revisión del código y vídeo de demostración |
| v2.0 | Francisco José Vela Ponce | Proyecto con JavaFX, MySQL y H2, `docker-compose.yml`, `schema.sql` y configuración externalizada, modelo de empresas y candidaturas, repositorios JDBC de empresas y candidaturas (JOIN, filtros combinables y transacciones) y sus tests, `OfertaService` y sus tests, arranque y navegación de JavaFX, hoja de estilos, pantallas de listado, detalle y formulario de candidaturas. README de la v2.0 |
| v2.0 | Alejandro López García | Modelo y repositorio JDBC de entrevistas (JOIN de tres tablas) y sus tests, importación del CSV a la base de datos en una transacción, `EmpresaService` y `EntrevistaService` con sus tests, pantallas de empresas y entrevistas, panel y diálogo de entrevistas en el detalle, `AGENTS.md` y `planning.md` de la v2.0 |
| v2.0 | Los dos | Revisión del código, pruebas de la aplicación y vídeo de demostración |

Hemos usado IA (Claude) como ayuda durante el proyecto. Las reglas que le dimos están en [`AGENTS.md`](AGENTS.md) y los prompts, en el registro de [`planning.md`](planning.md).
