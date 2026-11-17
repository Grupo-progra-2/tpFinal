package org.cine.modelo;

public abstract class Persona {
    private Integer id;
    private String nombre;
    private String apellido;
    private String dni;
    private String email;
    private String claveAcceso; //agregamos claveAcceso a la clase Persona para que tanto Cliente como Empleado puedan tener una clave de acceso,
    // y asi poder loguearse en el sistema.
   ///No estaria mal agregar telefono .



    public Persona() {
    }

    public Persona(Integer id, String nombre, String apellido, String dni, String email, String claveAcceso) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.dni = dni;
        this.email = email;
        this.claveAcceso = claveAcceso;
    }

    public Integer getId() {
        return id;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setDni(String dni) {
        this.dni = dni;
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
                " | Apellido: " + apellido +
                " | DNI: " + dni +
                " | Email: " + email;
    }
}
