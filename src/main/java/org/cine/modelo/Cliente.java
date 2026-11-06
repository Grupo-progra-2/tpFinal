package org.cine.modelo;

public class Cliente extends Persona {
    private Integer idCliente;

    public Cliente() {
        super();
    }

    public Cliente(Integer idCliente, String nombre, String dni, String email) {
        super(idCliente, nombre, dni, email);
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
