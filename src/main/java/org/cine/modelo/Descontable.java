package org.cine.modelo;

import java.math.BigDecimal;

/**
 * Interfaz para las entradas que tienen algun descuento.
 */
public interface Descontable {

    BigDecimal calcularDescuento();
}
