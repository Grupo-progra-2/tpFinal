package org.cine.modelo;

import java.time.DayOfWeek;

public class Empleado extends Persona {
    private String legajo;
    private boolean esAdmin;
    private DayOfWeek diaLibre;     //dayofWeek es una clase de java.time que representa un dia de la semana, y es mas conveniente que usar un String o un int para representar el dia libre del empleado.

    public Empleado() {  //constructor vacio para poder instanciar un objeto Empleado sin pasarle parametros, por ejemplo al traerlo de la base de datos
        super();
    }

    public Empleado(Integer id, String nombre, String apellido, String dni, String email, String legajo, String claveAcceso, boolean esAdmin, DayOfWeek diaLibre) {
        super(id, nombre, apellido, dni, email, claveAcceso);
        this.legajo = legajo;
        this.esAdmin = esAdmin;
        this.diaLibre = diaLibre;
    }

    public String getLegajo() {
        return legajo;
    }

    public boolean esAdmin() {
        return esAdmin;
    }

    public void setLegajo(String legajo) {
        this.legajo = legajo;
    }

    public void setEsAdmin(boolean esAdmin) {
        this.esAdmin = esAdmin;
    }

    public DayOfWeek getDiaLibre() {
        return diaLibre;
    }
    public void setDiaLibre(DayOfWeek diaLibre) {
        this.diaLibre = diaLibre;
    }

    @Override
    public String toString() {
        String rol = esAdmin ? "Administrador" : "Cajero";
          return "Empleado: " + super.toString() +
                 " | Legajo: " + legajo +
                 " | Rol: " + rol +    //podria dejarlo vacio y que se vea solo el rol sin poner "rol" delante, pero me parece que queda mas claro .
                 " | Dia Libre: " + diaLibre;
     }


}
