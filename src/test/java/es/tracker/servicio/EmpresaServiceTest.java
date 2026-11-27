package es.tracker.servicio;

import es.tracker.modelo.Empresa;
import es.tracker.repositorio.RepositorioEmpresas;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmpresaServiceTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 26);

    @Mock
    private RepositorioEmpresas repositorio;

    @InjectMocks
    private EmpresaService servicio;

    @Test
    @DisplayName("No se puede crear una empresa con un nombre que ya existe")
    void crearConNombreRepetido() {
        when(repositorio.buscarPorNombre("indra")).thenReturn(Optional.of(new Empresa(1, "Indra", "Tecnología", "", HOY)));

        assertThrows(IllegalArgumentException.class,
                () -> servicio.crear(new Empresa(null, "indra", "Tecnología", "", HOY)));
        verify(repositorio, never()).guardar(any());
    }

    @Test
    @DisplayName("Al editar una empresa se puede dejar su mismo nombre")
    void editarConservandoElNombre() {
        Empresa indra = new Empresa(1, "Indra", "Consultoría", "", HOY);
        when(repositorio.buscarPorNombre("Indra")).thenReturn(Optional.of(new Empresa(1, "Indra", "Tecnología", "", HOY)));
        when(repositorio.actualizar(indra)).thenReturn(true);

        assertDoesNotThrow(() -> servicio.actualizar(indra));
    }

    @Test
    @DisplayName("Una web que no empieza por http:// o https:// no es válida")
    void webNoValida() {
        when(repositorio.buscarPorNombre("Sopra")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> servicio.crear(new Empresa(null, "Sopra", "Consultoría", "sopra.com", HOY)));
    }

    @Test
    @DisplayName("Borrar una empresa que no existe da error")
    void borrarUnaQueNoExiste() {
        when(repositorio.borrar(42)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> servicio.borrar(42));
    }
}
