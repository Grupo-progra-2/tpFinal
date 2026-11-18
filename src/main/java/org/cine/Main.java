package org.cine;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner entrada = new Scanner(System.in)) {
            Consola consola = new Consola(entrada);
            GestorCine cine = new GestorCine();
            while (true) {
                consola.limpiar();
                consola.encabezado("CINEGESTOR");
                System.out.println("""
                        1. Administración
                        2. Funciones y reservas
                        0. Salir
                        """);
                switch (consola.entero("Opción: ")) {
                    case 0 -> { return; }
                    case 1 -> new MenuAdministracion(consola).ejecutar();
                    case 2 -> new MenuReservas(consola, cine).ejecutar();
                    default -> {
                        System.out.println("Opción inválida.");
                        consola.pausar();
                    }
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println("Fin de la entrada.");
        }
    }
}
