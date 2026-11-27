package es.tracker.ui;

import javafx.application.Application;

/**
 * Punto de entrada del programa. Arranca JavaFX desde una clase que no hereda de
 * Application para que se pueda ejecutar desde IntelliJ o desde un jar sin el
 * error "JavaFX runtime components are missing".
 */
public class Launcher {

    /**
     * Arranca la aplicacion.
     *
     * @param args no se usan
     */
    public static void main(String[] args) {
        Application.launch(AppTracker.class, args);
    }
}
