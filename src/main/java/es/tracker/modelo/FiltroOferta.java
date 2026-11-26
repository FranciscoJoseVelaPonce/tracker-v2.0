package es.tracker.modelo;

import java.time.LocalDate;

/**
 * Criterios para buscar candidaturas. Todos son opcionales: si un campo es null
 * no se usa, y si hay varios se tienen que cumplir todos a la vez.
 *
 * @param texto      texto que tiene que aparecer en el titulo o en la empresa
 * @param estado     estado que tiene que tener
 * @param idEmpresa  id de la empresa
 * @param salarioMin salario minimo
 * @param salarioMax salario maximo
 * @param desde      primera fecha de postulacion
 * @param hasta      ultima fecha de postulacion
 */
public record FiltroOferta(String texto, Estado estado, Integer idEmpresa,
                           Double salarioMin, Double salarioMax,
                           LocalDate desde, LocalDate hasta) {

    /** Filtro sin ningun criterio, devuelve todas las candidaturas. */
    public static final FiltroOferta TODAS = new FiltroOferta(null, null, null, null, null, null, null);

    /**
     * Comprueba que los rangos tengan sentido.
     *
     * @throws IllegalArgumentException si el minimo es mayor que el maximo o la fecha inicial es posterior a la final
     */
    public FiltroOferta {
        if (salarioMin != null && salarioMax != null && salarioMin > salarioMax) {
            throw new IllegalArgumentException("El salario mínimo no puede ser mayor que el máximo.");
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha «desde» no puede ser posterior a la fecha «hasta».");
        }
        texto = texto == null || texto.isBlank() ? null : texto.strip();
    }
}
