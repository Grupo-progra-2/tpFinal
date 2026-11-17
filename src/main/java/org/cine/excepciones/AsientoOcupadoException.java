package org.cine.excepciones;

/**
 * Se usa cuando se intenta reservar un asiento que ya esta ocupado en la funcion.
 */
public class AsientoOcupadoException extends Exception {

    public AsientoOcupadoException(String mensaje) {
        super(mensaje);
    }
}
