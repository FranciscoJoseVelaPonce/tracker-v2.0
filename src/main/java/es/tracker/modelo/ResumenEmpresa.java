package es.tracker.modelo;

/**
 * Una empresa junto con el numero de candidaturas que se le han enviado.
 * Se usa en la pantalla de empresas.
 *
 * @param empresa la empresa
 * @param ofertas cuantas candidaturas tiene
 */
public record ResumenEmpresa(Empresa empresa, int ofertas) {
}
