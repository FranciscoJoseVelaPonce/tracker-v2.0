# AGENTS.md — Instrucciones para asistentes de IA

> Este archivo recoge las reglas que se dan a las herramientas de IA generativa usadas en el proyecto, como pide el enunciado (apartado de Entregables). Los prompts empleados se registran en `planning.md`.

## Equipo

- Francisco José Vela Ponce
- Alejandro López García

## Proyecto

**Tracker de Candidaturas Laborales**: aplicación Java para hacer seguimiento de candidaturas a ofertas de empleo (ejercicio integrador del 1er trimestre, Acceso a Datos + Desarrollo de Interfaces).

Entrega actual: **v2.0 (JDBC + JavaFX)**. La v1.0 (archivos + CLI) está cerrada en el tag `v1.0`. En esta entrega **nada de Hibernate, JPA ni Ollama**: eso es de la v3.0.

## Reglas obligatorias de la v2.0

1. Java 24 o superior, gestionado con Maven.
2. Persistencia en **MySQL con JDBC**: `Connection`, `PreparedStatement` y `ResultSet`, siempre con `try-with-resources`.
3. Nunca se concatenan valores en el SQL: siempre parámetros `?`.
4. Tablas `empresa`, `oferta` y `entrevista`, creadas con `schema.sql`.
5. Las operaciones que tocan varias tablas van en una **transacción** (`setAutoCommit(false)`, `commit`, `rollback`).
6. La configuración de la conexión está **fuera del código**: `config.properties` o variables de entorno.
7. Interfaz en **JavaFX**. Todas las pantallas se maquetan en **Scene Builder** (`.fxml`); en Java solo van los eventos, la carga de datos, las celdas de las tablas y la validación.
8. Los controladores **solo hablan con los servicios**, nunca con los repositorios ni con JDBC.
9. Los errores se muestran con un mensaje claro (`Alert` o una etiqueta en el formulario). La aplicación nunca se cierra con un error ni enseña trazas.

## Modelo de datos

```
EMPRESA    (id, nombre, sector, web, fecha_registro)
OFERTA     (id, id_empresa FK, titulo, descripcion, estado, fecha_postulacion, enlace, salario, notas)
ENTREVISTA (id, id_oferta FK, fecha, tipo, notas, resultado)
```

- Estados de una candidatura: `pendiente`, `entrevista`, `rechazado`, `aceptado` (enum `Estado`).
- Tipos de entrevista: `telefónica`, `técnica`, `presencial`, `online` (enum `TipoEntrevista`).
- Resultados: `pendiente`, `positivo`, `negativo` (enum `ResultadoEntrevista`).
- Fechas con `LocalDate`, en pantalla como `dd/MM/yyyy`.
- `salario` es opcional: `null` en Java y `NULL` en la base de datos, nunca 0.

## Estructura de paquetes

```
src/main/java/es/tracker/
  modelo/          records y enums del dominio
  config/          ConfiguracionBD, FabricaConexiones, InicializadorBD
  repositorio/     interfaces + implementaciones JDBC + lector del CSV
  servicio/        OfertaService, EmpresaService, EntrevistaService
  ui/              AppTracker, Launcher, Navegador, Dialogos, Formatos
  ui/controlador/  un controlador por pantalla
```

## Calidad del código

- Código limpio en paquetes, sin código muerto ni comentarios de depuración.
- Nombres en **español**.
- **Javadoc** en clases y métodos públicos, escrito de forma sencilla. **Ningún otro comentario.**
- Sin código duplicado.
- `PersistenciaException` envuelve las `SQLException` para que la interfaz no dependa de JDBC.

## Pruebas

- JUnit 5 y Mockito para los servicios (repositorios simulados).
- Repositorios JDBC probados con **H2 en memoria** en modo MySQL, cargando el mismo `schema.sql`. Nunca contra la base de datos real.

## Control de versiones

- Un único repositorio en GitHub; los dos miembros son colaboradores.
- Commits pequeños, uno por cada parte terminada, con mensajes descriptivos en español.
- Cada uno hace sus commits con su cuenta. No se reescribe el historial.
- La entrega se etiqueta en `main` como `v2.0`.

## Cómo debe trabajar la IA en este repositorio

- Seguir estas reglas por encima de cualquier otra preferencia de estilo.
- **Explicar y preguntar antes de hacer cualquier cambio.** Si algo no está claro o hay que decidir, preguntar en lugar de inventar. Las decisiones se anotan en `planning.md`.
- Explicar qué cambia y por qué, para que el equipo lo revise y lo entienda.
- Cada miembro debe revisar y comprender el código que se incorpora.
- Registrar los prompts usados en `planning.md`.
