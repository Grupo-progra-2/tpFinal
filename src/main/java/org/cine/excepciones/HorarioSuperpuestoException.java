package org.cine.excepciones;

/**
 * Se usa cuando una funcion se pisa con otra en la misma sala.
 */
public class HorarioSuperpuestoException extends Exception {

    public HorarioSuperpuestoException(String mensaje) {
        super(mensaje);
    }
}
