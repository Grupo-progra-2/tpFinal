package org.cine;

import dao.ClienteDAO;
import dao.FuncionDAO;
import dao.PeliculaDAO;
import dao.ReservaDAO;
import dao.SalaDAO;
import excepciones.AsientoOcupadoException;
import excepciones.DatoInvalidoException;
import excepciones.ElementoNoEncontradoException;
import excepciones.HorarioSuperpuestoException;
import excepciones.PersistenciaException;
import org.cine.enums.EstadoFuncion;
import org.cine.enums.EstadoReserva;
import org.cine.enums.TipoEntrada;
import org.cine.modelo.Cliente;
import org.cine.modelo.Entrada;
import org.cine.modelo.EntradaGeneral;
import org.cine.modelo.EntradaReducida;
import org.cine.modelo.Funcion;
import org.cine.modelo.Pelicula;
import org.cine.modelo.Reserva;
import org.cine.modelo.Sala;
import org.cine.util.Repositorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** Reglas que consumen los menús de programación de funciones y reservas. */
public class GestorCine {
    private final ClienteDAO clientes = new ClienteDAO();
    private final PeliculaDAO peliculas = new PeliculaDAO();
    private final SalaDAO salas = new SalaDAO();
    private final FuncionDAO funciones = new FuncionDAO();
    private final ReservaDAO reservas = new ReservaDAO();

    public List<Pelicula> listarPeliculas() throws PersistenciaException {
        return peliculas.listar();
    }

    public List<Sala> listarSalas() throws PersistenciaException {
        return salas.listar();
    }

    public List<Cliente> listarClientes() throws PersistenciaException {
        return clientes.listar();
    }

    public Funcion programarFuncion(int idPelicula, int idSala, LocalDateTime fechaHora, BigDecimal precio)
            throws PersistenciaException, ElementoNoEncontradoException, DatoInvalidoException,
            HorarioSuperpuestoException {
        Pelicula pelicula = peliculas.buscarPorId(idPelicula);
        if (pelicula == null) {
            throw new ElementoNoEncontradoException("No existe la película " + idPelicula + ".");
        }
        Sala sala = salas.buscarPorId(idSala);
        if (sala == null) {
            throw new ElementoNoEncontradoException("No existe la sala " + idSala + ".");
        }
        if (fechaHora == null) {
            throw new DatoInvalidoException("Ingresá el día y la hora de la función.");
        }
        // No se puede programar una función en una fecha que ya pasó
        if (!fechaHora.isAfter(LocalDateTime.now())) {
            throw new DatoInvalidoException("No se puede programar una función en una fecha pasada.");
        }
        if (precio == null || precio.signum() <= 0) {
            throw new DatoInvalidoException("El precio debe ser mayor a 0.");
        }
        Funcion funcion = new Funcion(0, pelicula, sala, fechaHora.withNano(0), precio, EstadoFuncion.PROGRAMADA);
        funciones.guardar(funcion);
        return funcion;
    }

    public List<Funcion> listarFuncionesDisponibles() throws PersistenciaException {
        return funciones.listarDisponibles();
    }

    public List<Integer> listarAsientosDisponibles(int idFuncion)
            throws PersistenciaException, ElementoNoEncontradoException, DatoInvalidoException {
        Funcion funcion = buscarFuncionDisponible(idFuncion);
        List<Integer> ocupados = reservas.asientosOcupados(idFuncion);
        Repositorio<Integer> disponibles = new Repositorio<>();
        for (int asiento = 1; asiento <= funcion.getSala().getCapacidad(); asiento++) {
            if (!ocupados.contains(asiento)) {
                disponibles.agregar(asiento);
            }
        }
        return disponibles.listar();
    }

    public Reserva crearReserva(int idCliente, int idFuncion, int asiento, TipoEntrada tipo, BigDecimal descuento)
            throws PersistenciaException, ElementoNoEncontradoException, DatoInvalidoException, AsientoOcupadoException {
        Funcion funcion = buscarFuncionDisponible(idFuncion);
        if (asiento < 1 || asiento > funcion.getSala().getCapacidad()) {
            throw new DatoInvalidoException("El número de asiento está fuera de la capacidad de la sala.");
        }
        Cliente cliente = clientes.buscarPorId(idCliente);
        if (cliente == null) {
            throw new ElementoNoEncontradoException("No existe el cliente " + idCliente + ".");
        }
        cliente.setClaveAcceso(null);

        Entrada entrada = crearEntrada(funcion.getPrecioBase(), tipo, descuento);
        Reserva reserva = new Reserva(0, cliente, funcion, asiento, entrada, entrada.calcularPrecio(),
                LocalDateTime.now().withNano(0), EstadoReserva.CONFIRMADA);
        reservas.guardarReserva(reserva);
        return reserva;
    }

    private Funcion buscarFuncionDisponible(int id)
            throws PersistenciaException, ElementoNoEncontradoException, DatoInvalidoException {
        Funcion funcion = funciones.buscarPorId(id);
        if (funcion == null) {
            throw new ElementoNoEncontradoException("No existe la función " + id + ".");
        }
        if (!funcion.estaDisponible()) {
            throw new DatoInvalidoException("La función ya no está disponible.");
        }
        return funcion;
    }

    private Entrada crearEntrada(BigDecimal precio, TipoEntrada tipo, BigDecimal descuento)
            throws DatoInvalidoException {
        if (tipo == null) {
            throw new DatoInvalidoException("Elegí un tipo de entrada.");
        }
        try {
            if (tipo == TipoEntrada.REDUCIDA) {
                return new EntradaReducida(precio, descuento);
            }
            return new EntradaGeneral(precio);
        } catch (IllegalArgumentException e) {
            throw new DatoInvalidoException(e.getMessage());
        }
    }

    public void cancelarReserva(int idReserva) throws PersistenciaException, ElementoNoEncontradoException {
        reservas.cancelarReserva(idReserva);
    }

    public List<Reserva> listarReservasPorCliente(int idCliente) throws PersistenciaException {
        return reservas.listarPorCliente(idCliente);
    }

    public String informeFuncion(int idFuncion) throws PersistenciaException, ElementoNoEncontradoException {
        Funcion funcion = funciones.buscarPorId(idFuncion);
        if (funcion == null) {
            throw new ElementoNoEncontradoException("No existe la función " + idFuncion + ".");
        }
        int confirmadas = 0;
        BigDecimal recaudacion = new BigDecimal("0.00");
        for (Reserva reserva : reservas.listarPorFuncion(idFuncion)) {
            if (reserva.estaConfirmada()) {
                confirmadas++;
                recaudacion = recaudacion.add(reserva.getPrecioFinal());
            }
        }
        return "Entradas confirmadas: " + confirmadas + "/" + funcion.getSala().getCapacidad()
                + " | Recaudación: $" + recaudacion;
    }
}
