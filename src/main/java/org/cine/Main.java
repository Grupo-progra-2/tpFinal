package org.cine;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner entrada = new Scanner(System.in)) {
            new MenuReservas(new Consola(entrada), new GestorCine()).ejecutar();
        } catch (NoSuchElementException e) {
            System.out.println("Fin de la entrada.");
        }
    }
}
