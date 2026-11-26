package es.tracker.repositorio;

import es.tracker.config.FabricaConexiones;
import es.tracker.modelo.Estado;
import es.tracker.modelo.FiltroOferta;
import es.tracker.modelo.Oferta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Guarda las candidaturas en la tabla oferta usando JDBC. Las consultas hacen
 * JOIN con empresa para devolver cada candidatura con su empresa.
 */
public class RepositorioOfertasJdbc implements RepositorioOfertas {

    private final FabricaConexiones fabrica;

    /**
     * Crea el repositorio.
     *
     * @param fabrica de donde se sacan las conexiones
     */
    public RepositorioOfertasJdbc(FabricaConexiones fabrica) {
        this.fabrica = fabrica;
    }

    /**
     * {@inheritDoc}
     * La consulta se monta solo con trozos fijos de SQL; los valores del filtro
     * van siempre como parametros para evitar inyeccion SQL.
     */
    @Override
    public List<Oferta> buscar(FiltroOferta filtro) {
        StringBuilder sql = new StringBuilder(MapeadorJdbc.SELECT_OFERTA).append(" WHERE 1 = 1");
        List<Object> parametros = new ArrayList<>();
        if (filtro.texto() != null) {
            sql.append(" AND (LOWER(o.titulo) LIKE ? OR LOWER(e.nombre) LIKE ?)");
            String patron = "%" + filtro.texto().toLowerCase(Locale.ROOT) + "%";
            parametros.add(patron);
            parametros.add(patron);
        }
        if (filtro.estado() != null) {
            sql.append(" AND o.estado = ?");
            parametros.add(filtro.estado().texto());
        }
        if (filtro.idEmpresa() != null) {
            sql.append(" AND o.id_empresa = ?");
            parametros.add(filtro.idEmpresa());
        }
        if (filtro.salarioMin() != null) {
            sql.append(" AND o.salario >= ?");
            parametros.add(filtro.salarioMin());
        }
        if (filtro.salarioMax() != null) {
            sql.append(" AND o.salario <= ?");
            parametros.add(filtro.salarioMax());
        }
        if (filtro.desde() != null) {
            sql.append(" AND o.fecha_postulacion >= ?");
            parametros.add(filtro.desde());
        }
        if (filtro.hasta() != null) {
            sql.append(" AND o.fecha_postulacion <= ?");
            parametros.add(filtro.hasta());
        }
        sql.append(" ORDER BY o.fecha_postulacion DESC, o.id DESC");

        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                sentencia.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet filas = sentencia.executeQuery()) {
                List<Oferta> ofertas = new ArrayList<>();
                while (filas.next()) {
                    ofertas.add(MapeadorJdbc.oferta(filas));
                }
                return ofertas;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se han podido buscar las candidaturas.", e);
        }
    }

    @Override
    public Optional<Oferta> buscarPorId(int id) {
        String sql = MapeadorJdbc.SELECT_OFERTA + " WHERE o.id = ?";
        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            try (ResultSet filas = sentencia.executeQuery()) {
                return filas.next() ? Optional.of(MapeadorJdbc.oferta(filas)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido buscar la candidatura " + id + ".", e);
        }
    }

    @Override
    public Oferta guardar(Oferta oferta) {
        try (Connection conexion = fabrica.abrir()) {
            return insertar(conexion, oferta);
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido guardar la candidatura " + oferta.titulo() + ".", e);
        }
    }

    @Override
    public boolean actualizar(Oferta oferta) {
        String sql = "UPDATE oferta SET id_empresa = ?, titulo = ?, descripcion = ?, estado = ?, fecha_postulacion = ?, "
                + "enlace = ?, salario = ?, notas = ? WHERE id = ?";
        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            rellenar(sentencia, oferta);
            sentencia.setInt(9, oferta.id());
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido actualizar la candidatura " + oferta.titulo() + ".", e);
        }
    }

    @Override
    public boolean cambiarEstado(int id, Estado estado) {
        String sql = "UPDATE oferta SET estado = ? WHERE id = ?";
        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setString(1, estado.texto());
            sentencia.setInt(2, id);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido cambiar el estado de la candidatura " + id + ".", e);
        }
    }

    @Override
    public boolean borrar(int id) {
        try (Connection conexion = fabrica.abrir()) {
            conexion.setAutoCommit(false);
            try {
                borrarPorId(conexion, "DELETE FROM entrevista WHERE id_oferta = ?", id);
                boolean borrada = borrarPorId(conexion, "DELETE FROM oferta WHERE id = ?", id) == 1;
                conexion.commit();
                return borrada;
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido borrar la candidatura. No se ha cambiado nada.", e);
        }
    }

    private static Oferta insertar(Connection conexion, Oferta oferta) throws SQLException {
        String sql = "INSERT INTO oferta (id_empresa, titulo, descripcion, estado, fecha_postulacion, enlace, salario, notas) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            rellenar(sentencia, oferta);
            sentencia.executeUpdate();
            return oferta.conId(MapeadorJdbc.idGenerado(sentencia));
        }
    }

    private static void rellenar(PreparedStatement sentencia, Oferta oferta) throws SQLException {
        sentencia.setInt(1, oferta.empresa().id());
        sentencia.setString(2, oferta.titulo());
        MapeadorJdbc.textoOpcional(sentencia, 3, oferta.descripcion());
        sentencia.setString(4, oferta.estado().texto());
        sentencia.setObject(5, oferta.fechaPostulacion());
        MapeadorJdbc.textoOpcional(sentencia, 6, oferta.enlace());
        MapeadorJdbc.salario(sentencia, 7, oferta.salario());
        MapeadorJdbc.textoOpcional(sentencia, 8, oferta.notas());
    }

    private static int borrarPorId(Connection conexion, String sql, int id) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate();
        }
    }
}
