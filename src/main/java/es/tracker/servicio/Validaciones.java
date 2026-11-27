package es.tracker.servicio;

/**
 * Comprobaciones que usan varios servicios.
 */
final class Validaciones {

    private Validaciones() {
    }

    /**
     * Comprueba que una url, si se ha escrito, empiece por http:// o https://.
     *
     * @param url   la url, puede estar vacia
     * @param campo nombre del campo para el mensaje de error
     * @throws IllegalArgumentException si la url no tiene el formato correcto
     */
    static void comprobarUrl(String url, String campo) {
        if (url == null || url.isBlank()) {
            return;
        }
        String limpia = url.strip();
        boolean empiezaBien = limpia.startsWith("http://") || limpia.startsWith("https://");
        if (!empiezaBien || limpia.contains(" ") || limpia.length() <= "https://".length()) {
            throw new IllegalArgumentException(campo + " tiene que empezar por http:// o https:// y no puede tener espacios.");
        }
    }
}
