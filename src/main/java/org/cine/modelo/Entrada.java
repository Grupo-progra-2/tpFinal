package org.cine.modelo;

import org.cine.enums.TipoEntrada;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Clase abstracta de la que heredan los tipos de entrada.
 * Cada tipo calcula su precio de forma distinta (polimorfismo).
 */
public abstract class Entrada {
    protected final BigDecimal precioBase;

    public Entrada(BigDecimal precioBase) {
        if (precioBase == null || precioBase.signum() < 0) {
            throw new IllegalArgumentException("El precio base debe ser mayor o igual a cero.");
        }
        this.precioBase = precioBase.setScale(2, RoundingMode.HALF_UP);
    }

    public abstract BigDecimal calcularPrecio();

    public abstract TipoEntrada getTipo();
}
