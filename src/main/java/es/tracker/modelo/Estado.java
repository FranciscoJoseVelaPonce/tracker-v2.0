package es.tracker.modelo;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Estados en los que puede estar una candidatura.
 */
public enum Estado {
    PENDIENTE("pendiente"),
    ENTREVISTA("entrevista"),
    RECHAZADO("rechazado"),
    ACEPTADO("aceptado");

    private final String texto;

    Estado(String texto) {
        this.texto = texto;
    }

    /**
     * Devuelve el estado tal y como se guarda en la base de datos y en el CSV.
     *
     * @return el texto del estado, por ejemplo "pendiente"
     */
    public String texto() {
        return texto;
    }

    /**
     * Busca el estado a partir de un texto. Da igual si viene en mayusculas o minusculas.
     *
     * @param texto el texto del estado
     * @return el estado que corresponde
     * @throws IllegalArgumentException si el texto no es ningun estado
     */
    public static Estado desdeTexto(String texto) {
        return Arrays.stream(values())
                .filter(estado -> texto != null && estado.texto.equalsIgnoreCase(texto.strip()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Estado no válido: \"" + texto + "\". Estados permitidos: " + textosPermitidos()));
    }

    /**
     * Lista de estados separada por comas, para los mensajes de error.
     *
     * @return por ejemplo "pendiente, entrevista, rechazado, aceptado"
     */
    public static String textosPermitidos() {
        return Arrays.stream(values()).map(Estado::texto).collect(Collectors.joining(", "));
    }

    /**
     * Texto que se ve en la interfaz, con la primera letra en mayuscula.
     *
     * @return por ejemplo "Pendiente"
     */
    @Override
    public String toString() {
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
