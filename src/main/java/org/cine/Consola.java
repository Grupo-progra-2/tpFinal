package org.cine;

import java.math.BigDecimal;
import java.util.Scanner;

final class Consola {
    private final Scanner entrada;

    Consola(Scanner entrada) {
        this.entrada = entrada;
    }

    String texto(String mensaje) {
        System.out.print(mensaje);
        return entrada.nextLine().trim();
    }

    int entero(String mensaje) {
        try {
            return Integer.parseInt(texto(mensaje));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresá un número entero.", e);
        }
    }

    BigDecimal decimal(String mensaje) {
        try {
            return new BigDecimal(texto(mensaje).replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ingresá un importe o porcentaje válido.", e);
        }
    }
}
