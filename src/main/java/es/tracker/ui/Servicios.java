package es.tracker.ui;

import es.tracker.servicio.EmpresaService;
import es.tracker.servicio.EntrevistaService;
import es.tracker.servicio.OfertaService;

/**
 * Los servicios que usan los controladores. Se crean una vez al arrancar y se
 * pasan a cada controlador por su constructor.
 *
 * @param ofertas     servicio de candidaturas
 * @param empresas    servicio de empresas
 * @param entrevistas servicio de entrevistas
 */
public record Servicios(OfertaService ofertas, EmpresaService empresas, EntrevistaService entrevistas) {
}
