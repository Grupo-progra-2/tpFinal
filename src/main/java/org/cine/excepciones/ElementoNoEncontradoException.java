package org.cine.excepciones;

/**
 * Se usa cuando no existe en la base de datos el elemento buscado por id o dni.
 */
public class ElementoNoEncontradoException extends Exception {

    public ElementoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
