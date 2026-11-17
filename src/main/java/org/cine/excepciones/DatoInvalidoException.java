package org.cine.excepciones;

/**
 * Se usa cuando un dato ingresado no es valido (vacio, negativo, formato incorrecto).
 */
public class DatoInvalidoException extends Exception {

    public DatoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
