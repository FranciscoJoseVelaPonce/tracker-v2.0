package es.tracker.servicio;

import es.tracker.modelo.Empresa;
import es.tracker.modelo.Entrevista;
import es.tracker.modelo.Estado;
import es.tracker.modelo.Oferta;
import es.tracker.modelo.ResultadoEntrevista;
import es.tracker.modelo.TipoEntrevista;
import es.tracker.repositorio.RepositorioEntrevistas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntrevistaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 26);
    private static final Oferta OFERTA = new Oferta(3, "Programador Backend",
            new Empresa(1, "Accenture", "Consultoría", "", HOY), Estado.ENTREVISTA, HOY, "", "", null, "");

    @Mock
    private RepositorioEntrevistas repositorio;

    @InjectMocks
    private EntrevistaService servicio;

    private static Entrevista entrevista(Integer id) {
        return new Entrevista(id, OFERTA, HOY, TipoEntrevista.TECNICA, "", ResultadoEntrevista.PENDIENTE);
    }

    @Test
    @DisplayName("Crear una entrevista la guarda y devuelve su id")
    void crearGuarda() {
        when(repositorio.guardar(entrevista(null))).thenReturn(entrevista(5));

        assertEquals(5, servicio.crear(entrevista(null)).id());
    }

    @Test
    @DisplayName("Una entrevista nueva no puede venir con id")
    void crearConIdDaError() {
        assertThrows(IllegalArgumentException.class, () -> servicio.crear(entrevista(5)));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    @DisplayName("No se puede crear una entrevista de una candidatura que no está guardada")
    void entrevistaSinOfertaGuardada() {
        Oferta sinGuardar = new Oferta(null, "Analista", OFERTA.empresa(), Estado.PENDIENTE, HOY, "", "", null, "");

        assertThrows(IllegalArgumentException.class, () -> new Entrevista(null, sinGuardar, HOY,
                TipoEntrevista.ONLINE, "", ResultadoEntrevista.PENDIENTE));
    }

    @Test
    @DisplayName("Editar o borrar una entrevista que no existe da error")
    void editarOBorrarUnaQueNoExiste() {
        when(repositorio.actualizar(entrevista(9))).thenReturn(false);
        when(repositorio.borrar(9)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> servicio.actualizar(entrevista(9)));
        assertThrows(NoSuchElementException.class, () -> servicio.borrar(9));
    }
}
