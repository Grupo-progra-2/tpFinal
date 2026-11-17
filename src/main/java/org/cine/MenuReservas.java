package org.cine;

import dao.ClienteDAO;
import dao.PeliculaDAO;
import dao.SalaDAO;
import excepciones.AsientoOcupadoException;
import excepciones.DatoInvalidoException;
import excepciones.ElementoNoEncontradoException;
import excepciones.HorarioSuperpuestoException;
import excepciones.PersistenciaException;
import org.cine.enums.TipoEntrada;
import org.cine.modelo.Reserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

final class MenuReservas {
    private final Consola consola;
    private final GestorCine cine;

    MenuReservas(Consola consola, GestorCine cine) {
        this.consola = consola;
        this.cine = cine;
    }

    void ejecutar() {
        while (true) {
            System.out.println("""
                    Funciones y reservas
                    1. Programar función
                    2. Ver funciones disponibles
                    3. Ver asientos libres
                    4. Reservar asiento
                    5. Ver reservas por cliente
                    6. Cancelar reserva
                    0. Salir del menú
                    """);
            try {
                switch (consola.entero("Opción: ")) {
                    case 0 -> { return; }
                    case 1 -> programar();
                    case 2 -> cine.listarFuncionesDisponibles()
                            .forEach(f -> System.out.println(f + " | Precio: $" + f.getPrecioBase()));
                    case 3 -> System.out.println("Asientos libres: " +
                            cine.listarAsientosDisponibles(consola.entero("ID de función: ")));
                    case 4 -> reservar();
                    case 5 -> cine.listarReservasPorCliente(consola.entero("ID de cliente: "))
                            .forEach(System.out::println);
                    case 6 -> cancelar();
                    default -> System.out.println("Opción inválida.");
                }
            } catch (DatoInvalidoException | ElementoNoEncontradoException | HorarioSuperpuestoException
                     | AsientoOcupadoException | PersistenciaException | IllegalArgumentException e) {
                System.out.println(e.getMessage());
            } catch (DateTimeParseException e) {
                System.out.println("Usá el formato AAAA-MM-DD HH:mm para la fecha y la hora.");
            }
        }
    }

    private void programar() throws PersistenciaException, ElementoNoEncontradoException,
            DatoInvalidoException, HorarioSuperpuestoException {
        new PeliculaDAO().listar().forEach(p -> System.out.println(p.getIdPelicula() + ". " + p));
        int pelicula = consola.entero("ID de película: ");
        new SalaDAO().listar().forEach(s -> System.out.println(s.getIdSala() + ". " + s));
        int sala = consola.entero("ID de sala: ");
        LocalDateTime fecha = LocalDateTime.parse(consola.texto("Fecha (AAAA-MM-DD HH:mm): ").replace(' ', 'T'));
        BigDecimal precio = consola.decimal("Precio base: ");
        System.out.println("Función programada: " + cine.programarFuncion(pelicula, sala, fecha, precio));
    }

    private void reservar() throws PersistenciaException, ElementoNoEncontradoException,
            DatoInvalidoException, AsientoOcupadoException {
        new ClienteDAO().listar().forEach(c -> System.out.println(c.getId() + ". " + c.getNombre() + " (" + c.getDni() + ")"));
        int cliente = consola.entero("ID de cliente: ");
        int funcion = consola.entero("ID de función: ");
        var libres = cine.listarAsientosDisponibles(funcion);
        if (libres.isEmpty()) {
            System.out.println("No quedan asientos libres.");
            return;
        }
        System.out.println("Asientos libres: " + libres);
        int asiento = consola.entero("Asiento: ");
        TipoEntrada tipo = switch (consola.entero("1. General | 2. Reducida: ")) {
            case 1 -> TipoEntrada.GENERAL;
            case 2 -> TipoEntrada.REDUCIDA;
            default -> throw new DatoInvalidoException("Elegí un tipo de entrada válido.");
        };
        BigDecimal descuento = tipo == TipoEntrada.REDUCIDA
                ? consola.decimal("Porcentaje de descuento (20 = 20%): ") : BigDecimal.ZERO;
        Reserva reserva = cine.crearReserva(cliente, funcion, asiento, tipo, descuento);
        System.out.println("Reserva confirmada: " + reserva);
    }

    private void cancelar() throws PersistenciaException, ElementoNoEncontradoException {
        cine.cancelarReserva(consola.entero("ID de reserva: "));
        System.out.println("Reserva cancelada.");
    }
}
