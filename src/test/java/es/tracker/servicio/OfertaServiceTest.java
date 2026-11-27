package es.tracker.servicio;

import es.tracker.modelo.Empresa;
import es.tracker.modelo.Estado;
import es.tracker.modelo.FiltroOferta;
import es.tracker.modelo.Oferta;
import es.tracker.repositorio.FilaCsv;
import es.tracker.repositorio.LectorCsvOfertas;
import es.tracker.repositorio.RepositorioOfertas;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OfertaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 26);
    private static final Empresa INDRA = new Empresa(1, "Indra", "Tecnología", "https://www.indracompany.com", HOY);

    @Mock
    private RepositorioOfertas repositorio;

    @Mock
    private LectorCsvOfertas lectorCsv;

    private OfertaService servicio;

    @BeforeEach
    void preparar() {
        Clock reloj = Clock.fixed(Instant.parse("2026-11-26T10:00:00Z"), ZoneId.of("Europe/Madrid"));
        servicio = new OfertaService(repositorio, lectorCsv, reloj);
    }

    private static Oferta oferta(Integer id, LocalDate fecha, String enlace) {
        return new Oferta(id, "Desarrollador Java", INDRA, Estado.PENDIENTE, fecha, enlace, "", 24000.0, "");
    }

    @Test
    @DisplayName("Buscar sin filtro pide todas las candidaturas al repositorio")
    void buscarSinFiltroUsaElFiltroVacio() {
        when(repositorio.buscar(FiltroOferta.TODAS)).thenReturn(List.of(oferta(1, HOY, "")));

        assertEquals(1, servicio.buscar(null).size());
    }

    @Test
    @DisplayName("Un filtro con el salario mínimo mayor que el máximo no se puede crear")
    void filtroConRangoDeSalarioAlReves() {
        assertThrows(IllegalArgumentException.class,
                () -> new FiltroOferta(null, null, null, 30000.0, 20000.0, null, null));
    }

    @Test
    @DisplayName("Crear una candidatura válida la guarda en el repositorio")
    void crearGuardaLaCandidatura() {
        Oferta nueva = oferta(null, HOY, "https://empleo.ejemplo/1");
        when(repositorio.guardar(nueva)).thenReturn(nueva.conId(7));

        assertEquals(7, servicio.crear(nueva).id());
    }

    @Test
    @DisplayName("No se guarda una candidatura con fecha futura ni con un enlace mal escrito")
    void crearRechazaDatosNoValidos() {
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(oferta(null, HOY.plusDays(1), "")));
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(oferta(null, HOY, "www.sin-protocolo.com")));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    @DisplayName("No se puede crear una candidatura con una empresa que no está guardada")
    void crearConEmpresaSinGuardar() {
        Empresa sinGuardar = new Empresa(null, "Nueva", "Banca", "", HOY);
        Oferta oferta = new Oferta(null, "Analista", sinGuardar, Estado.PENDIENTE, HOY, "", "", null, "");

        assertThrows(IllegalArgumentException.class, () -> servicio.crear(oferta));
    }

    @Test
    @DisplayName("Cambiar el estado de una candidatura que no existe da error")
    void cambiarEstadoDeUnaQueNoExiste() {
        when(repositorio.cambiarEstado(99, Estado.ACEPTADO)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> servicio.cambiarEstado(99, Estado.ACEPTADO));
    }

    @Test
    @DisplayName("Ver una candidatura que no existe da un error con un mensaje claro")
    void verUnaQueNoExiste() {
        when(repositorio.buscarPorId(13)).thenReturn(Optional.empty());

        NoSuchElementException error = assertThrows(NoSuchElementException.class, () -> servicio.ver(13));
        assertEquals("No existe ninguna candidatura con el ID 13.", error.getMessage());
    }

    @Test
    @DisplayName("Importar guarda las filas válidas e informa de las líneas que estaban mal")
    void importarDevuelveElResumen() throws Exception {
        Path csv = Path.of("candidaturas.csv");
        List<FilaCsv> filas = List.of(new FilaCsv("Dev", "Indra", Estado.PENDIENTE, HOY, "", ""));
        when(lectorCsv.leer(csv)).thenReturn(new LectorCsvOfertas.Lectura(filas, List.of(4)));
        when(repositorio.importar(filas, HOY)).thenReturn(1);

        OfertaService.ResultadoImportacion resultado = servicio.importar(csv);

        assertEquals(1, resultado.importadas());
        assertEquals(1, resultado.empresasCreadas());
        assertEquals(List.of(4), resultado.lineasNoValidas());
    }

    @Test
    @DisplayName("Importar un CSV sin filas válidas no toca la base de datos")
    void importarSinFilasValidas() throws Exception {
        Path csv = Path.of("vacio.csv");
        when(lectorCsv.leer(csv)).thenReturn(new LectorCsvOfertas.Lectura(List.of(), List.of(2, 3)));

        assertEquals(0, servicio.importar(csv).importadas());
        verify(repositorio, never()).importar(any(), any());
    }
}
