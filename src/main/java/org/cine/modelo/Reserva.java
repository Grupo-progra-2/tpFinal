package org.cine.modelo;

import org.cine.enums.EstadoReserva;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Reserva {
    private int idReserva;
    private Cliente cliente;
    private Funcion funcion;
    private int numeroAsiento;
    private Entrada entrada;
    private BigDecimal precioFinal;
    private LocalDateTime fechaReserva;
    private EstadoReserva estado;

    public Reserva(int idReserva, Cliente cliente, Funcion funcion, int numeroAsiento, Entrada entrada, BigDecimal precioFinal, LocalDateTime fechaReserva, EstadoReserva estado) {

        this.idReserva = idReserva;
        this.cliente = cliente;
        this.funcion = funcion;
        this.numeroAsiento = numeroAsiento;
        this.entrada = entrada;
        this.precioFinal = precioFinal;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
    }

    public int getIdReserva() {
        return idReserva;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Funcion getFuncion() {
        return funcion;
    }

    public int getNumeroAsiento() {
        return numeroAsiento;
    }

    public BigDecimal getPrecioFinal() {
        return precioFinal;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public LocalDateTime getFechaReserva() {
        return fechaReserva;
    }

    public void setIdReserva(int idReserva) {
        this.idReserva = idReserva;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void setFuncion(Funcion funcion) {
        this.funcion = funcion;
    }

    public void setNumeroAsiento(int numeroAsiento) {
        this.numeroAsiento = numeroAsiento;
    }

    public void setPrecioFinal(BigDecimal precioFinal) {
        this.precioFinal = precioFinal;
    }

    public void setFechaReserva(LocalDateTime fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }



    public void cancelar() {
        estado = EstadoReserva.CANCELADA;
    }

    public boolean estaConfirmada() {
        return estado == EstadoReserva.CONFIRMADA;
    }

    @Override
    public String toString() {
        return "Reserva: " + idReserva +
                " | Cliente: " + cliente.getNombre() +
                " | Funcion: " + funcion.getIdFuncion() +
                " | Asiento: " + numeroAsiento +
                " | Precio: " + precioFinal +
                " | Estado: " + estado;
    }
}

