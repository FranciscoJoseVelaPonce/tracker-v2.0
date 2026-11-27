package es.tracker.repositorio;

import es.tracker.config.FabricaConexiones;
import es.tracker.modelo.Empresa;
import es.tracker.modelo.Estado;
import es.tracker.modelo.FiltroOferta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ImportacionJdbcTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 26);

    private RepositorioEmpresasJdbc empresas;
    private RepositorioOfertasJdbc ofertas;

    @BeforeEach
    void preparar() {
        FabricaConexiones fabrica = BaseDatosPrueba.nueva();
        empresas = new RepositorioEmpresasJdbc(fabrica);
        ofertas = new RepositorioOfertasJdbc(fabrica);
        empresas.guardar(new Empresa(null, "Indra", "Tecnología", null, HOY));
    }

    @Test
    @DisplayName("Importar crea las empresas que faltan y reutiliza las que ya existen")
    void importarCreaEmpresas() {
        List<FilaCsv> filas = List.of(
                new FilaCsv("Desarrollador Java", "indra", Estado.PENDIENTE, HOY, "", ""),
                new FilaCsv("Analista", "NTT Data", Estado.ACEPTADO, HOY, "", ""),
                new FilaCsv("Becario", "NTT Data", Estado.PENDIENTE, HOY, "", ""));

        int creadas = ofertas.importar(filas, HOY);

        assertEquals(1, creadas);
        assertEquals(List.of("Indra", "NTT Data"), empresas.listar().stream().map(Empresa::nombre).toList());
        assertEquals(RepositorioOfertasJdbc.SECTOR_DESCONOCIDO, empresas.buscarPorNombre("NTT Data").orElseThrow().sector());
        assertEquals(3, ofertas.buscar(FiltroOferta.TODAS).size());
    }

    @Test
    @DisplayName("El CSV de datos de prueba del proyecto se importa entero")
    void importarElCsvDelProyecto() throws Exception {
        LectorCsvOfertas.Lectura lectura = new LectorCsvOfertas().leer(Path.of("candidaturas.csv"));

        ofertas.importar(lectura.filas(), HOY);

        assertEquals(15, ofertas.buscar(FiltroOferta.TODAS).size());
        assertEquals(5, empresas.listar().size());
    }
}
