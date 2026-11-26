package es.tracker.repositorio;

import es.tracker.config.FabricaConexiones;
import es.tracker.modelo.Empresa;
import es.tracker.modelo.ResumenEmpresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Guarda las empresas en la tabla empresa usando JDBC.
 */
public class RepositorioEmpresasJdbc implements RepositorioEmpresas {

    private static final String SELECT_EMPRESA = "SELECT " + MapeadorJdbc.COLUMNAS_EMPRESA + " FROM empresa e";

    private final FabricaConexiones fabrica;

    /**
     * Crea el repositorio.
     *
     * @param fabrica de donde se sacan las conexiones
     */
    public RepositorioEmpresasJdbc(FabricaConexiones fabrica) {
        this.fabrica = fabrica;
    }

    @Override
    public List<Empresa> listar() {
        String sql = SELECT_EMPRESA + " ORDER BY e.nombre";
        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet filas = sentencia.executeQuery()) {
            List<Empresa> empresas = new ArrayList<>();
            while (filas.next()) {
                empresas.add(MapeadorJdbc.empresa(filas));
            }
            return empresas;
        } catch (SQLException e) {
            throw new PersistenciaException("No se han podido leer las empresas.", e);
        }
    }

    @Override
    public List<ResumenEmpresa> listarConNumeroDeOfertas() {
        String sql = "SELECT " + MapeadorJdbc.COLUMNAS_EMPRESA + ", COUNT(o.id) AS num_ofertas"
                + " FROM empresa e LEFT JOIN oferta o ON o.id_empresa = e.id"
                + " GROUP BY e.id, e.nombre, e.sector, e.web, e.fecha_registro"
                + " ORDER BY e.nombre";
        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql);
             ResultSet filas = sentencia.executeQuery()) {
            List<ResumenEmpresa> resumen = new ArrayList<>();
            while (filas.next()) {
                resumen.add(new ResumenEmpresa(MapeadorJdbc.empresa(filas), filas.getInt("num_ofertas")));
            }
            return resumen;
        } catch (SQLException e) {
            throw new PersistenciaException("No se han podido leer las empresas.", e);
        }
    }

    @Override
    public Optional<Empresa> buscarPorId(int id) {
        return buscarUna(SELECT_EMPRESA + " WHERE e.id = ?", id);
    }

    @Override
    public Optional<Empresa> buscarPorNombre(String nombre) {
        return buscarUna(SELECT_EMPRESA + " WHERE LOWER(e.nombre) = LOWER(?)", nombre.strip());
    }

    @Override
    public Empresa guardar(Empresa empresa) {
        try (Connection conexion = fabrica.abrir()) {
            return insertar(conexion, empresa);
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido guardar la empresa " + empresa.nombre() + ".", e);
        }
    }

    @Override
    public boolean actualizar(Empresa empresa) {
        String sql = "UPDATE empresa SET nombre = ?, sector = ?, web = ?, fecha_registro = ? WHERE id = ?";
        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            rellenar(sentencia, empresa);
            sentencia.setInt(5, empresa.id());
            return sentencia.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido actualizar la empresa " + empresa.nombre() + ".", e);
        }
    }

    @Override
    public boolean borrar(int id) {
        try (Connection conexion = fabrica.abrir()) {
            conexion.setAutoCommit(false);
            try {
                ejecutar(conexion, "DELETE FROM entrevista WHERE id_oferta IN (SELECT id FROM oferta WHERE id_empresa = ?)", id);
                ejecutar(conexion, "DELETE FROM oferta WHERE id_empresa = ?", id);
                boolean borrada = ejecutar(conexion, "DELETE FROM empresa WHERE id = ?", id) == 1;
                conexion.commit();
                return borrada;
            } catch (SQLException e) {
                conexion.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido borrar la empresa. No se ha cambiado nada.", e);
        }
    }

    /**
     * Inserta una empresa usando una conexion que ya esta abierta, para poder hacerlo
     * dentro de una transaccion de otro repositorio.
     *
     * @param conexion conexion abierta
     * @param empresa  la empresa, sin id
     * @return la empresa con su id
     * @throws SQLException si falla la insercion
     */
    static Empresa insertar(Connection conexion, Empresa empresa) throws SQLException {
        String sql = "INSERT INTO empresa (nombre, sector, web, fecha_registro) VALUES (?, ?, ?, ?)";
        try (PreparedStatement sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            rellenar(sentencia, empresa);
            sentencia.executeUpdate();
            return empresa.conId(MapeadorJdbc.idGenerado(sentencia));
        }
    }

    private Optional<Empresa> buscarUna(String sql, Object parametro) {
        try (Connection conexion = fabrica.abrir();
             PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setObject(1, parametro);
            try (ResultSet filas = sentencia.executeQuery()) {
                return filas.next() ? Optional.of(MapeadorJdbc.empresa(filas)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se ha podido buscar la empresa.", e);
        }
    }

    private static void rellenar(PreparedStatement sentencia, Empresa empresa) throws SQLException {
        sentencia.setString(1, empresa.nombre());
        sentencia.setString(2, empresa.sector());
        MapeadorJdbc.textoOpcional(sentencia, 3, empresa.web());
        sentencia.setObject(4, empresa.fechaRegistro());
    }

    private static int ejecutar(Connection conexion, String sql, int id) throws SQLException {
        try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {
            sentencia.setInt(1, id);
            return sentencia.executeUpdate();
        }
    }
}
