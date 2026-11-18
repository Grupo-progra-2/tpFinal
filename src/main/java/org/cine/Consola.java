package org.cine;

import excepciones.DatoInvalidoException;
import org.cine.util.Validador;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Scanner;

/**
 * Se ocupa de leer lo que escribe el usuario y de mostrar los titulos.
 * Cada metodo de lectura vuelve a preguntar hasta que el dato sea valido.
 */
final class Consola {
    private final Scanner entrada;

    Consola(Scanner entrada) {
        this.entrada = entrada;
    }

    // ---------- pantalla ----------

    // Deja la consola "limpia" imprimiendo lineas en blanco
    void limpiar() {
        for (int i = 0; i < 40; i++) {
            System.out.println();
        }
    }

    // Muestra un titulo dentro de un cuadro
    void encabezado(String texto) {
        String borde = "+";
        for (int i = 0; i < texto.length() + 4; i++) {
            borde = borde + "=";
        }
        borde = borde + "+";
        System.out.println(borde);
        System.out.println("|  " + texto + "  |");
        System.out.println(borde);
        System.out.println();
    }

    // Espera un Enter para que el usuario alcance a leer el resultado antes de limpiar
    void pausar() {
        System.out.println();
        texto("Presioná Enter para continuar...");
    }

    // ---------- lectura ----------

    String texto(String mensaje) {
        System.out.print(mensaje);
        return entrada.nextLine().trim();
    }

    int entero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(texto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Error: ingresá un número entero.");
            }
        }
    }

    String textoObligatorio(String mensaje, String campo) {
        while (true) {
            try {
                return Validador.textoObligatorio(texto(mensaje), campo);
            } catch (DatoInvalidoException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    String nombre(String mensaje, String campo) {
        while (true) {
            try {
                return Validador.nombre(texto(mensaje), campo);
            } catch (DatoInvalidoException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    String dni(String mensaje) {
        while (true) {
            try {
                return Validador.dni(texto(mensaje));
            } catch (DatoInvalidoException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    String email(String mensaje) {
        while (true) {
            try {
                return Validador.email(texto(mensaje));
            } catch (DatoInvalidoException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    String clave(String mensaje) {
        System.out.println("(mínimo 8 caracteres, con mayúscula, minúscula y número, sin espacios)");
        while (true) {
            try {
                // La clave no se recorta con trim: los espacios tambien se validan
                System.out.print(mensaje);
                return Validador.clave(entrada.nextLine());
            } catch (DatoInvalidoException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    int numero(String mensaje, String campo, int minimo, int maximo) {
        while (true) {
            try {
                return Validador.entero(texto(mensaje), campo, minimo, maximo);
            } catch (DatoInvalidoException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    BigDecimal precio(String mensaje) {
        while (true) {
            try {
                return Validador.precio(texto(mensaje));
            } catch (DatoInvalidoException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    BigDecimal porcentaje(String mensaje) {
        while (true) {
            try {
                return Validador.porcentaje(texto(mensaje));
            } catch (DatoInvalidoException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    LocalDateTime fechaFutura(String mensaje) {
        while (true) {
            try {
                return Validador.fechaFutura(texto(mensaje));
            } catch (DatoInvalidoException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    // Muestra los generos disponibles y pide uno
    String genero() {
        System.out.println("Géneros disponibles:");
        for (int i = 0; i < Validador.GENEROS.length; i++) {
            System.out.println("  " + (i + 1) + ". " + Validador.GENEROS[i]);
        }
        while (true) {
            try {
                return Validador.genero(texto("Género (número o nombre): "));
            } catch (DatoInvalidoException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    String clasificacion() {
        System.out.println("Clasificaciones disponibles:");
        for (int i = 0; i < Validador.CLASIFICACIONES.length; i++) {
            System.out.println("  " + (i + 1) + ". " + Validador.CLASIFICACIONES[i]);
        }
        while (true) {
            try {
                return Validador.clasificacion(texto("Clasificación (número o nombre): "));
            } catch (DatoInvalidoException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }
}
