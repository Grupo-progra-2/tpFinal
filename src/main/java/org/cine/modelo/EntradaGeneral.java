package org.cine.modelo;

import org.cine.enums.TipoEntrada;

import java.math.BigDecimal;

public class EntradaGeneral extends Entrada {

    public EntradaGeneral(BigDecimal precioBase) {
        super(precioBase);
    }

    // La entrada general paga el precio completo
    @Override
    public BigDecimal calcularPrecio() {
        return precioBase;
    }

    @Override
    public TipoEntrada getTipo() {
        return TipoEntrada.GENERAL;
    }
}
