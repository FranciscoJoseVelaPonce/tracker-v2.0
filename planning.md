# planning.md — Planificación del proyecto

**Proyecto:** Tracker de Candidaturas Laborales
**Equipo:** Francisco José Vela Ponce y Alejandro López García

---

# Entrega v2.0 (JDBC + JavaFX)

## 1. Decisiones de diseño

- [x] **Base de datos:** MySQL 8.4 en Docker (`docker/docker-compose.yml`), en el puerto 3307 para no chocar con otro MySQL.
- [x] **Creación de las tablas:** la aplicación ejecuta `schema.sql` al arrancar. Usa `CREATE TABLE IF NOT EXISTS`, así que no se pierden datos.
- [x] **Configuración:** `config.properties` en la carpeta del proyecto; las variables de entorno `TRACKER_DB_URL`, `TRACKER_DB_USER` y `TRACKER_DB_PASSWORD` tienen prioridad.
- [x] **Conexiones:** una conexión por operación con `try-with-resources` (`FabricaConexiones`). Suficiente para una aplicación de escritorio.
- [x] **Errores de base de datos:** `PersistenciaException` envuelve la `SQLException` con un mensaje para el usuario.
- [x] **Modelo:** records inmutables que se validan al crearse, como en la v1.0. La `Oferta` lleva la `Empresa` completa.
- [x] **Salario:** opcional; `NULL` en la base de datos y `null` en Java para distinguir «sin salario» de 0.
- [x] **Filtros:** se combinan todos con `AND` en la consulta SQL. El texto busca en el título y en el nombre de la empresa.
- [x] **Borrados:** borrar una candidatura borra sus entrevistas; borrar una empresa borra sus candidaturas y sus entrevistas. Se hace en una transacción y se pide confirmación indicando cuántas se van a borrar.
- [x] **Importar el CSV:** las líneas mal se saltan y se avisa de cuáles son. Las buenas se guardan en una transacción: si falla una, no se guarda ninguna. Las empresas que no existen se crean con el sector «Sin especificar».
- [x] **Validaciones:** obligatorios en el modelo; en el servicio, fecha de postulación no posterior a hoy, enlaces y webs con `http://` o `https://` y nombres de empresa sin repetir.
- [x] **Interfaz:** menú lateral como en el prototipo de Figma. El `Navegador` carga los FXML con `FXMLLoader` y crea los controladores pasándoles los servicios por el constructor (`setControllerFactory`).
- [x] **Cambio de estado (HU-12):** desplegable de colores dentro de la columna Estado de la tabla.
- [x] **Entrevistas en el detalle (HU-11):** `TitledPane` desplegable con su tabla y un diálogo para añadir o editar.
- [x] **Estilos:** una única hoja `tracker.css` con los colores y fuentes del manual de estilo.
- [x] **Tests:** Mockito para los servicios y H2 en memoria (modo MySQL) para los repositorios.
- [x] **Flujo de Git:** commits directos a `main`, cada uno con su cuenta, como en la v1.0.

## 2. Plan de trabajo

### 2.1 Acceso a Datos

- [x] Dependencias de JavaFX, MySQL, Mockito y H2
- [x] `docker-compose.yml` con MySQL
- [x] `schema.sql` con las tres tablas, claves ajenas y restricciones
- [x] Configuración externalizada y creación de las tablas al arrancar
- [x] Modelo: `Empresa`, `Oferta` con empresa, salario y descripción, `FiltroOferta`
- [x] Repositorio JDBC de empresas (con número de candidaturas por empresa)
- [x] Repositorio JDBC de candidaturas con filtros combinables y cambio de estado
- [x] Modelo y repositorio JDBC de entrevistas (JOIN de tres tablas)
- [x] Transacciones en los borrados en cascada
- [x] Importación del CSV de la v1.0 en una transacción
- [x] Servicios con las validaciones
- [x] Tests de servicios (Mockito) y de repositorios (H2)

### 2.2 Desarrollo de Interfaces

- [x] Arranque de JavaFX, `Launcher` y navegación con menú lateral
- [x] Hoja de estilos con el manual de estilo
- [x] Listado de candidaturas con filtros, ordenación y botones de acción
- [x] Cambio de estado desde la tabla
- [x] Detalle de una candidatura con su empresa y sus entrevistas
- [x] Formulario de alta y edición de candidaturas con validación
- [x] Gestión de empresas (tabla + formulario)
- [x] Gestión de entrevistas (tabla + formulario) y diálogo desde el detalle
- [ ] Abrir cada FXML en Scene Builder y ajustar lo que haga falta
- [ ] Revisión final entre los dos

## 3. Reparto de tareas

| Tarea | Francisco José | Alejandro |
|---|---|---|
| Proyecto, Docker, esquema y configuración | ✔ | |
| Empresas y candidaturas: modelo, repositorio JDBC y tests | ✔ | |
| Entrevistas: modelo, repositorio JDBC y tests | | ✔ |
| Importación del CSV a la base de datos | | ✔ |
| `OfertaService` y tests | ✔ | |
| `EmpresaService`, `EntrevistaService` y tests | | ✔ |
| Arranque, navegación y hoja de estilos | ✔ | |
| Pantallas de candidaturas (listado, detalle y formulario) | ✔ | |
| Pantallas de empresas y entrevistas, panel y diálogo de entrevistas del detalle | | ✔ |
| README | ✔ | |
| `AGENTS.md` y `planning.md` | | ✔ |
| Vídeo de demostración | ✔ | ✔ |

## 4. Registro de prompts

| Fecha | Quién | Herramienta | Prompt | Resultado / qué se hizo con él |
|---|---|---|---|---|
| 26/11/2026 | Francisco José | Claude | «Hay que hacer hasta la v2.0. La v1.0 la hicimos ya, puedes mirarla en mi repositorio de GitHub; no sé si hay que combinarlas o hacer una nueva» (con el enunciado completo) | Se decidió seguir en el mismo repositorio sobre la v1.0. Plan de la v2.0 por capas y reparto por funcionalidades |
| 26/11/2026 | Francisco José | Claude | «Hay que hacerlo commit a commit y poco a poco con mi compañero» | Guion de commits alternando a los dos miembros. Cada uno hace los suyos con su cuenta |
| 26/11/2026 | Francisco José | Claude | Implementar la v2.0 por capas (modelo, config, repositorios JDBC, servicios, interfaz JavaFX) con los requisitos del enunciado | Código, FXML y tests. Revisados y probados con `mvn test` y ejecutando la aplicación |
| 26/11/2026 | Francisco José | Claude | Actualizar el README, `AGENTS.md` y `planning.md` para la v2.0 | Documentación de la entrega |

## 5. Checklist de entrega v2.0

- [ ] `mvn test` pasa en verde
- [ ] La aplicación arranca con MySQL en Docker e importa `candidaturas.csv`
- [ ] Cada pantalla se ha probado: listado, filtros, cambio de estado, detalle, formularios, empresas y entrevistas
- [ ] Tag `v2.0` en `main`
- [ ] Vídeo de 5 a 10 minutos con los dos mostrando la aplicación funcionando
- [x] README con tecnologías, instrucciones de ejecución de cada versión y tabla de contribuciones
- [x] `schema.sql` y configuración externalizada
- [x] `AGENTS.md` y `planning.md` actualizados

---

# Entrega v1.0 (Archivos + CLI + Diseño)

## 1. Decisiones de diseño

- [x] **Fecha en `-añadir`:** `--fecha` es opcional; si no se indica, se usa la fecha de hoy.
- [x] **Campos opcionales en `-añadir`:** se admiten también `--enlace` y `--notas`.
- [x] **Identificadores:** el nuevo ID es el mayor existente + 1.
- [x] **Importación:** las candidaturas importadas se añaden al final con IDs nuevos. Las líneas no válidas se omiten y se informa de su número de línea.
- [x] **Filtros de `-listar`:** sin distinguir mayúsculas/minúsculas; `--empresa` busca el texto dentro del nombre; si se dan `--estado` y `--empresa`, deben cumplirse ambos.
- [x] **Punto y coma en los textos:** se sustituye por `,` al guardar.
- [x] **Argumentos:** se procesan a mano, sin librerías.
- [x] **Archivo de datos estropeado:** si `candidaturas.csv` tiene líneas mal, el programa avisa y no guarda nada, para no perder datos.
- [x] **Letra `ñ` en `-añadir`:** funciona en Windows; se acepta también `-anadir`.
- [x] **Comentarios:** solo Javadoc en clases y métodos públicos.
- [x] **Flujo de Git:** commits directos a `main`, cada uno desde su ordenador y con su cuenta.

## 2. Reparto de tareas

| Tarea | Francisco José | Alejandro |
|---|---|---|
| Modelo + repositorio + tests | ✔ | |
| Servicio + CLI + tests | | ✔ |
| Pantallas de Figma | ✔ | ✔ |
| Manual de estilo | ✔ | ✔ |
| README y CSV de prueba | ✔ | ✔ |
| Vídeo de demostración | ✔ | ✔ |

## 3. Registro de prompts

| Fecha | Quién | Herramienta | Prompt | Resultado / qué se hizo con él |
|---|---|---|---|---|
| 28/10/2026 | Francisco José | Claude (Claude Code) | "Cámbialo todo por el tracker de candidaturas" (a partir del enunciado completo) | Plan de la v1.0. Se decidió proyecto nuevo aparte, Figma nuevo y Javadoc sencillo |
| 28/10/2026 | Francisco José | Claude (Claude Code) | Implementar la v1.0 por capas (modelo, repositorio, servicio, cli) con los comandos del enunciado | Código y tests. Revisados y probados con `mvn package` y ejecutando cada comando |
| 28/10/2026 | Francisco José | Claude (Claude Code) | Genera un archivo CSV con 15 registros de candidaturas laborales para una aplicación de seguimiento de empleo. El CSV debe tener las siguientes columnas separadas por punto y coma: id;titulo;empresa;estado;fechaPostulacion;enlace;notas. Los estados posibles son: pendiente, entrevista, rechazado, aceptado. Incluye al menos 4 empresas diferentes y al menos 3 ofertas en cada estado. Las fechas deben estar en formato dd/MM/yyyy y abarcar los últimos 3 meses. Devuelve solo el CSV, sin explicaciones. | `candidaturas.csv`. Se comprobó que cumple las condiciones |
| 28/10/2026 | Francisco José | Claude (Claude Code) | Prototipo en Figma con las pantallas del enunciado y manual de estilo | Archivo de Figma del Tracker |
| 29/10/2026 | Francisco José | Claude (Claude Code) | Guion de los commits de la v1.0, con el reparto entre los dos y el título y la descripción de cada uno | `COMMITS-v1.0.md`. Cada uno ha hecho sus commits desde su cuenta |
| 29/10/2026 | Francisco José | Claude (Claude Code) | Prepara el README: descripción, tecnologías, datos de prueba, cómo compilar y ejecutar con un ejemplo de cada comando y los errores | Apartados del README. Las salidas de ejemplo se comprobaron ejecutando cada comando |
| 29/10/2026 | Francisco José | Claude (Claude Code) | Apartado de diseño del README con la paleta y las tipografías del Figma | Apartado «Diseño», con los colores y las fuentes sacados de las variables y los estilos del Figma |
| 29/10/2026 | Francisco José | Claude (Claude Code) | Tabla de contribuciones y checklist de la entrega | Apartado «Contribuciones» del README y checklist de este archivo, revisados con el historial de commits |
