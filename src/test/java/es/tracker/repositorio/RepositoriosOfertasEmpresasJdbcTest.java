package es.tracker.repositorio;

import es.tracker.config.FabricaConexiones;
import es.tracker.modelo.Empresa;
import es.tracker.modelo.Estado;
import es.tracker.modelo.FiltroOferta;
import es.tracker.modelo.Oferta;
import es.tracker.modelo.ResumenEmpresa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepositoriosOfertasEmpresasJdbcTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 26);

    private RepositorioEmpresasJdbc empresas;
    private RepositorioOfertasJdbc ofertas;
    private Empresa indra;
    private Empresa accenture;

    @BeforeEach
    void preparar() {
        FabricaConexiones fabrica = BaseDatosPrueba.nueva();
        empresas = new RepositorioEmpresasJdbc(fabrica);
        ofertas = new RepositorioOfertasJdbc(fabrica);
        indra = empresas.guardar(new Empresa(null, "Indra", "Tecnología", "https://www.indracompany.com", HOY));
        accenture = empresas.guardar(new Empresa(null, "Accenture", "Consultoría", null, HOY));
    }

    private Oferta guardarOferta(String titulo, Empresa empresa, Estado estado, LocalDate fecha, Double salario) {
        return ofertas.guardar(new Oferta(null, titulo, empresa, estado, fecha, "", "", salario, ""));
    }

    @Test
    @DisplayName("Una candidatura guardada se lee con los datos de su empresa (JOIN)")
    void guardarYLeerConEmpresa() {
        Oferta guardada = guardarOferta("Desarrollador Java", indra, Estado.PENDIENTE, HOY, 24000.0);

        Oferta leida = ofertas.buscarPorId(guardada.id()).orElseThrow();

        assertEquals("Desarrollador Java", leida.titulo());
        assertEquals("Indra", leida.empresa().nombre());
        assertEquals("Tecnología", leida.empresa().sector());
        assertEquals(24000.0, leida.salario());
    }

    @Test
    @DisplayName("El salario vacío se guarda como NULL y se lee como null, no como 0")
    void salarioNulo() {
        Oferta guardada = guardarOferta("Becario", indra, Estado.PENDIENTE, HOY, null);

        assertNull(ofertas.buscarPorId(guardada.id()).orElseThrow().salario());
    }

    @Test
    @DisplayName("Los filtros se combinan: empresa + estado + salario y fecha + texto")
    void filtrosCombinados() {
        guardarOferta("Desarrollador Java", indra, Estado.PENDIENTE, HOY.minusDays(30), 22000.0);
        guardarOferta("Prácticas DAM", indra, Estado.PENDIENTE, HOY.minusDays(10), 12000.0);
        guardarOferta("Programador Backend", accenture, Estado.PENDIENTE, HOY.minusDays(5), 30000.0);
        guardarOferta("Analista Java", indra, Estado.RECHAZADO, HOY.minusDays(2), 28000.0);

        List<Oferta> porEmpresaEstadoYSalario = ofertas.buscar(
                new FiltroOferta(null, Estado.PENDIENTE, indra.id(), 20000.0, null, null, null));
        List<Oferta> porFechaYTexto = ofertas.buscar(
                new FiltroOferta("java", null, null, null, null, HOY.minusDays(7), HOY));

        assertEquals(List.of("Desarrollador Java"), porEmpresaEstadoYSalario.stream().map(Oferta::titulo).toList());
        assertEquals(List.of("Analista Java"), porFechaYTexto.stream().map(Oferta::titulo).toList());
        assertEquals(4, ofertas.buscar(FiltroOferta.TODAS).size());
    }

    @Test
    @DisplayName("El texto busca también en el nombre de la empresa")
    void filtroDeTextoPorEmpresa() {
        guardarOferta("Programador Backend", accenture, Estado.PENDIENTE, HOY, null);
        guardarOferta("Desarrollador Java", indra, Estado.PENDIENTE, HOY, null);

        assertEquals(1, ofertas.buscar(new FiltroOferta("ACCEN", null, null, null, null, null, null)).size());
    }

    @Test
    @DisplayName("Editar una candidatura guarda todos los campos nuevos")
    void actualizarOferta() {
        Oferta oferta = guardarOferta("Becario", indra, Estado.PENDIENTE, HOY, null);
        Oferta editada = new Oferta(oferta.id(), "Becario de desarrollo", accenture, Estado.ENTREVISTA, HOY,
                "https://empleo.ejemplo/2", "Prácticas de 6 meses", 9000.0, "Llamar el lunes");

        assertTrue(ofertas.actualizar(editada));
        assertEquals(editada, ofertas.buscarPorId(oferta.id()).orElseThrow());
    }

    @Test
    @DisplayName("Cambiar el estado solo modifica esa candidatura")
    void cambiarEstado() {
        Oferta oferta = guardarOferta("Desarrollador Java", indra, Estado.PENDIENTE, HOY, null);

        assertTrue(ofertas.cambiarEstado(oferta.id(), Estado.ENTREVISTA));
        assertEquals(Estado.ENTREVISTA, ofertas.buscarPorId(oferta.id()).orElseThrow().estado());
        assertFalse(ofertas.cambiarEstado(999, Estado.ACEPTADO));
    }

    @Test
    @DisplayName("Borrar una candidatura la quita, y si ya no existe devuelve false")
    void borrarOferta() {
        Oferta oferta = guardarOferta("Desarrollador Java", indra, Estado.PENDIENTE, HOY, null);

        assertTrue(ofertas.borrar(oferta.id()));
        assertTrue(ofertas.buscarPorId(oferta.id()).isEmpty());
        assertFalse(ofertas.borrar(oferta.id()));
    }

    @Test
    @DisplayName("Borrar una empresa borra también sus candidaturas y no toca las de otras")
    void borrarEmpresaConSusOfertas() {
        guardarOferta("Desarrollador Java", indra, Estado.PENDIENTE, HOY, null);
        guardarOferta("Programador Backend", accenture, Estado.PENDIENTE, HOY, null);

        assertTrue(empresas.borrar(indra.id()));

        assertEquals(List.of("Accenture"), empresas.listar().stream().map(Empresa::nombre).toList());
        assertEquals(1, ofertas.buscar(FiltroOferta.TODAS).size());
    }

    @Test
    @DisplayName("El resumen de empresas cuenta sus candidaturas (LEFT JOIN + GROUP BY)")
    void resumenDeEmpresas() {
        guardarOferta("Desarrollador Java", indra, Estado.PENDIENTE, HOY, null);
        guardarOferta("Prácticas DAM", indra, Estado.PENDIENTE, HOY, null);

        List<ResumenEmpresa> resumen = empresas.listarConNumeroDeOfertas();

        assertEquals("Accenture", resumen.get(0).empresa().nombre());
        assertEquals(0, resumen.get(0).ofertas());
        assertEquals(2, resumen.get(1).ofertas());
    }

    @Test
    @DisplayName("Buscar empresa por nombre no distingue mayúsculas")
    void buscarEmpresaPorNombre() {
        assertEquals(indra, empresas.buscarPorNombre("INDRA").orElseThrow());
        assertTrue(empresas.buscarPorNombre("Sopra").isEmpty());
    }

    @Test
    @DisplayName("No se pueden guardar dos empresas con el mismo nombre")
    void nombreDeEmpresaUnico() {
        assertThrows(PersistenciaException.class,
                () -> empresas.guardar(new Empresa(null, "Indra", "Otro", null, HOY)));
    }
}
