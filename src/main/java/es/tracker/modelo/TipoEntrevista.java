package es.tracker.modelo;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Tipos de entrevista que se pueden registrar.
 */
public enum TipoEntrevista {
    TELEFONICA("telefónica"),
    TECNICA("técnica"),
    PRESENCIAL("presencial"),
    ONLINE("online");

    private final String texto;

    TipoEntrevista(String texto) {
        this.texto = texto;
    }

    /**
     * Devuelve el tipo tal y como se guarda en la base de datos.
     *
     * @return el texto del tipo, por ejemplo "técnica"
     */
    public String texto() {
        return texto;
    }

    /**
     * Busca el tipo a partir de un texto, sin mirar mayusculas.
     *
     * @param texto el texto del tipo
     * @return el tipo que corresponde
     * @throws IllegalArgumentException si el texto no es ningun tipo
     */
    public static TipoEntrevista desdeTexto(String texto) {
        return Arrays.stream(values())
                .filter(tipo -> texto != null && tipo.texto.equalsIgnoreCase(texto.strip()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Tipo de entrevista no válido: \"" + texto
                        + "\". Tipos permitidos: " + Arrays.stream(values()).map(TipoEntrevista::texto)
                        .collect(Collectors.joining(", "))));
    }

    /**
     * Texto que se ve en la interfaz, con la primera letra en mayuscula.
     *
     * @return por ejemplo "Técnica"
     */
    @Override
    public String toString() {
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
