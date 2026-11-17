package org.cine.modelo;

import org.cine.enums.TipoEntrada;

import java.math.BigDecimal;

/**
 * Clase abstracta de la que heredan los tipos de entrada.
 * Cada tipo calcula su precio de forma distinta (polimorfismo).
 */
public abstract class Entrada {
    protected BigDecimal precioBase;

    public Entrada(BigDecimal precioBase) {
        this.precioBase = precioBase;
    }

    public abstract BigDecimal calcularPrecio();

    public abstract TipoEntrada getTipo();
}
