package org.cine.modelo;

public abstract class Persona {
    private Integer idPersona;
    private String nombre;
    private String dni;
    private String email;

    public Persona() {
    }

    public Persona(Integer idPersona, String nombre, String dni, String email) {
        this.idPersona = idPersona;
        this.nombre = nombre;
        this.dni = dni;
        this.email = email;
    }

    public Integer getIdPersona() {
        return idPersona;
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
        return "Nombre : " + nombre +
                " | DNI: " + dni +
                " | Email: " + email;
}
