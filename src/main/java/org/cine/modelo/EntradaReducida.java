package org.cine.modelo;

import org.cine.enums.TipoEntrada;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EntradaReducida extends Entrada implements Descontable {
    // Porcentaje de descuento, por ejemplo 20 = 20%
    private static final BigDecimal CIEN = BigDecimal.valueOf(100);
    private final BigDecimal porcentajeDescuento;

    public EntradaReducida(BigDecimal precioBase, BigDecimal porcentajeDescuento) {
        super(precioBase);
        if (porcentajeDescuento == null || porcentajeDescuento.signum() < 0
                || porcentajeDescuento.compareTo(CIEN) > 0) {
            throw new IllegalArgumentException("El porcentaje de descuento debe estar entre 0 y 100.");
        }
        this.porcentajeDescuento = porcentajeDescuento;
    }

    @Override
    public BigDecimal calcularDescuento() {
        return precioBase.multiply(porcentajeDescuento).movePointLeft(2).setScale(2, RoundingMode.HALF_UP);
    }

    // Precio base menos el descuento
    @Override
    public BigDecimal calcularPrecio() {
        return precioBase.subtract(calcularDescuento());
    }

    @Override
    public TipoEntrada getTipo() {
        return TipoEntrada.REDUCIDA;
    }
}
