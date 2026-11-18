package org.cine;

import excepciones.AsientoOcupadoException;
import excepciones.DatoInvalidoException;
import excepciones.ElementoNoEncontradoException;
import excepciones.HorarioSuperpuestoException;
import excepciones.PersistenciaException;
import org.cine.enums.TipoEntrada;
import org.cine.modelo.Cliente;
import org.cine.modelo.Funcion;
import org.cine.modelo.Pelicula;
import org.cine.modelo.Reserva;
import org.cine.modelo.Sala;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

final class MenuReservas {
    private final Consola consola;
    private final GestorCine cine;

    MenuReservas(Consola consola, GestorCine cine) {
        this.consola = consola;
        this.cine = cine;
    }

    void ejecutar() {
        while (true) {
            consola.limpiar();
            consola.encabezado("FUNCIONES Y RESERVAS");
            System.out.println("""
                    1. Programar función
                    2. Ver funciones disponibles
                    3. Ver asientos libres
                    4. Reservar asiento
                    5. Ver reservas por cliente
                    6. Cancelar reserva
                    7. Informe de ocupación y recaudación
                    0. Salir del menú
                    """);
            try {
                switch (consola.entero("Opción: ")) {
                    case 0 -> { return; }
                    case 1 -> programar();
                    case 2 -> listarFunciones();
                    case 3 -> System.out.println("Asientos libres: " +
                            cine.listarAsientosDisponibles(consola.entero("ID de función: ")));
                    case 4 -> reservar();
                    case 5 -> listarReservas();
                    case 6 -> cancelar();
                    case 7 -> System.out.println(cine.informeFuncion(consola.entero("ID de función: ")));
                    default -> System.out.println("Opción inválida.");
                }
            } catch (DatoInvalidoException | ElementoNoEncontradoException | HorarioSuperpuestoException
                     | AsientoOcupadoException | PersistenciaException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
            consola.pausar();
        }
    }

    private void listarFunciones() throws PersistenciaException {
        for (Funcion funcion : cine.listarFuncionesDisponibles()) {
            System.out.println(funcion + " | Precio: $" + funcion.getPrecioBase());
        }
    }

    private void listarReservas() throws PersistenciaException {
        int cliente = consola.entero("ID de cliente: ");
        for (Reserva reserva : cine.listarReservasPorCliente(cliente)) {
            System.out.println(reserva);
        }
    }

    private void programar() throws PersistenciaException, ElementoNoEncontradoException,
            DatoInvalidoException, HorarioSuperpuestoException {
        for (Pelicula pelicula : cine.listarPeliculas()) {
            System.out.println(pelicula.getIdPelicula() + ". " + pelicula);
        }
        int pelicula = consola.entero("ID de película: ");
        for (Sala sala : cine.listarSalas()) {
            System.out.println(sala.getIdSala() + ". " + sala);
        }
        int sala = consola.entero("ID de sala: ");
        // Se vuelve a pedir el dato hasta que sea válido (fecha con formato correcto y futura, precio mayor a 0)
        LocalDateTime fecha = consola.fechaFutura("Fecha (AAAA-MM-DD HH:mm): ");
        BigDecimal precio = consola.precio("Precio base: ");
        System.out.println("Función programada: " + cine.programarFuncion(pelicula, sala, fecha, precio));
    }

    private void reservar() throws PersistenciaException, ElementoNoEncontradoException,
            DatoInvalidoException, AsientoOcupadoException {
        for (Cliente cliente : cine.listarClientes()) {
            System.out.println(cliente.getId() + ". " + cliente.getNombre() + " (" + cliente.getDni() + ")");
        }
        int cliente = consola.entero("ID de cliente: ");
        int funcion = consola.entero("ID de función: ");
        List<Integer> libres = cine.listarAsientosDisponibles(funcion);
        if (libres.isEmpty()) {
            System.out.println("No quedan asientos libres.");
            return;
        }
        System.out.println("Asientos libres: " + libres);
        int asiento = consola.entero("Asiento: ");

        while (!libres.contains(asiento)) {
            System.out.println("Ese asiento no está disponible.");
            System.out.println("Asientos libres: " + libres);
            asiento = consola.entero("Elegí otro asiento: ");
        }
        int opcion = consola.entero("1. General | 2. Reducida: ");
        TipoEntrada tipo;
        BigDecimal descuento = BigDecimal.ZERO;
        if (opcion == 1) {
            tipo = TipoEntrada.GENERAL;
        } else if (opcion == 2) {
            tipo = TipoEntrada.REDUCIDA;
            descuento = consola.porcentaje("Porcentaje de descuento (20 = 20%): ");
        } else {
            throw new DatoInvalidoException("Elegí un tipo de entrada válido.");
        }
        Reserva reserva = cine.crearReserva(cliente, funcion, asiento, tipo, descuento);
        System.out.println("Reserva confirmada: " + reserva);
    }

    private void cancelar() throws PersistenciaException, ElementoNoEncontradoException {
        cine.cancelarReserva(consola.entero("ID de reserva: "));
        System.out.println("Reserva cancelada.");
    }
}
