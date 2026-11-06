package org.cine.modelo;

public class Empleado extends Persona {
    private String legajo;
    private String claveAcceso;
    private boolean esAdmin;

    public Empleado() {  //constructor vacio para poder instanciar un objeto Empleado sin pasarle parametros, por ejemplo al traerlo de la base de datos
        super();
    }

    public Empleado(Integer idEmpleado, String nombre, String dni, String email, String legajo, String claveAcceso, boolean esAdmin) {
        super(idEmpleado, nombre, dni, email);
        this.legajo = legajo;
        this.claveAcceso = claveAcceso;
        this.esAdmin = esAdmin;
    }

    public String getLegajo() {
        return legajo;
    }

    public String getClaveAcceso() {
        return claveAcceso;
    }

    public boolean esAdmin() {
        return esAdmin;
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }

    public void setClaveAcceso(String claveAcceso) {
        this.claveAcceso = claveAcceso;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }

    @Override
    public String toString() {
        String rol = esAdmin ? "Administrador" : "Cajero";
          return "Empleado: " + nombre +
                 " | DNI: " + dni +
                 " | Email: " + email +
                 " | Legajo: " + legajo +
                 " | Rol: " + rol;   //podria dejarlo vacio y que se vea solo el rol sin poner "rol" delante, pero me parece que queda mas claro asi
     }


}
