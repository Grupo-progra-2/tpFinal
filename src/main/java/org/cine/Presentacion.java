package org.cine;

import javafx.application.Application;

/** Punto de entrada para abrir la ventana desde IntelliJ o desde un lanzador de escritorio. */
public class Presentacion {
    public static void main(String[] args) {
        Application.launch(CineGestorFX.class, args);
    }
}
