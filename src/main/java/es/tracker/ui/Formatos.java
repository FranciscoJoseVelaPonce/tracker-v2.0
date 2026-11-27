package es.tracker.ui;

import es.tracker.modelo.Oferta;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/**
 * Formatos que se repiten en todas las pantallas: fechas dd/MM/yyyy, salarios en
 * euros y desplegables con un texto cuando no hay nada elegido.
 */
public final class Formatos {

    private static final NumberFormat EUROS = NumberFormat.getNumberInstance(Locale.forLanguageTag("es-ES"));

    static {
        EUROS.setMaximumFractionDigits(2);
    }

    private Formatos() {
    }

    /**
     * Escribe una fecha como dd/MM/yyyy.
     *
     * @param fecha la fecha, puede ser null
     * @return la fecha en texto, o vacio si es null
     */
    public static String fecha(LocalDate fecha) {
        return fecha == null ? "" : fecha.format(Oferta.FORMATO_FECHA);
    }

    /**
     * Escribe un salario en euros, por ejemplo "24.000 €".
     *
     * @param salario el salario, puede ser null
     * @return el salario en texto, o "—" si no se sabe
     */
    public static String salario(Double salario) {
        return salario == null ? "—" : EUROS.format(salario) + " €";
    }

    /**
     * Convierte lo que escribe el usuario en un salario. Acepta "22000", "22.000" o "22.000,50".
     *
     * @param texto lo escrito
     * @return el salario, o null si el campo esta vacio
     * @throws IllegalArgumentException si no es un numero o es negativo
     */
    public static Double leerSalario(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        String limpio = texto.replace("€", "").replace(" ", "").replace(".", "").replace(',', '.');
        try {
            double salario = Double.parseDouble(limpio);
            if (salario < 0) {
                throw new IllegalArgumentException("El salario no puede ser negativo.");
            }
            return salario;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El salario tiene que ser un número, por ejemplo 22.000.");
        }
    }

    /**
     * Prepara un DatePicker para que escriba y lea las fechas como dd/MM/yyyy.
     *
     * @param selector el DatePicker
     */
    public static void prepararFecha(DatePicker selector) {
        selector.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate fecha) {
                return fecha(fecha);
            }

            @Override
            public LocalDate fromString(String texto) {
                return texto == null || texto.isBlank() ? null : LocalDate.parse(texto.strip(), Oferta.FORMATO_FECHA);
            }
        });
    }

    /**
     * Lee la fecha de un DatePicker aunque el usuario la haya escrito a mano sin pulsar Enter.
     *
     * @param selector el DatePicker
     * @return la fecha, o null si el campo esta vacio
     * @throws IllegalArgumentException si lo escrito no es una fecha dd/MM/yyyy
     */
    public static LocalDate leerFecha(DatePicker selector) {
        String texto = selector.getEditor().getText();
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            LocalDate fecha = LocalDate.parse(texto.strip(), Oferta.FORMATO_FECHA);
            selector.setValue(fecha);
            return fecha;
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha «" + texto.strip() + "» no es válida. Usa el formato dd/MM/yyyy.");
        }
    }

    /**
     * Hace que un desplegable vuelva a mostrar su texto de ayuda cuando se deja sin valor
     * (por defecto JavaFX lo deja en blanco).
     *
     * @param desplegable el ComboBox
     * @param <T>         tipo de los elementos
     */
    public static <T> void textoSiVacio(ComboBox<T> desplegable) {
        desplegable.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T elemento, boolean vacio) {
                super.updateItem(elemento, vacio);
                setText(vacio || elemento == null ? desplegable.getPromptText() : elemento.toString());
            }
        });
    }
}
