package es.tracker.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.util.Optional;

/**
 * Mensajes al usuario: errores, avisos y preguntas de confirmacion.
 */
public final class Dialogos {

    private Dialogos() {
    }

    /**
     * Muestra un error y espera a que el usuario lo cierre.
     *
     * @param titulo  que ha fallado
     * @param mensaje explicacion para el usuario
     */
    public static void error(String titulo, String mensaje) {
        mostrar(Alert.AlertType.ERROR, titulo, mensaje);
    }

    /**
     * Muestra un mensaje informativo y espera a que el usuario lo cierre.
     *
     * @param titulo  titulo del mensaje
     * @param mensaje texto del mensaje
     */
    public static void informacion(String titulo, String mensaje) {
        mostrar(Alert.AlertType.INFORMATION, titulo, mensaje);
    }

    /**
     * Pregunta antes de hacer algo que no se puede deshacer. El boton por defecto
     * es Cancelar, asi pulsar Enter sin querer no borra nada.
     *
     * @param titulo   titulo de la pregunta
     * @param pregunta texto de la pregunta
     * @return true si el usuario pulsa Aceptar
     */
    public static boolean confirmar(String titulo, String pregunta) {
        ButtonType aceptar = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION, pregunta, aceptar, cancelar);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        ((Button) alerta.getDialogPane().lookupButton(aceptar)).setDefaultButton(false);
        ((Button) alerta.getDialogPane().lookupButton(cancelar)).setDefaultButton(true);
        Optional<ButtonType> respuesta = alerta.showAndWait();
        return respuesta.isPresent() && respuesta.get() == aceptar;
    }

    private static void mostrar(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }
}
