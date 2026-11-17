package org.cine.modelo;

import org.cine.enums.EstadoFuncion;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Funcion {
    private int idFuncion;
    private Pelicula pelicula;
    private Sala sala;
    private LocalDateTime fechaHora;
    private BigDecimal precioBase;
    private EstadoFuncion estado;

    public Funcion() {
    }

    public Funcion(int idFuncion, Pelicula pelicula, Sala sala, LocalDateTime fechaHora, BigDecimal precioBase, EstadoFuncion estado) {
        this.idFuncion = idFuncion;
        this.pelicula = pelicula;
        this.sala = sala;
        this.fechaHora = fechaHora;
        this.precioBase = precioBase;
        this.estado = estado;
    }

    public int getIdFuncion() {
        return idFuncion;
    }

    public Pelicula getPelicula() {
        return pelicula;
    }

    public Sala getSala() {
        return sala;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    public EstadoFuncion getEstado() {
        return estado;
    }

    public LocalDateTime calcularFin() {
        return fechaHora.plusMinutes(
                pelicula.getDuracionMinutos()
        );
    }

    public boolean estaDisponible() {
        return estaDisponible(LocalDateTime.now());
    }

    public boolean estaDisponible(LocalDateTime ahora) {
        return estado == EstadoFuncion.PROGRAMADA
                && fechaHora != null && fechaHora.isAfter(ahora)
                && sala != null && sala.estaHabilitada()
                && pelicula != null && pelicula.estaActiva();
    }

    public void cancelar() {
        estado = EstadoFuncion.CANCELADA;
    }

    public void setIdFuncion(int idFuncion) {
        this.idFuncion = idFuncion;
    }

    @Override
    public String toString() {
        return "Funcion: " + idFuncion +
                " | Pelicula: " + pelicula.getTitulo() +
                " | Sala: " + sala.getNumero() +
                " | Fecha: " + fechaHora;
    }
}
