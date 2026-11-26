package es.tracker.repositorio;

import es.tracker.modelo.Empresa;
import es.tracker.modelo.Estado;
import es.tracker.modelo.Oferta;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

/**
 * Convierte filas de las consultas en objetos del modelo y ayuda a rellenar
 * los parametros que pueden ser null. Lo usan todos los repositorios JDBC para
 * no repetir codigo.
 */
final class MapeadorJdbc {

    /** Columnas de la empresa con un alias, para poder juntarlas con otras tablas. */
    static final String COLUMNAS_EMPRESA =
            "e.id AS e_id, e.nombre AS e_nombre, e.sector AS e_sector, e.web AS e_web, e.fecha_registro AS e_fecha_registro";

    /** Columnas de la oferta con un alias. Siempre se piden junto con las de la empresa. */
    static final String COLUMNAS_OFERTA =
            "o.id AS o_id, o.titulo AS o_titulo, o.descripcion AS o_descripcion, o.estado AS o_estado, "
                    + "o.fecha_postulacion AS o_fecha_postulacion, o.enlace AS o_enlace, o.salario AS o_salario, "
                    + "o.notas AS o_notas, " + COLUMNAS_EMPRESA;

    /** Ofertas con su empresa: JOIN entre oferta y empresa. */
    static final String SELECT_OFERTA = "SELECT " + COLUMNAS_OFERTA
            + " FROM oferta o JOIN empresa e ON e.id = o.id_empresa";

    private MapeadorJdbc() {
    }

    /**
     * Convierte las columnas e_* de la fila actual en una empresa.
     *
     * @param fila fila de una consulta que incluye COLUMNAS_EMPRESA
     * @return la empresa
     * @throws SQLException si falla la lectura
     */
    static Empresa empresa(ResultSet fila) throws SQLException {
        return new Empresa(
                fila.getInt("e_id"),
                fila.getString("e_nombre"),
                fila.getString("e_sector"),
                fila.getString("e_web"),
                fila.getObject("e_fecha_registro", LocalDate.class));
    }

    /**
     * Convierte las columnas o_* y e_* de la fila actual en una oferta con su empresa.
     *
     * @param fila fila de una consulta que incluye COLUMNAS_OFERTA
     * @return la oferta
     * @throws SQLException si falla la lectura
     */
    static Oferta oferta(ResultSet fila) throws SQLException {
        BigDecimal salario = fila.getBigDecimal("o_salario");
        return new Oferta(
                fila.getInt("o_id"),
                fila.getString("o_titulo"),
                empresa(fila),
                Estado.desdeTexto(fila.getString("o_estado")),
                fila.getObject("o_fecha_postulacion", LocalDate.class),
                fila.getString("o_enlace"),
                fila.getString("o_descripcion"),
                salario == null ? null : salario.doubleValue(),
                fila.getString("o_notas"));
    }

    static void textoOpcional(PreparedStatement sentencia, int posicion, String texto) throws SQLException {
        if (texto == null || texto.isBlank()) {
            sentencia.setNull(posicion, Types.VARCHAR);
        } else {
            sentencia.setString(posicion, texto);
        }
    }

    static void salario(PreparedStatement sentencia, int posicion, Double salario) throws SQLException {
        if (salario == null) {
            sentencia.setNull(posicion, Types.DECIMAL);
        } else {
            sentencia.setBigDecimal(posicion, BigDecimal.valueOf(salario));
        }
    }

    static int idGenerado(PreparedStatement sentencia) throws SQLException {
        try (ResultSet claves = sentencia.getGeneratedKeys()) {
            if (!claves.next()) {
                throw new SQLException("La base de datos no ha devuelto el id generado.");
            }
            return claves.getInt(1);
        }
    }
}
