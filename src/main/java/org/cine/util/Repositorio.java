package org.cine.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase generica que guarda elementos de cualquier tipo en una lista en memoria.
 */
public class Repositorio<T> {
    private List<T> elementos = new ArrayList<>();

    public void agregar(T elemento) {
        elementos.add(elemento);
    }

    public boolean eliminar(T elemento) {
        return elementos.remove(elemento);
    }

    public List<T> listar() {
        return new ArrayList<>(elementos);
    }

    public void limpiar() {
        elementos.clear();
    }
}
