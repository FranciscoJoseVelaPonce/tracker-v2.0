package es.tracker.modelo;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Resultado de una entrevista.
 */
public enum ResultadoEntrevista {
    PENDIENTE("pendiente"),
    POSITIVO("positivo"),
    NEGATIVO("negativo");

    private final String texto;

    ResultadoEntrevista(String texto) {
        this.texto = texto;
    }

    /**
     * Devuelve el resultado tal y como se guarda en la base de datos.
     *
     * @return el texto del resultado, por ejemplo "positivo"
     */
    public String texto() {
        return texto;
    }

    /**
     * Busca el resultado a partir de un texto, sin mirar mayusculas.
     *
     * @param texto el texto del resultado
     * @return el resultado que corresponde
     * @throws IllegalArgumentException si el texto no es ningun resultado
     */
    public static ResultadoEntrevista desdeTexto(String texto) {
        return Arrays.stream(values())
                .filter(resultado -> texto != null && resultado.texto.equalsIgnoreCase(texto.strip()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Resultado no válido: \"" + texto
                        + "\". Resultados permitidos: " + Arrays.stream(values()).map(ResultadoEntrevista::texto)
                        .collect(Collectors.joining(", "))));
    }

    /**
     * Texto que se ve en la interfaz, con la primera letra en mayuscula.
     *
     * @return por ejemplo "Positivo"
     */
    @Override
    public String toString() {
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
