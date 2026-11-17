package org.cine.modelo;

import org.cine.enums.TipoEntrada;

import java.math.BigDecimal;

public class EntradaReducida extends Entrada implements Descontable {
    // Porcentaje de descuento, por ejemplo 20 = 20%
    private BigDecimal porcentajeDescuento;

    public EntradaReducida(BigDecimal precioBase, BigDecimal porcentajeDescuento) {
        super(precioBase);
        this.porcentajeDescuento = porcentajeDescuento;
    }

    @Override
    public BigDecimal calcularDescuento() {
        return precioBase.multiply(porcentajeDescuento).divide(new BigDecimal("100"));
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
