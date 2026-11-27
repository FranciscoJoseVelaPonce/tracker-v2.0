package es.tracker.ui.controlador;

import es.tracker.modelo.ResultadoEntrevista;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

/**
 * Celda que muestra el resultado de una entrevista como una insignia de color
 * (pendiente en amarillo, positivo en verde y negativo en rojo).
 *
 * @param <S> tipo de las filas de la tabla
 */
public class CeldaResultado<S> extends TableCell<S, ResultadoEntrevista> {

    private final Label insignia = new Label();

    /**
     * Crea la celda.
     */
    public CeldaResultado() {
        insignia.getStyleClass().add("insignia");
    }

    @Override
    protected void updateItem(ResultadoEntrevista resultado, boolean vacia) {
        super.updateItem(resultado, vacia);
        if (vacia || resultado == null) {
            setGraphic(null);
            return;
        }
        insignia.setText(resultado.toString());
        insignia.getStyleClass().removeIf(clase -> clase.startsWith("resultado-"));
        insignia.getStyleClass().add("resultado-" + resultado.texto());
        setGraphic(insignia);
    }
}
