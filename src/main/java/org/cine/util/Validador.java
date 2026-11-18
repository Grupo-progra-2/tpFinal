package org.cine.util;

import excepciones.DatoInvalidoException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * Metodos para validar los datos que escribe el usuario.
 * Si un dato no es valido lanzan una DatoInvalidoException con el motivo.
 */
public class Validador {

    // Lista de generos para que el usuario sepa cuales puede elegir
    public static final String[] GENEROS = {"Acción", "Animación", "Aventura", "Ciencia ficción",
            "Comedia", "Documental", "Drama", "Fantasía", "Musical", "Romance", "Suspenso", "Terror"};

    public static final String[] CLASIFICACIONES = {"ATP", "+13", "+16", "+18"};

    // Texto que no puede estar vacio ni tener solo espacios en blanco
    public static String textoObligatorio(String texto, String campo) throws DatoInvalidoException {
        if (texto == null || texto.trim().isEmpty()) {
            throw new DatoInvalidoException("El campo " + campo + " no puede estar vacío.");
        }
        if (texto.trim().length() > 100) {
            throw new DatoInvalidoException("El campo " + campo + " no puede tener más de 100 caracteres.");
        }
        return texto.trim();
    }

    // Nombre o apellido: solo letras (puede tener espacios, apostrofe o guion en el medio)
    public static String nombre(String texto, String campo) throws DatoInvalidoException {
        String valor = textoObligatorio(texto, campo);
        if (!valor.matches("\\p{L}+([ '-]\\p{L}+)*")) {
            throw new DatoInvalidoException("El campo " + campo + " solo puede tener letras.");
        }
        return valor;
    }

    // DNI: 7 u 8 numeros, no acepta letras ni puntos
    public static String dni(String texto) throws DatoInvalidoException {
        String valor = textoObligatorio(texto, "DNI");
        if (!valor.matches("\\d{7,8}")) {
            throw new DatoInvalidoException("DNI inválido. Ingresá 7 u 8 números, sin letras ni puntos.");
        }
        return valor;
    }

    // Email: tiene que tener arroba y un punto despues del arroba (ej: usuario@gmail.com)
    public static String email(String texto) throws DatoInvalidoException {
        String valor = textoObligatorio(texto, "email");
        if (valor.contains(" ") || !valor.contains("@")) {
            throw new DatoInvalidoException("Email inválido. Tiene que tener @ y no puede tener espacios.");
        }
        int arroba = valor.indexOf("@");
        String despues = valor.substring(arroba + 1);
        if (arroba == 0 || despues.contains("@") || !despues.contains(".")
                || despues.startsWith(".") || despues.endsWith(".")) {
            throw new DatoInvalidoException("Email inválido. Ejemplo: usuario@gmail.com");
        }
        return valor;
    }

    // Clave segura: minimo 8 caracteres, una mayuscula, una minuscula, un numero y sin espacios
    public static String clave(String texto) throws DatoInvalidoException {
        if (texto == null || texto.length() < 8) {
            throw new DatoInvalidoException("La clave debe tener al menos 8 caracteres.");
        }
        boolean hayMayuscula = false;
        boolean hayMinuscula = false;
        boolean hayNumero = false;
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            if (c == ' ') {
                throw new DatoInvalidoException("La clave no puede tener espacios.");
            }
            if (Character.isUpperCase(c)) {
                hayMayuscula = true;
            } else if (Character.isLowerCase(c)) {
                hayMinuscula = true;
            } else if (Character.isDigit(c)) {
                hayNumero = true;
            }
        }
        if (!hayMayuscula || !hayMinuscula || !hayNumero) {
            throw new DatoInvalidoException("La clave debe tener una mayúscula, una minúscula y un número.");
        }
        return texto;
    }

    // Numero entero entre un minimo y un maximo
    public static int entero(String texto, String campo, int minimo, int maximo) throws DatoInvalidoException {
        int numero;
        try {
            numero = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("El campo " + campo + " debe ser un número entero.");
        }
        if (numero < minimo || numero > maximo) {
            throw new DatoInvalidoException("El campo " + campo + " debe estar entre " + minimo + " y " + maximo + ".");
        }
        return numero;
    }

    // Precio mayor a 0
    public static BigDecimal precio(String texto) throws DatoInvalidoException {
        BigDecimal valor = decimal(texto, "precio");
        if (valor.signum() <= 0) {
            throw new DatoInvalidoException("El precio debe ser mayor a 0.");
        }
        return valor;
    }

    // Porcentaje entre 0 y 100
    public static BigDecimal porcentaje(String texto) throws DatoInvalidoException {
        BigDecimal valor = decimal(texto, "porcentaje");
        if (valor.signum() < 0 || valor.compareTo(new BigDecimal("100")) > 0) {
            throw new DatoInvalidoException("El porcentaje debe estar entre 0 y 100.");
        }
        return valor;
    }

    private static BigDecimal decimal(String texto, String campo) throws DatoInvalidoException {
        try {
            return new BigDecimal(texto.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new DatoInvalidoException("El campo " + campo + " debe ser un número.");
        }
    }

    // Fecha con formato AAAA-MM-DD HH:mm que no sea pasada
    public static LocalDateTime fechaFutura(String texto) throws DatoInvalidoException {
        LocalDateTime fecha;
        try {
            fecha = LocalDateTime.parse(texto.trim().replace(' ', 'T'));
        } catch (DateTimeParseException e) {
            throw new DatoInvalidoException("Usá el formato AAAA-MM-DD HH:mm (ejemplo: 2026-12-20 21:30).");
        }
        if (!fecha.isAfter(LocalDateTime.now())) {
            throw new DatoInvalidoException("La fecha no puede ser pasada.");
        }
        return fecha;
    }

    // Elige un genero de la lista, por numero o por nombre (sin importar mayusculas)
    public static String genero(String texto) throws DatoInvalidoException {
        return elegirDeLista(texto, GENEROS, "género");
    }

    public static String clasificacion(String texto) throws DatoInvalidoException {
        return elegirDeLista(texto, CLASIFICACIONES, "clasificación");
    }

    private static String elegirDeLista(String texto, String[] opciones, String campo) throws DatoInvalidoException {
        String valor = textoObligatorio(texto, campo);
        // Si escribio un numero, es la posicion en la lista
        if (valor.matches("\\d+")) {
            int posicion = Integer.parseInt(valor);
            if (posicion >= 1 && posicion <= opciones.length) {
                return opciones[posicion - 1];
            }
        }
        // Si escribio el nombre, se compara ignorando mayusculas y minusculas
        for (int i = 0; i < opciones.length; i++) {
            if (opciones[i].equalsIgnoreCase(valor)) {
                return opciones[i];
            }
        }
        throw new DatoInvalidoException("Elegí un " + campo + " de la lista (número o nombre).");
    }
}
