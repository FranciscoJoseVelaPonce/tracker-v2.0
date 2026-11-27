package es.tracker.repositorio;

import es.tracker.config.FabricaConexiones;
import es.tracker.modelo.Empresa;
import es.tracker.modelo.Entrevista;
import es.tracker.modelo.Estado;
import es.tracker.modelo.Oferta;
import es.tracker.modelo.ResultadoEntrevista;
import es.tracker.modelo.TipoEntrevista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositorioEntrevistasJdbcTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 26);

    private RepositorioEmpresasJdbc empresas;
    private RepositorioOfertasJdbc ofertas;
    private RepositorioEntrevistasJdbc entrevistas;
    private Empresa accenture;
    private Oferta oferta;

    @BeforeEach
    void preparar() {
        FabricaConexiones fabrica = BaseDatosPrueba.nueva();
        empresas = new RepositorioEmpresasJdbc(fabrica);
        ofertas = new RepositorioOfertasJdbc(fabrica);
        entrevistas = new RepositorioEntrevistasJdbc(fabrica);
        accenture = empresas.guardar(new Empresa(null, "Accenture", "Consultoría", null, HOY));
        oferta = ofertas.guardar(new Oferta(null, "Programador Backend", accenture, Estado.ENTREVISTA, HOY,
                "", "", null, ""));
    }

    private Entrevista guardar(TipoEntrevista tipo, ResultadoEntrevista resultado, LocalDate fecha) {
        return entrevistas.guardar(new Entrevista(null, oferta, fecha, tipo, "", resultado));
    }

    @Test
    @DisplayName("Las entrevistas se leen con su candidatura y su empresa (JOIN de tres tablas)")
    void entrevistaConOfertaYEmpresa() {
        Entrevista guardada = entrevistas.guardar(new Entrevista(null, oferta, HOY.minusDays(1),
                TipoEntrevista.TELEFONICA, "Primera llamada", ResultadoEntrevista.POSITIVO));

        Entrevista leida = entrevistas.buscarPorId(guardada.id()).orElseThrow();

        assertEquals("Programador Backend", leida.oferta().titulo());
        assertEquals("Accenture", leida.oferta().empresa().nombre());
        assertEquals(TipoEntrevista.TELEFONICA, leida.tipo());
        assertEquals("Primera llamada", leida.notas());
    }

    @Test
    @DisplayName("Las entrevistas de una candidatura salen ordenadas por fecha")
    void entrevistasDeUnaOferta() {
        guardar(TipoEntrevista.PRESENCIAL, ResultadoEntrevista.PENDIENTE, HOY);
        guardar(TipoEntrevista.TECNICA, ResultadoEntrevista.POSITIVO, HOY.minusDays(10));

        assertEquals(TipoEntrevista.TECNICA, entrevistas.listarDeOferta(oferta.id()).getFirst().tipo());
        assertEquals(2, entrevistas.listar().size());
    }

    @Test
    @DisplayName("Editar y borrar una entrevista")
    void editarYBorrar() {
        Entrevista entrevista = guardar(TipoEntrevista.ONLINE, ResultadoEntrevista.PENDIENTE, HOY);
        Entrevista editada = new Entrevista(entrevista.id(), oferta, HOY, TipoEntrevista.ONLINE, "Ha ido bien",
                ResultadoEntrevista.POSITIVO);

        assertTrue(entrevistas.actualizar(editada));
        assertEquals(editada, entrevistas.buscarPorId(entrevista.id()).orElseThrow());
        assertTrue(entrevistas.borrar(entrevista.id()));
        assertFalse(entrevistas.borrar(entrevista.id()));
    }

    @Test
    @DisplayName("Borrar una candidatura borra también sus entrevistas en la misma transacción")
    void borrarOfertaConEntrevistas() {
        guardar(TipoEntrevista.TECNICA, ResultadoEntrevista.PENDIENTE, HOY);
        guardar(TipoEntrevista.ONLINE, ResultadoEntrevista.POSITIVO, HOY);

        assertTrue(ofertas.borrar(oferta.id()));

        assertTrue(entrevistas.listar().isEmpty());
    }

    @Test
    @DisplayName("Borrar una empresa borra sus candidaturas y las entrevistas de esas candidaturas")
    void borrarEmpresaEnCascada() {
        guardar(TipoEntrevista.TECNICA, ResultadoEntrevista.PENDIENTE, HOY);

        assertTrue(empresas.borrar(accenture.id()));

        assertTrue(entrevistas.listar().isEmpty());
        assertTrue(ofertas.buscarPorId(oferta.id()).isEmpty());
    }
}
