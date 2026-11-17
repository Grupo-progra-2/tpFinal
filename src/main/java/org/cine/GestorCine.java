package org.cine;

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
import org.cine.enums.TipoEntrada;
import org.cine.modelo.Funcion;
import org.cine.modelo.Pelicula;
import org.cine.modelo.Reserva;
import org.cine.modelo.Sala;
import org.cine.util.Repositorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/** Reglas que consumen los menús de programación de funciones y reservas. */
public class GestorCine {
    private final PeliculaDAO peliculas = new PeliculaDAO();
    private final SalaDAO salas = new SalaDAO();
    private final FuncionDAO funciones = new FuncionDAO();
    private final ReservaDAO reservas = new ReservaDAO();

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
        Funcion funcion = new Funcion(0, pelicula, sala, fechaHora.withNano(0), precio, EstadoFuncion.PROGRAMADA);
        funciones.guardar(funcion);
        return funcion;
    }

    public List<Funcion> listarFuncionesDisponibles() throws PersistenciaException {
        return funciones.listarDisponibles();
    }

    public List<Integer> listarAsientosDisponibles(int idFuncion)
            throws PersistenciaException, ElementoNoEncontradoException, DatoInvalidoException {
        Funcion funcion = funciones.buscarPorId(idFuncion);
        if (funcion == null) {
            throw new ElementoNoEncontradoException("No existe la función " + idFuncion + ".");
        }
        if (!funcion.estaDisponible()) {
            throw new DatoInvalidoException("La función ya no está disponible.");
        }
        Set<Integer> ocupados = reservas.asientosOcupados(idFuncion);
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
        return reservas.crearReserva(idCliente, idFuncion, asiento, tipo, descuento);
    }

    public void cancelarReserva(int idReserva) throws PersistenciaException, ElementoNoEncontradoException {
        reservas.cancelarReserva(idReserva);
    }

    public List<Reserva> listarReservasPorCliente(int idCliente) throws PersistenciaException {
        return reservas.listarPorCliente(idCliente);
    }
}
