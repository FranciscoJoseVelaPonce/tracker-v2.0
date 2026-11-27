package es.tracker.repositorio;

import es.tracker.config.FabricaConexiones;
import es.tracker.modelo.Entrevista;
import es.tracker.modelo.ResultadoEntrevista;
import es.tracker.modelo.TipoEntrevista;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda las entrevistas en la tabla entrevista usando JDBC. Las consultas hacen
 * JOIN con oferta y empresa para devolver cada entrevista con su candidatura.
 */
public class RepositorioEntrevistasJdbc implements RepositorioEntrevistas {

    private static final String SELECT_ENTREVISTA = "SELECT en.id AS en_id, en.fecha AS en_fecha, en.tipo AS en_tipo, "
            + "en.notas AS en_notas, en.resultado AS en_resultado, " + MapeadorJdbc.COLUMNAS_OFERTA
            + " FROM entrevista en JOIN oferta o ON o.id = en.id_oferta JOIN empresa e ON e.id = o.id_empresa";

    private final FabricaConexiones fabrica;

    /**
     * Crea el repositorio.
     *
     * @param fabrica de donde se sacan las conexiones
     */
    public RepositorioEntrevistasJdbc(FabricaConexiones fabrica) {
        this.fabrica = fabrica;
    }

    @Override
    public List<Entrevista> listar() {
        return consultar(SELECT_ENTREVISTA + " ORDER BY en.fecha DESC, en.id DESC", null);
    }

    @Override
    public List<Entrevista> listarDeOferta(int idOferta) {
        return consultar(SELECT_ENTREVISTA + " WHERE en.id_oferta = ? ORDER BY en.fecha, en.id", idOferta);
    }

    @Override
    public Optional<Entrevista> buscarPorId(int id) {
        return consultar(SELECT_ENTREVISTA + " WHERE en.id = ?", id).stream().findFirst();
    }

    @Override
    public Entrevista guardar(Entrevista entrevista) {
        String sql = "INSERT INTO entrevista (id_oferta, fecha, tipo, notas, resultado) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            rellenar(sentencia, entrevista);
            sentencia.executeUpdate();
            return entrevista.conId(MapeadorJdbc.idGenerado(sentencia));
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido guardar la entrevista.", e);
        }
    }

    @Override
    public boolean actualizar(Entrevista entrevista) {
        String sql = "UPDATE entrevista SET id_oferta = ?, fecha = ?, tipo = ?, notas = ?, resultado = ? WHERE id = ?";
        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            rellenar(sentencia, entrevista);
            sentencia.setInt(6, entrevista.id());
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido actualizar la entrevista.", e);
        }
    }

    @Override
    public boolean borrar(int id) {
        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement("DELETE FROM entrevista WHERE id = ?")) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido borrar la entrevista.", e);
        }
    }

    private List<Entrevista> consultar(String sql, Integer id) {
        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            if (id != null) {
                sentencia.setInt(1, id);
            }
            try (ResultSet filas = sentencia.executeQuery()) {
                List<Entrevista> entrevistas = new ArrayList<>();
                while (filas.next()) {
                    entrevistas.add(entrevista(filas));
                }
                return entrevistas;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se han podido leer las entrevistas.", e);
        }
    }

    private static Entrevista entrevista(ResultSet fila) throws SQLException {
        return new Entrevista(
                fila.getInt("en_id"),
                MapeadorJdbc.oferta(fila),
                fila.getObject("en_fecha", LocalDate.class),
                TipoEntrevista.desdeTexto(fila.getString("en_tipo")),
                fila.getString("en_notas"),
                ResultadoEntrevista.desdeTexto(fila.getString("en_resultado")));
    }

    private static void rellenar(PreparedStatement sentencia, Entrevista entrevista) throws SQLException {
        sentencia.setInt(1, entrevista.oferta().id());
        sentencia.setObject(2, entrevista.fecha());
        sentencia.setString(3, entrevista.tipo().texto());
        MapeadorJdbc.textoOpcional(sentencia, 4, entrevista.notas());
        sentencia.setString(5, entrevista.resultado().texto());
    }
}
