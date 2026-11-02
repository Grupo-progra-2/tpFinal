package org.cine.modelo;

public class Cliente {
    private int idCliente;
    private String nombre;
    private String dni;
    private String email;

    public Cliente() {
    }

    public Cliente(int idCliente, String nombre, String dni, String email) {
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.dni = dni;
        this.email = email;
    }

    public int getIdCliente() {
        return idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDni() {
        return dni;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Cliente: " + nombre +
                " | DNI: " + dni +
                " | Email: " + email;
    }
}
