package org.cine.modelo;

public class Sala {
    private int idSala;
    private int numero;
    private int capacidad;
    private boolean habilitada;

    public Sala() {
    }

    public Sala(int idSala, int numero, int capacidad, boolean habilitada) {
        this.idSala = idSala;
        this.numero = numero;
        this.capacidad = capacidad;
        this.habilitada = habilitada;
    }

    public int getIdSala() {
        return idSala;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public int getNumero() {
        return numero;
    }

    public boolean estaHabilitada() {
        return habilitada;
    }

    public void setHabilitada(boolean habilitada) {
        this.habilitada = habilitada;
    }

    @Override
    public String toString() {
        return "Sala: " + numero +
                " | Capacidad: " + capacidad +
                " | Habilitada: " + habilitada;
    }
}
